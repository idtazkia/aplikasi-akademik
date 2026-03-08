#!/bin/bash
# Full database backup with gzip and timestamp
# Saves to both server ~/backup_* and local backup/ folder
# Usage: ./tools/db-backup.sh

SCRIPT_DIR="$(cd "$(dirname "$0")" && pwd)"
PROJECT_DIR="$(cd "$SCRIPT_DIR/.." && pwd)"
source "$SCRIPT_DIR/db-credentials.sh"

TIMESTAMP=$(date +%Y%m%d_%H%M%S)
FILENAME="backup_${DB_NAME}_${TIMESTAMP}.sql.gz"
LOCAL_BACKUP_DIR="$PROJECT_DIR/backup"

mkdir -p "$LOCAL_BACKUP_DIR"

echo "Creating backup on server..."
ssh -o ConnectTimeout=10 "$DB_SSH_USER@$DB_SSH_HOST" \
    "mysqldump -u $DB_USER -p'$DB_PASS' $DB_NAME | gzip > ~/$FILENAME"

echo "Downloading backup..."
scp -o ConnectTimeout=10 "$DB_SSH_USER@$DB_SSH_HOST:~/$FILENAME" "$LOCAL_BACKUP_DIR/"

echo "Backup saved:"
echo "  Server: ~/$FILENAME"
echo "  Local:  $LOCAL_BACKUP_DIR/$FILENAME"
ls -lh "$LOCAL_BACKUP_DIR/$FILENAME"
