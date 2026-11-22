-- Grant PostgREST table privileges to authenticated customer sessions.
-- Row-level security policies remain responsible for restricting which rows each user can access.
grant usage on schema public to authenticated;

grant select, insert, update on table public.customer_profiles to authenticated;
grant select, insert, update on table public.customer_applications to authenticated;
grant select, insert, update on table public.customer_documents to authenticated;
