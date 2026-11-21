import { createClient } from "https://esm.sh/@supabase/supabase-js@2";
import { createRemoteJWKSet, jwtVerify } from "https://deno.land/x/jose@v5.9.6/index.ts";

const PROJECT_ID = "gopal-136d3";
const ADMIN_EMAIL = "636657750019@gopalcsccenter.app";
const BUCKET = "customer-documents";
const JWKS = createRemoteJWKSet(
  new URL("https://www.googleapis.com/service_accounts/v1/jwk/securetoken@system.gserviceaccount.com"),
);

const corsHeaders = {
  "Access-Control-Allow-Origin": "*",
  "Access-Control-Allow-Headers": "authorization, x-client-info, apikey, content-type",
  "Access-Control-Allow-Methods": "GET, POST, OPTIONS",
  "Content-Type": "application/json",
};

function reply(status: number, body: unknown) {
  return new Response(JSON.stringify(body), { status, headers: corsHeaders });
}

async function verifyAdmin(request: Request) {
  const header = request.headers.get("Authorization") ?? "";
  const token = header.startsWith("Bearer ") ? header.slice(7) : "";
  if (!token) throw new Error("Admin authentication required.");
  const { payload } = await jwtVerify(token, JWKS, {
    issuer: `https://securetoken.google.com/${PROJECT_ID}`,
    audience: PROJECT_ID,
  });
  if (payload.email !== ADMIN_EMAIL || typeof payload.sub !== "string" || !payload.sub) {
    throw new Error("Admin access denied.");
  }
  return { uid: payload.sub, email: payload.email };
}

Deno.serve(async (request) => {
  if (request.method === "OPTIONS") return new Response("ok", { headers: corsHeaders });
  if (request.method !== "GET" && request.method !== "POST") {
    return reply(405, { error: "Method not allowed." });
  }

  try {
    await verifyAdmin(request);

    const url = Deno.env.get("SUPABASE_URL");
    const serviceKey = Deno.env.get("SUPABASE_SERVICE_ROLE_KEY");
    if (!url || !serviceKey) throw new Error("Server configuration is incomplete.");

    const supabase = createClient(url, serviceKey, {
      auth: { persistSession: false, autoRefreshToken: false },
    });

    if (request.method === "GET") {
      const status = new URL(request.url).searchParams.get("status") ?? "Pending";
      if (!["Pending", "Approved", "Rejected", "All"].includes(status)) {
        return reply(400, { error: "Invalid status." });
      }
      let query = supabase
        .from("customer_documents")
        .select("id, application_id, customer_id, document_type, storage_path, review_status, rejection_reason, uploaded_at, reviewed_at, customer_applications(application_no, service_name, status)")
        .order("uploaded_at", { ascending: false })
        .limit(200);
      if (status !== "All") query = query.eq("review_status", status);
      const { data, error } = await query;
      if (error) throw error;

      const ids = [...new Set((data ?? []).map((row) => row.customer_id))];
      const profilesResult = ids.length
        ? await supabase.from("customer_profiles").select("id, full_name, phone").in("id", ids)
        : { data: [], error: null };
      if (profilesResult.error) throw profilesResult.error;
      const profiles = new Map((profilesResult.data ?? []).map((p) => [p.id, p]));
      const documents = (data ?? []).map((row) => ({
        ...row,
        customer: profiles.get(row.customer_id) ?? null,
      }));
      return reply(200, { documents });
    }

    const body = await request.json();
    if (!body || typeof body !== "object" || typeof body.action !== "string") {
      return reply(400, { error: "Invalid request." });
    }

    if (body.action === "signed_url") {
      if (typeof body.document_id !== "string") return reply(400, { error: "Document ID required." });
      const { data: doc, error: docError } = await supabase
        .from("customer_documents")
        .select("storage_path")
        .eq("id", body.document_id)
        .single();
      if (docError || !doc) return reply(404, { error: "Document not found." });
      const { data, error } = await supabase.storage
        .from(BUCKET)
        .createSignedUrl(doc.storage_path, 60);
      if (error) throw error;
      return reply(200, { signed_url: data.signedUrl, expires_in: 60 });
    }

    if (body.action === "review") {
      const id = typeof body.document_id === "string" ? body.document_id : "";
      const status = body.review_status;
      const reason = typeof body.rejection_reason === "string" ? body.rejection_reason.trim() : "";
      if (!id || !["Approved", "Rejected"].includes(status)) {
        return reply(400, { error: "Invalid review request." });
      }
      if (status === "Rejected" && (!reason || reason.length > 500)) {
        return reply(400, { error: "नकारण्याचे कारण 1 ते 500 अक्षरांत लिहा." });
      }
      const { data, error } = await supabase
        .from("customer_documents")
        .update({
          review_status: status,
          rejection_reason: status === "Rejected" ? reason : null,
          reviewed_at: new Date().toISOString(),
        })
        .eq("id", id)
        .eq("review_status", "Pending")
        .select("id, review_status, rejection_reason, reviewed_at")
        .maybeSingle();
      if (error) throw error;
      if (!data) return reply(409, { error: "हे कागदपत्र आधीच तपासले गेले आहे किंवा उपलब्ध नाही." });
      return reply(200, { document: data });
    }

    return reply(400, { error: "Unknown action." });
  } catch (error) {
    const message = error instanceof Error ? error.message : "Request failed.";
    const status = message.includes("authentication") || message.includes("denied") ? 401 : 500;
    return reply(status, { error: message });
  }
});
