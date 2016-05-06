#!/bin/bash
set -eo pipefail

echo "IP ${GEOTRELLISADMIN_ADMINSERVER_1_PORT_8080_TCP_ADDR}"
echo "PORT ${GEOTRELLISADMIN_ADMINSERVER_1_PORT_8080_TCP_PORT}"

sed -i.bak "s/{GT-ADMIN-ADDR}/${GEOTRELLISADMIN_ADMINSERVER_1_PORT_8080_TCP_ADDR}/g" /etc/nginx/nginx.conf
sed -i.bak "s/{GT-ADMIN-PORT}/${GEOTRELLISADMIN_ADMINSERVER_1_PORT_8080_TCP_PORT}/g" /etc/nginx/nginx.conf

nginx -g "daemon off;"
"$@"
