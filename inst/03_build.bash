#!/bin/bash
echo "start build"

echo "BEA_VERSION_TIMESTAMP"

echo "Build images via docker compose build, tail -f build_extr.log to follow along"
docker compose -f ../docker-build/mba-stack/docker-compose.yml build --force-rm --no-cache >> ./build_extr.log 2>&1

echo "check for built images"
docker images | grep -i mba

echo "finish build"
