const EXPIRY_SAFETY_BUFFER_SECONDS = 60;

function decodeJwtPayload(token: string): Record<string, unknown> | null {
  const payloadSegment = token.split(".")[1];
  if (!payloadSegment) {
    return null;
  }
  try {
    const base64 = payloadSegment.replace(/-/g, "+").replace(/_/g, "/");
    const padded = base64.padEnd(base64.length + ((4 - (base64.length % 4)) % 4), "=");
    return JSON.parse(Buffer.from(padded, "base64").toString("utf8")) as Record<string, unknown>;
  } catch {
    return null;
  }
}

export function isAccessTokenExpiringSoon(accessToken: string): boolean {
  const payload = decodeJwtPayload(accessToken);
  const expiresAtSeconds = typeof payload?.exp === "number" ? payload.exp : undefined;
  if (expiresAtSeconds === undefined) {
    return true;
  }
  const nowSeconds = Date.now() / 1000;
  return expiresAtSeconds - nowSeconds <= EXPIRY_SAFETY_BUFFER_SECONDS;
}
