package com.gopalcsccenter.app

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration

/**
 * Cloud storage for Gopal CSC bills.
 * Each signed-in account can access only its own billing records.
 */
object BillingRepository {
    private val auth: FirebaseAuth get() = FirebaseAuth.getInstance()
    private val db: FirebaseFirestore get() = FirebaseFirestore.getInstance()

    private fun billsCollection() =
        db.collection("users").document(
            auth.currentUser?.uid ?: throw IllegalStateException("कृपया आधी लॉगिन करा")
        ).collection("bills")

    fun saveBill(
        bill: Map<String, Any>,
        onComplete: (Boolean, String?) -> Unit
    ) {
        val billNumber = bill["billNumber"]?.toString()?.trim().orEmpty()
        if (billNumber.isBlank()) {
            onComplete(false, "बिल क्रमांक उपलब्ध नाही")
            return
        }
        try {
            billsCollection().document(billNumber)
                .set(bill)
                .addOnSuccessListener { onComplete(true, null) }
                .addOnFailureListener { error ->
                    onComplete(false, error.localizedMessage ?: "बिल सेव्ह झाले नाही")
                }
        } catch (error: Exception) {
            onComplete(false, error.localizedMessage ?: "लॉगिन आवश्यक आहे")
        }
    }

    fun listenToBills(
        onChanged: (List<Map<String, Any>>) -> Unit,
        onError: (String) -> Unit
    ): ListenerRegistration? {
        return try {
            billsCollection()
                .orderBy("createdAt")
                .addSnapshotListener { snapshot, error ->
                    if (error != null) {
                        onError(error.localizedMessage ?: "बिल डेटा मिळाला नाही")
                    } else {
                        onChanged(snapshot?.documents?.mapNotNull { document ->
                            document.data
                        } ?: emptyList())
                    }
                }
        } catch (error: Exception) {
            onError(error.localizedMessage ?: "लॉगिन आवश्यक आहे")
            null
        }
    }
}
