-- Customer service form details
alter table public.customer_applications
  add column if not exists reference_no text,
  add column if not exists service_fee numeric(12,2) not null default 0,
  add column if not exists payment_status text not null default 'बाकी'
    check (payment_status in ('बाकी', 'रोख भरले', 'UPI भरले'));
