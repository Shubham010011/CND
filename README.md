<<<<<<< HEAD
# Crypto Exchange App — Full Stack

Everything in one folder now: Spring Boot backend + React frontend.

```
crypto-exchange-project/
├── backend/     Spring Boot REST API (auth, wallets, transactions, live market data)
└── frontend/    React + Vite SPA that consumes the backend
```

Each folder has its own README with full details. Quick start for both:

```bash
# Terminal 1 — backend, runs on http://localhost:8080
cd backend
mvn spring-boot:run

# Terminal 2 — frontend, runs on http://localhost:5173
cd frontend
npm install
npm run dev
```

Open `http://localhost:5173`. The frontend is already configured
(`frontend/.env`) to call the backend at `http://localhost:8080` — change
that value if you run the backend somewhere else.

See `backend/README.md` for the full API reference and security notes,
and `frontend/README.md` for how the two sides are wired together.
=======
# Crypto Exchange App (Spring Boot)

A simulated crypto wallet + exchange backend: signup/login with JWT auth,
per-user auto-generated wallets (MetaMask-style hashed addresses),
send/receive crypto by wallet ID or QR code, and **live** market prices
pulled from CoinGecko's free public API.

> **Important — read this first:** this system does **not** touch a real
> blockchain. Wallets are internal, off-chain ledger entries (like a
> exchange's custodial balance, e.g. how Binance/Coinbase show your
> balance) — not real BTC/ETH addresses with spendable on-chain funds.
> Building a real self-custody wallet means generating and *securely
> storing raw private keys* that authorize irreversible on-chain
> transfers, which is a materially different (and much higher-risk)
> engineering problem than what's implemented here. This project is
> structured so you can swap in a real chain integration later
> (see "Going to a real chain" below) without re-architecting the API layer.

## Features

- **Signup / Login** — `POST /api/auth/register`, `POST /api/auth/login`
- **Password hashing** — BCrypt (cost factor 12), never store/log plaintext
- **JWT auth + authorization** — stateless bearer tokens, role-based access
  (`ROLE_USER`, `ROLE_ADMIN`) via Spring Security
- **Auto-generated wallet per user** — SHA-256-hashed `0x…` wallet ID,
  created at signup, seeded with demo USDT balance
- **Send / receive crypto** — transfer by wallet ID, atomic debit+credit,
  full transaction ledger/history
- **QR code wallet sharing** — `GET /api/wallet/me/qrcode` returns a
  base64 PNG QR code encoding your wallet ID (scan-to-send, like MetaMask's
  receive screen); any QR scanner library on the client decodes it back
  to a wallet ID string to fill the send form
- **Live market data** — real-time prices, 24h change, market cap via
  CoinGecko's free, keyless REST API

## Tech stack

Java 17, Spring Boot 3.3, Spring Security + JJWT, Spring Data JPA + H2
(swap for Postgres/MySQL trivially), Spring WebFlux `WebClient` for the
external market API call, ZXing for QR generation.

## Running it

```bash
cd crypto-exchange-app
mvn spring-boot:run
```

The app starts on `http://localhost:8080`. An H2 file-based DB is created
under `./data/` on first run, so your users/wallets survive restarts.

**Before doing anything real with this**, change `app.jwt.secret` in
`application.properties` to a long random value, and put it in an
environment variable instead of the properties file
(`app.jwt.secret=${JWT_SECRET}`).

## API reference

### Auth (public)

**Register**
```bash
curl -X POST http://localhost:8080/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{"username":"alice","email":"alice@example.com","password":"SuperSecret123"}'
```
Response includes a JWT and alice's new wallet ID.

**Login**
```bash
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"usernameOrEmail":"alice","password":"SuperSecret123"}'
```

Use the returned token as `Authorization: Bearer <token>` on every call below.

### Wallet (authenticated)

| Method | Endpoint | Purpose |
|---|---|---|
| GET | `/api/wallet/me` | Your wallet ID + all coin balances |
| GET | `/api/wallet/{walletId}` | Verify a wallet ID exists before sending to it |
| GET | `/api/wallet/me/qrcode` | Base64 PNG QR code of your wallet ID |

### Transactions (authenticated)

**Send crypto**
```bash
curl -X POST http://localhost:8080/api/transactions/send \
  -H "Authorization: Bearer <token>" \
  -H "Content-Type: application/json" \
  -d '{"toWalletId":"0xabc123...","coinSymbol":"USDT","amount":25.5,"note":"lunch"}'
```

**Transaction history**
```bash
curl http://localhost:8080/api/transactions/history -H "Authorization: Bearer <token>"
```

### Trade — buy / sell / swap on the demo account (authenticated)

All three execute at the live CoinGecko USD price and settle against your
own wallet's balances (USDT ⇄ coin, or coin ⇄ coin) — no real funds or
real exchange involved.

