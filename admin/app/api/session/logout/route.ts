import { clearSessionCookie } from "@/lib/auth/session";

export async function POST(): Promise<Response> {
  await clearSessionCookie();
  return Response.json({ success: true, data: null, error: null });
}
