-- Gopal CSC Center: customer portal schema
-- Apply only after Supabase Auth accounts and trusted Admin role provisioning are implemented.
-- Admin access is granted ONLY by trusted server-side app_metadata.role = 'admin'.
-- Never set app_metadata from the Android client.

create table if not exists public.customer_profiles (
  id uuid primary key references auth.users(id) on delete cascade,
  full_name text not null,
  phone text not null unique,
  created_at timestamptz not null default now()
);

create table if not exists public.customer_applications (
  id uuid primary key default gen_random_uuid(),
  customer_id uuid not null references auth.users(id) on delete cascade,
  application_no text not null unique,
  service_name text not null,
  status text not null default 'Submitted',
  created_at timestamptz not null default now()
);

create table if not exists public.customer_documents (
  id uuid primary key default gen_random_uuid(),
  application_id uuid not null references public.customer_applications(id) on delete cascade,
  customer_id uuid not null references auth.users(id) on delete cascade,
  document_type text not null,
  storage_path text not null unique,
  review_status text not null default 'Pending'
    check (review_status in ('Pending', 'Approved', 'Rejected')),
  rejection_reason text,
  uploaded_at timestamptz not null default now(),
  reviewed_at timestamptz
);

create index if not exists customer_applications_customer_id_idx
  on public.customer_applications(customer_id);
create index if not exists customer_documents_customer_id_idx
  on public.customer_documents(customer_id);
create index if not exists customer_documents_application_id_idx
  on public.customer_documents(application_id);

alter table public.customer_profiles enable row level security;
alter table public.customer_applications enable row level security;
alter table public.customer_documents enable row level security;

-- Remove only policies with these exact names so this migration can be re-run safely.
drop policy if exists "profile_select_own_or_admin" on public.customer_profiles;
drop policy if exists "profile_insert_own" on public.customer_profiles;
drop policy if exists "profile_update_own_or_admin" on public.customer_profiles;
drop policy if exists "applications_select_own_or_admin" on public.customer_applications;
drop policy if exists "applications_insert_own_or_admin" on public.customer_applications;
drop policy if exists "applications_update_admin" on public.customer_applications;
drop policy if exists "documents_select_own_or_admin" on public.customer_documents;
drop policy if exists "documents_insert_own" on public.customer_documents;
drop policy if exists "documents_replace_rejected_own" on public.customer_documents;
drop policy if exists "documents_review_admin" on public.customer_documents;

create policy "profile_select_own_or_admin"
on public.customer_profiles for select to authenticated
using (id = auth.uid() or (auth.jwt() -> 'app_metadata' ->> 'role') = 'admin');

create policy "profile_insert_own"
on public.customer_profiles for insert to authenticated
with check (id = auth.uid());

create policy "profile_update_own_or_admin"
on public.customer_profiles for update to authenticated
using (id = auth.uid() or (auth.jwt() -> 'app_metadata' ->> 'role') = 'admin')
with check (id = auth.uid() or (auth.jwt() -> 'app_metadata' ->> 'role') = 'admin');

create policy "applications_select_own_or_admin"
on public.customer_applications for select to authenticated
using (customer_id = auth.uid() or (auth.jwt() -> 'app_metadata' ->> 'role') = 'admin');

create policy "applications_insert_own_or_admin"
on public.customer_applications for insert to authenticated
with check (customer_id = auth.uid() or (auth.jwt() -> 'app_metadata' ->> 'role') = 'admin');

create policy "applications_update_admin"
on public.customer_applications for update to authenticated
using ((auth.jwt() -> 'app_metadata' ->> 'role') = 'admin')
with check ((auth.jwt() -> 'app_metadata' ->> 'role') = 'admin');

create policy "documents_select_own_or_admin"
on public.customer_documents for select to authenticated
using (customer_id = auth.uid() or (auth.jwt() -> 'app_metadata' ->> 'role') = 'admin');

create policy "documents_insert_own"
on public.customer_documents for insert to authenticated
with check (
  customer_id = auth.uid()
  and exists (
    select 1 from public.customer_applications a
    where a.id = application_id and a.customer_id = auth.uid()
  )
);

-- A customer may replace only a document previously rejected by Admin.
create policy "documents_replace_rejected_own"
on public.customer_documents for update to authenticated
using (customer_id = auth.uid() and review_status = 'Rejected')
with check (
  customer_id = auth.uid()
  and review_status = 'Pending'
  and rejection_reason is null
  and exists (
    select 1 from public.customer_applications a
    where a.id = application_id and a.customer_id = auth.uid()
  )
);

create policy "documents_review_admin"
on public.customer_documents for update to authenticated
using ((auth.jwt() -> 'app_metadata' ->> 'role') = 'admin')
with check ((auth.jwt() -> 'app_metadata' ->> 'role') = 'admin');

-- Private Storage policies for bucket customer-documents.
-- Object names must start with the authenticated user's UUID: <uid>/<application-id>/<filename>
drop policy if exists "customer_docs_read_own_or_admin" on storage.objects;
drop policy if exists "customer_docs_upload_own" on storage.objects;
drop policy if exists "customer_docs_update_own" on storage.objects;
drop policy if exists "customer_docs_admin_all" on storage.objects;

create policy "customer_docs_read_own_or_admin"
on storage.objects for select to authenticated
using (
  bucket_id = 'customer-documents'
  and (
    (storage.foldername(name))[1] = auth.uid()::text
    or (auth.jwt() -> 'app_metadata' ->> 'role') = 'admin'
  )
);

create policy "customer_docs_upload_own"
on storage.objects for insert to authenticated
with check (
  bucket_id = 'customer-documents'
  and (storage.foldername(name))[1] = auth.uid()::text
);

create policy "customer_docs_update_own"
on storage.objects for update to authenticated
using (
  bucket_id = 'customer-documents'
  and (storage.foldername(name))[1] = auth.uid()::text
)
with check (
  bucket_id = 'customer-documents'
  and (storage.foldername(name))[1] = auth.uid()::text
);

create policy "customer_docs_admin_all"
on storage.objects for all to authenticated
using (
  bucket_id = 'customer-documents'
  and (auth.jwt() -> 'app_metadata' ->> 'role') = 'admin'
)
with check (
  bucket_id = 'customer-documents'
  and (auth.jwt() -> 'app_metadata' ->> 'role') = 'admin'
);
