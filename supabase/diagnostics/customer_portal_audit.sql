-- Read-only audit for the existing Gopal CSC customer portal schema.
-- Run in Supabase SQL Editor. This script makes no changes.

-- 1) Required table columns and data types.
select table_name, column_name, data_type, is_nullable
from information_schema.columns
where table_schema = 'public'
  and table_name in ('customer_profiles', 'customer_applications', 'customer_documents')
order by table_name, ordinal_position;

-- 2) Existing RLS policies on customer portal tables.
select schemaname, tablename, policyname, permissive, roles, cmd, qual, with_check
from pg_policies
where schemaname = 'public'
  and tablename in ('customer_profiles', 'customer_applications', 'customer_documents')
order by tablename, policyname;

-- 3) Existing policies on Storage objects for the customer-documents bucket.
select policyname, permissive, roles, cmd, qual, with_check
from pg_policies
where schemaname = 'storage'
  and tablename = 'objects'
order by policyname;

-- 4) Confirm the private bucket configuration.
select id, name, public, file_size_limit, allowed_mime_types
from storage.buckets
where id = 'customer-documents';
