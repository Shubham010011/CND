/**
 * Shares a short text summary via the browser's native share sheet
 * (mobile Safari/Chrome, some desktop browsers) when available, and
 * falls back to copying it to the clipboard everywhere else.
 *
 * This is entirely client-side — it doesn't call the backend and doesn't
 * publish anything publicly; it just hands text to whatever the user
 * picks in their own share sheet (Messages, WhatsApp, Twitter/X, etc.)
 * or puts it on their clipboard to paste themselves.
 */
export async function shareText(text, title = "CryptoX") {
  if (navigator.share) {
    try {
      await navigator.share({ title, text });
      return { method: "share" };
    } catch (err) {
      // AbortError just means the user cancelled the share sheet — not a failure.
      if (err.name === "AbortError") return { method: "cancelled" };
      // Any other failure, fall through to clipboard.
    }
  }

  try {
    await navigator.clipboard.writeText(text);
    return { method: "clipboard" };
  } catch {
    return { method: "failed" };
  }
}
