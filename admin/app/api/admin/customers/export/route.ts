import { internalApiBaseUrl } from "@/lib/config/environment";
import { getSessionToken } from "@/lib/auth/session";

export async function GET(request: Request): Promise<Response> {
  const sessionToken = await getSessionToken();
  const { search } = new URL(request.url);

  const backendResponse = await fetch(`${internalApiBaseUrl()}/api/v1/admin/customers/export${search}`, {
    headers: sessionToken ? { Authorization: `Bearer ${sessionToken}` } : {},
    cache: "no-store",
  });

  const csv = await backendResponse.arrayBuffer();
  return new Response(csv, {
    status: backendResponse.status,
    headers: {
      "Content-Type": backendResponse.headers.get("Content-Type") ?? "text/csv",
      "Content-Disposition":
        backendResponse.headers.get("Content-Disposition") ?? "attachment; filename=customers.csv",
    },
  });
}
