#!/bin/bash
# A script to rebuild, PUSH, and restart all services.

set -euo pipefail  # Safer: exit on error, undefined vars, or failed pipe

# --- Services list ---
SERVICES=("api-gateway" "product-service" "order-service" "inventory-service" "discovery-server")

echo ""
echo "--- [1/5] Cleaning and packaging all microservices (skip tests)... ---"
mvn clean package -DskipTests

echo ""
echo "--- [2/5] Building Docker images using Dockerfiles... ---"
for SERVICE in "${SERVICES[@]}"; do
  IMAGE_NAME="majdissa34/$SERVICE:latest"
  DOCKERFILE="./$SERVICE/Dockerfile"
  CONTEXT="./$SERVICE"
  echo "-> Building $IMAGE_NAME from $DOCKERFILE"
  docker build -t "$IMAGE_NAME" -f "$DOCKERFILE" "$CONTEXT"
done

echo ""
echo "--- [3/5] Pushing new images to Docker Hub... ---"
for SERVICE in "${SERVICES[@]}"; do
  IMAGE_NAME="majdissa34/$SERVICE:latest"
  echo "-> Pushing $IMAGE_NAME"
  docker push "$IMAGE_NAME"
done

echo ""
echo "--- [4/5] Shutting down all running containers... ---"
docker compose down --remove-orphans

#echo ""
#echo "--- [5/5] Spinning up all containers fresh... ---"
#docker compose up --build -d

#echo ""
#echo "--- ✅ Full stack has been pushed and is running locally! ---"
#echo "Use 'docker ps' to verify container status."