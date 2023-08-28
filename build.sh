#!/usr/bin/env bash
echo "build web"
cd web
pnpm install
pnpm build
echo "build server"
cd ..
mvn -B package --file pom.xml -DskipTests

