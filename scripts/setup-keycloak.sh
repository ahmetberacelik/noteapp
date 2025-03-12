#!/bin/bash

# Keycloak setup script for NoteApp
# This script imports realm configuration into Keycloak

# Configuration
KEYCLOAK_CONTAINER="noteapp-keycloak"
REALM_FILE="./keycloak/realm-export.json"
KEYCLOAK_URL="http://localhost:8080"
KEYCLOAK_ADMIN="admin"
KEYCLOAK_PASSWORD="admin"

echo "Starting Keycloak setup at $(date)"

# Check if Keycloak container is running
if ! docker ps | grep -q $KEYCLOAK_CONTAINER; then
    echo "Keycloak container not running. Please start it first."
    exit 1
fi

# Wait for Keycloak to be ready
echo "Waiting for Keycloak to be ready..."
READY=false
MAX_ATTEMPTS=30
ATTEMPTS=0

while [ $READY = false ] && [ $ATTEMPTS -lt $MAX_ATTEMPTS ]; do
    ATTEMPTS=$((ATTEMPTS + 1))
    echo "Attempt $ATTEMPTS of $MAX_ATTEMPTS..."
    
    if curl -s "$KEYCLOAK_URL/health" | grep -q "UP"; then
        READY=true
        echo "Keycloak is ready!"
    else
        echo "Keycloak not ready yet. Waiting..."
        sleep 5
    fi
done

if [ $READY = false ]; then
    echo "Keycloak did not become ready in time. Aborting setup."
    exit 1
fi

# Get access token for admin
echo "Getting admin access token..."
TOKEN=$(curl -s \
    -d "client_id=admin-cli" \
    -d "username=$KEYCLOAK_ADMIN" \
    -d "password=$KEYCLOAK_PASSWORD" \
    -d "grant_type=password" \
    "$KEYCLOAK_URL/realms/master/protocol/openid-connect/token" | jq -r '.access_token')

if [ -z "$TOKEN" ] || [ "$TOKEN" = "null" ]; then
    echo "Failed to obtain admin token. Check credentials."
    exit 1
fi

# Check if realm already exists
REALM_EXISTS=$(curl -s \
    -H "Authorization: Bearer $TOKEN" \
    "$KEYCLOAK_URL/admin/realms/noteapp" | jq -r '.realm')

if [ "$REALM_EXISTS" = "noteapp" ]; then
    echo "Realm 'noteapp' already exists. Skipping import."
else
    # Import realm
    echo "Importing realm configuration..."
    curl -s \
        -H "Authorization: Bearer $TOKEN" \
        -H "Content-Type: application/json" \
        -d @"$REALM_FILE" \
        "$KEYCLOAK_URL/admin/realms"
    
    if [ $? -eq 0 ]; then
        echo "Realm imported successfully!"
    else
        echo "Failed to import realm."
        exit 1
    fi
fi

# Copy custom theme (if exists)
if [ -d "./keycloak/themes/noteapp" ]; then
    echo "Installing custom theme..."
    docker cp ./keycloak/themes/noteapp $KEYCLOAK_CONTAINER:/opt/keycloak/themes/
    
    # Restart Keycloak to apply theme
    docker restart $KEYCLOAK_CONTAINER
    echo "Custom theme installed and Keycloak restarted."
fi

echo "Keycloak setup completed at $(date)"