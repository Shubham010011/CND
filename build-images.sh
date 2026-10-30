#!/usr/bin/env sh
set -e
docker build -t cryptox/auth-service:1.0 services/auth-service
docker build -t cryptox/market-service:1.0 services/market-service
docker build -t cryptox/wallet-service:1.0 services/wallet-service
docker build -t cryptox/trade-service:1.0 services/trade-service
docker build -t cryptox/api-gateway:1.0 gateway
docker build -t cryptox/frontend:1.0 frontend
