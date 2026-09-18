# Crypto Exchange Frontend (React + Vite)

React SPA that talks to the `crypto-exchange-app` Spring Boot backend over
REST: signup/login, wallet dashboard with a receive QR code, send crypto
(manual entry or camera QR scan), transaction history, and a live market
price ticker.

## Setup

```bash
cd crypto-exchange-frontend
npm install
npm run dev
```

Runs at `http://localhost:5173` by default.

**Make sure the backend is running first** (`mvn spring-boot:run` in
`crypto-exchange-app`, on `http://localhost:8080`). The frontend calls it
directly via axios — no proxy step needed since the backend's
`SecurityConfig` already allows CORS from any origin.

## Connecting to a different backend URL

Edit `.env`:

```
VITE_API_BASE_URL=http://localhost:8080
```

Point this at wherever your Spring Boot app is actually running (e.g. a
deployed URL) and restart `npm run dev` / rebuild.

## How the two apps talk to each other

- `src/api/client.js` is the single place that knows the backend's base
  URL and every endpoint path. Everything else in the app calls functions
  exported from here — nothing else constructs a URL by hand.
- On login/register, the JWT returned by the backend
  (`/api/auth/login`, `/api/auth/register`) is stored in `localStorage`
  and attached as `Authorization: Bearer <token>` to every subsequent
  request via an axios request interceptor.
- A response interceptor watches for `401`s (expired/invalid token) and
  automatically logs the user out and redirects to `/login`, so the app
  never gets stuck showing a broken "authenticated" UI with a dead token.
- The **Send** page's QR scanner (`html5-qrcode`) decodes a scanned QR
  purely client-side into a wallet-ID string — it never calls the
  backend to decode anything. The backend's job is only to *generate*
  the QR (`GET /api/wallet/me/qrcode`) and accept a wallet ID string on
  `POST /api/transactions/send`.
- The **Market** page polls `GET /api/market/coins` every 30 seconds —
  that endpoint is public (no token needed), matching the backend's
  `permitAll()` rule for `/api/market/**`.

## Pages

| Route | Auth required | Purpose |
|---|---|---|
| `/market` | No | Live price ticker (also the default landing page) |
| `/login`, `/register` | No | Auth forms |
| `/dashboard` | Yes | Wallet ID, balances, "Receive" QR code, "Share portfolio" |
| `/trade` | Yes | Buy / sell / swap tabs, live-quoted, "Share this trade" |
| `/send` | Yes | Send crypto by wallet ID or by scanning a QR |
| `/history` | Yes | Full ledger — transfers and trades (buy/sell/swap) |

## Sharing

`src/utils/share.js` wraps the Web Share API (`navigator.share`) with a
clipboard fallback for browsers/desktop contexts that don't support it.
It's used in two places:
- **Trade page** — after a successful buy/sell/swap, a "Share this trade"
  button shares the backend's `shareText` (e.g. *"Bought 0.0021 BTC for
  150 USDT"*).
- **Dashboard** — a "Share portfolio" button builds a one-line summary of
  current non-zero balances client-side and shares that.

This never posts anything publicly on its own — it hands text to
whatever the user picks in their own device's share sheet (or copies it
to their clipboard), the same way any "Share" button on a phone works.

## Notes

- Camera-based QR scanning requires the browser to have camera
  permission, and most browsers require **HTTPS or localhost** to grant
  camera access at all — this works fine on `localhost:5173` in dev, but
  if you deploy the frontend elsewhere, it needs to be served over HTTPS
  for scanning to work.
- The production build (`npm run build`) bundles the QR scanner library,
  which pushes the JS bundle past Vite's default 500kB warning threshold.
  It's harmless, but if you want to trim it, code-split the `Send` page
  with `React.lazy()`.
