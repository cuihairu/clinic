#!/usr/bin/env bash
cd web
rm -rf dist
pnpm install
npm run build
sudo sudo cp -r dist/* /usr/share/nginx/html/
