#!/bin/bash
set -eo pipefail

echo "Starting nginx..."

nginx -g "daemon off;"
"$@"
