package com.gopalcsccenter.app

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration

/**
 * Shared billing collection for the signed-in Gopal CSC administrator.
 * Firestore rules must restrict billingRecords to the Admin email.
 */
object BillingRepository {
    private const val ADMIN_EMAIL = "636657750019@gopalcsccenter.app"
    private val auth: FirebaseAuth get() = FirebaseAuth.getInstance()
    private val db: FirebaseFirestore get() = FirebaseFirestore.getInstance()

    private fun isAdmin(): Boolean = auth.currentUser?.email == ADMIN_EMAIL

    fun saveBill(
        bill: Map<String, Any>,
        onComplete: (Boolean, String?) -> Unit
    ) {
        if (!isAdmin()) {
            // Customer quotations remain local; only Admin-issued bills enter shared billing.
            onComplete(true, null)
            return
        }
        val billNumber = bill["billNumber"]?.toString()?.trim().orEmpty()
        if (billNumber.isBlank()) {
            onComplete(false, "बिल क्रमांक उपलब्ध नाही")
            return
        }
        db.collection("billingRecords").document(billNumber)
            .set(bill)
            .addOnSuccessListener { onComplete(true, null) }
            .addOnFailureListener { error ->
                onComplete(false, error.localizedMessage ?: "बिल सेव्ह झाले नाही")
            }
    }

    fun listenToBills(
        onChanged: (List<Map<String, Any>>) -> Unit,
        onError: (String) -> Unit
    ): ListenerRegistration? {
        if (!isAdmin()) {
            onError("फक्त Admin बिल रेकॉर्ड पाहू शकतो.")
            return null
        }
        return db.collection("billingRecords")
            .orderBy("createdAt")
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    onError(error.localizedMessage ?: "बिल डेटा मिळाला नाही")
                } else {
                    onChanged(snapshot?.documents?.mapNotNull { it.data } ?: emptyList())
                }
            }
    }
}
