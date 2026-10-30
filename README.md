# CryptoX Cloud-Native Microservices

This is the microservices version of the original CryptoX monolith. The React UI and public API paths are preserved.

## Architecture

Browser -> Frontend (Nginx) -> API Gateway (Nginx) -> Auth / Market / Wallet / Trade services.

- **auth-service**: registration, login, BCrypt passwords, JWT creation; owns user DB.
- **wallet-service**: wallet addresses, balances, sends, QR codes, transaction ledger; owns wallet DB.
- **market-service**: CoinGecko integration; stateless.
- **trade-service**: buy/sell/swap orchestration; stateless; calls market + wallet.
- **api-gateway**: one API entry point and route-by-path.
- **frontend**: original React/Vite application served by Nginx.

Each stateful service owns its own database, which is an important microservices boundary. H2 file databases are kept to make the college project easy to run; PostgreSQL can replace them later.

## Docker Compose (easiest)

```bash
cp .env.example .env
# edit JWT_SECRET and INTERNAL_KEY
docker compose up --build
```

Open **http://localhost:3000**. API gateway is also exposed at **http://localhost:8080**.

Stop: `docker compose down`
Reset demo databases: `docker compose down -v`

## Kubernetes (Minikube / Docker Desktop Kubernetes)

Build images first:
```bash
./build-images.sh
```

For Minikube, build inside Minikube's Docker daemon or load images with `minikube image load`. Then:
```bash
kubectl apply -f k8s/crypto-exchange.yaml
kubectl get pods
kubectl get services
```

With Minikube:
```bash
minikube service frontend
```

The manifest exposes frontend on NodePort **30080**.

## Demonstrated CND concepts

Microservices, API Gateway, independent service databases, service-to-service HTTP communication, stateless horizontal scaling, containers, Docker Compose orchestration, Kubernetes Deployments/Services, Secrets, persistent volumes, and replica scaling.

## Important demo note

This is a simulated exchange and wallet ledger. It does not custody real cryptocurrency or blockchain private keys.
