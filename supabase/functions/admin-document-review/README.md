# Admin document review function

This function authorizes the existing Firebase Admin login and uses the Supabase
service-role key only on the server. The key must never be added to the Android app.

## Required Supabase secrets

Set these in the Supabase project before deploying:

- `SUPABASE_URL`
- `SUPABASE_SERVICE_ROLE_KEY`

Supabase normally provides these to Edge Functions. Confirm both are available
in the function environment. Do not paste the service-role key into GitHub or chat.

## Deploy

From the repository root, after linking the correct Supabase project:

```sh
supabase functions deploy admin-document-review --no-verify-jwt
```

The function verifies the Firebase ID token signature, issuer, audience, and exact
authorized Admin email before it uses server-side database/storage access.

Endpoints:
- GET: list documents; optional `?status=Pending|Approved|Rejected|All`
- POST `{"action":"signed_url","document_id":"..."}`: return a 60-second private-file URL
- POST `{"action":"review","document_id":"...","review_status":"Rejected","rejection_reason":"..."}`
- POST `{"action":"review","document_id":"...","review_status":"Approved"}`

The Android Admin screen must send a fresh Firebase ID token as
`Authorization: Bearer <token>`. Customer uploads continue to use their own
Supabase access token and customer-scoped RLS policies.
