#!/bin/bash
# Run a SQL file against the production database
# Usage: ./tools/db-run-sql.sh path/to/file.sql

SCRIPT_DIR="$(cd "$(dirname "$0")" && pwd)"
source "$SCRIPT_DIR/db-credentials.sh"

if [ $# -eq 0 ]; then
    echo "Usage: $0 <sql-file>" >&2
    exit 1
fi

SQL_FILE="$1"
if [ ! -f "$SQL_FILE" ]; then
    echo "File not found: $SQL_FILE" >&2
    exit 1
fi

scp -o ConnectTimeout=10 "$SQL_FILE" "$DB_SSH_USER@$DB_SSH_HOST:~/tmp_sql_exec.sql"
ssh -o ConnectTimeout=10 "$DB_SSH_USER@$DB_SSH_HOST" \
    "mysql -u $DB_USER -p'$DB_PASS' $DB_NAME < ~/tmp_sql_exec.sql && rm ~/tmp_sql_exec.sql"
