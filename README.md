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
