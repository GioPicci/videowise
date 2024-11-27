#!/bin/sh

# Replace placeholders in config.js.template with actual environment variables
envsubst < /usr/share/nginx/html/config.js.template > /usr/share/nginx/html/config.js

# Start Nginx
exec nginx -g "daemon off;"
