#!/bin/sh

docker build --platform linux/arm64 -f src/main/docker/Dockerfile.arm -t brunoborges/rinha-brborges-api:arm64 .
docker push brunoborges/rinha-brborges-api:arm64

docker build --platform linux/amd64 -f src/main/docker/Dockerfile.jvm -t brunoborges/rinha-brborges-api:latest .
docker push brunoborges/rinha-brborges-api:latest