| Method | Endpoint | Purpose |
|---|---|---|
| GET | `/api/trade/quote/{coinSymbol}` | Live USD price for one coin |
| GET | `/api/trade/supported-coins` | Which symbols this demo exchange supports |
| POST | `/api/trade/buy` | `{coinSymbol, usdtAmount}` — spend USDT to buy a coin |
| POST | `/api/trade/sell` | `{coinSymbol, coinAmount}` — sell a coin back to USDT |
| POST | `/api/trade/swap` | `{fromCoinSymbol, toCoinSymbol, amount}` — trade coin-to-coin |

```bash
curl -X POST http://localhost:8080/api/trade/buy \
  -H "Authorization: Bearer <token>" \
  -H "Content-Type: application/json" \
  -d '{"coinSymbol":"BTC","usdtAmount":100}'
```

Every trade also writes a row to the same transaction ledger as SEND —
`GET /api/transactions/history` returns BUY/SELL/SWAP rows alongside
transfers, with `counterCoinSymbol`, `counterAmount`, and
`executionPriceUsd` populated for trades.

**Note on the blocking price call:** `TradeService` fetches the live price
synchronously (`.block()` on the reactive `WebClient` call) so the whole
buy/sell/swap can run inside one `@Transactional` method. That's fine at
demo scale; under real load you'd want to either cache prices with a short
TTL or move trade execution to an async flow.

### Market data (public, live)

| Endpoint | Example |
|---|---|
| `GET /api/market/prices?coins=bitcoin,ethereum&vsCurrencies=usd` | Live simple prices + 24h change |
| `GET /api/market/coins?vsCurrency=usd&perPage=50&page=1` | Ranked market listing (like an exchange ticker table) |
| `GET /api/market/coins/{coinId}` | Full detail for one coin, e.g. `/api/market/coins/bitcoin` |

CoinGecko's free tier is rate-limited (roughly 10–30 req/min per IP). For
a production-facing UI polling prices frequently, add a short server-side
cache (e.g. Caffeine, 10–30s TTL) in `MarketDataService` rather than
proxying every client request straight through.

## "Share" feature

There's no dedicated share endpoint — sharing is handled entirely on the
frontend using the browser's native share sheet (with a clipboard
fallback), seeded with a one-line summary. The backend's only role is
supplying that summary text: every `TradeResponse` includes a
ready-to-share `shareText` field (e.g. *"Bought 0.0021 BTC for 150 USDT"*)
that the frontend hands straight to `navigator.share()`.

## QR "scan to send" flow (client side)

1. Receiver opens their app, hits `/api/wallet/me/qrcode`, displays the PNG.
2. Sender's app opens the device camera (e.g. a JS QR-scanner lib on web,
   or `ML Kit`/`ZXing` on Android, `AVFoundation` on iOS) and decodes the
   QR into plain text — which is just the wallet ID string.
3. Sender's app pre-fills `toWalletId` in the send form and calls
   `/api/transactions/send` as normal.

No new backend endpoint is needed for *scanning* — QR decoding happens
entirely on the client; the backend only needs to *generate* the code
(already done) and accept a wallet ID string on `/send` (already done).

## Security notes

- Passwords are hashed with BCrypt before storage; plaintext is never
  persisted or logged.
- JWTs are signed with HMAC-SHA256; keep `app.jwt.secret` out of source
  control in real deployments (env var or a secrets manager).
- All wallet/transaction endpoints require a valid bearer token
  (`SecurityConfig` — only `/api/auth/**` and `/api/market/**` are public).
- Wallet lookup by ID only ever returns the ID itself, never balances or
  the owner's identity, so it's safe to expose for "does this address exist"
  checks before sending.
- Transfers run inside a single `@Transactional` method so a partial
  debit-without-credit can't happen.

## Going to a real chain (optional future work)

If you eventually want *actual* on-chain crypto rather than a simulated
ledger, the pieces that would change are isolated:
- `WalletAddressGenerator` → replace with real keypair generation
  (e.g. via `web3j` for Ethereum-compatible chains), and you'd need a
  secure key-management strategy (HSM, encrypted-at-rest key vault) —
  never store raw private keys in your own DB in plaintext.
- `TransactionService.sendCrypto` → replace the in-DB debit/credit with
  a signed on-chain transaction submitted through a node provider
  (Infura/Alchemy/your own node).
- Everything else (auth, JWT, controllers, QR flow, market data) stays
  the same, since it doesn't care whether balances are on-chain or off.

This is a substantially larger undertaking (key custody, gas fees, chain
reorgs, node reliability) and is intentionally out of scope for a demo/
learning project — flagging it here so the boundary is clear.
>>>>>>> 1aae317ca0558bf569b6ca2af5843e0a3c0e451a
