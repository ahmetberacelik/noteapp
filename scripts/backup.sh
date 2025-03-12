#!/bin/bash

# Backup script for NoteApp
# This script creates backups of the PostgreSQL database and uploads them to a backup location

# Configuration
BACKUP_DIR="./backups"
POSTGRES_CONTAINER="noteapp-postgres"
DB_NAME="noteapp"
DB_USER="noteapp"
TIMESTAMP=$(date +"%Y%m%d_%H%M%S")
BACKUP_FILE="${BACKUP_DIR}/noteapp_${TIMESTAMP}.sql.gz"

# Ensure backup directory exists
mkdir -p $BACKUP_DIR

echo "Starting backup process at $(date)"

# Check if PostgreSQL container is running
if ! docker ps | grep -q $POSTGRES_CONTAINER; then
    echo "PostgreSQL container not running. Aborting backup."
    exit 1
fi

# Create database backup
echo "Creating PostgreSQL backup..."
docker exec $POSTGRES_CONTAINER pg_dump -U $DB_USER $DB_NAME | gzip > $BACKUP_FILE

# Check if backup was successful
if [ $? -eq 0 ]; then
    echo "Database backup completed successfully: $BACKUP_FILE"
else
    echo "Database backup failed!"
    exit 1
fi

# Optional: Remove backups older than 30 days
find $BACKUP_DIR -name "noteapp_*.sql.gz" -type f -mtime +30 -delete

echo "Backup process completed at $(date)"