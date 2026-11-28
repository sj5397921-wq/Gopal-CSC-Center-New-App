# Gopal CSC Computer Billing

## Features
- Admin email/password login using the same Firebase Authentication account as Android.
- Creates bills in the shared Firestore collection `billingRecords`.
- Live updates across the Android app and browser portal.
- Marathi + English bill fields.
- Select bills and print four selected receipts on one A4 sheet (2 × 2).

## Firebase setup (one-time)
1. Open Firebase Console and select project `gopal-136d3`.
2. Confirm **Authentication → Sign-in method → Email/Password** is enabled.
3. Confirm **Firestore Database** has been created.
4. Under **Authentication → Settings → Authorized domains**, add `gopal-136d3.web.app` if it is not already listed.
5. Review `firestore.rules`. It permits reads/writes only for the Admin account `636657750019@gopalcsccenter.app` in `billingRecords`. If this project already has other Firestore collections/rules, merge these billing rules into the existing rules instead of replacing unrelated access rules.

## Deploy from a computer
Install Node.js and Firebase CLI, then run these commands from the repository root:

```bash
npm install -g firebase-tools
firebase login
firebase deploy --project gopal-136d3 --only firestore:rules,hosting
```

After successful deployment, open:
- https://gopal-136d3.web.app

Sign in with the same Admin email and password used in the Android app. Do not share the password.

## Important
- Do not make Firestore rules public (for example, `allow read, write: if true`).
- The Android app and web portal use the same top-level `billingRecords` collection.
- Only bills created while signed in as the authorized Admin are added to the shared collection. Customer quotations remain local.
- A successful GitHub source commit is not the same as a deployed website or a tested APK. Deploy and test before using for real customer billing.
