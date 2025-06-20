#!/bin/bash
# A script to rebuild a single service's image using Dockerfile
# and restart the entire Docker Compose stack.

# Exit immediately if a command exits with a non-zero status.
set -e

# --- Configuration ---
SERVICE_NAME="api-gateway"
DOCKERFILE_PATH="./$SERVICE_NAME/Dockerfile"
CONTEXT_PATH="./$SERVICE_NAME"
IMAGE_NAME="majdissa34/$SERVICE_NAME:latest"
# ----------------------

echo "--- [1/4] Building JAR for $SERVICE_NAME (skipping tests)... ---"
mvn clean package -DskipTests -pl $SERVICE_NAME

echo "--- [2/4] Building Docker image for $SERVICE_NAME using Dockerfile... ---"
docker build -t $IMAGE_NAME -f $DOCKERFILE_PATH $CONTEXT_PATH

echo "--- [3/4] Tearing down the Docker environment... ---"
docker compose down

echo "--- [4/4] Starting all services with updated image... ---"
docker compose up -d

echo ""
echo "--- ✅ Full deployment complete! ---"
echo "You can now tail logs for any service, for example:"
echo "docker logs -f $SERVICE_NAME"
