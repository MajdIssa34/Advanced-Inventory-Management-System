#!/bin/bash
# A script to rebuild ALL services using Dockerfiles
# and restart Docker Compose from scratch.

set -euo pipefail  # Safer: exit on error, undefined vars, or failed pipe

# --- Services list ---
SERVICES=("api-gateway" "product-service" "order-service" "inventory-service" "discovery-server")
SERVICES2=("product-service")

echo ""
echo "--- [1/4] Cleaning and packaging all microservices (skip tests)... ---"
mvn clean package -DskipTests

echo ""
echo "--- [2/4] Building Docker images using Dockerfiles... ---"
for SERVICE in "${SERVICES[@]}"; do
  IMAGE_NAME="majdissa34/$SERVICE:latest"
  DOCKERFILE="./$SERVICE/Dockerfile"
  CONTEXT="./$SERVICE"
  echo "-> Building $IMAGE_NAME from $DOCKERFILE"
  docker build -t $IMAGE_NAME -f $DOCKERFILE $CONTEXT
done

echo ""
echo "--- [3/4] Shutting down all running containers... ---"
docker compose down --remove-orphans

echo ""
echo "--- [4/4] Spinning up all containers fresh... ---"
docker compose up --build -d

echo ""
echo "--- ✅ Full stack is up and running! ---"
echo "Use 'docker ps' to verify container status."
echo "Tail logs with: docker logs -f api-gateway"
