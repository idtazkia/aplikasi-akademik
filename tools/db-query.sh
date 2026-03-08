#!/bin/bash
# Run a SQL query against the production database
# Usage: ./tools/db-query.sh "SELECT * FROM table LIMIT 5"
# Usage: ./tools/db-query.sh < file.sql

SCRIPT_DIR="$(cd "$(dirname "$0")" && pwd)"
source "$SCRIPT_DIR/db-credentials.sh"

if [ -t 0 ] && [ $# -gt 0 ]; then
    # Argument mode
    ssh -o ConnectTimeout=10 "$DB_SSH_USER@$DB_SSH_HOST" \
        "mysql -u $DB_USER -p'$DB_PASS' $DB_NAME -e \"$1\""
elif [ ! -t 0 ]; then
    # Pipe/file mode
    SQL=$(cat)
    ssh -o ConnectTimeout=10 "$DB_SSH_USER@$DB_SSH_HOST" \
        "mysql -u $DB_USER -p'$DB_PASS' $DB_NAME -e \"$SQL\""
else
    echo "Usage: $0 \"SQL query\"" >&2
    echo "       cat file.sql | $0" >&2
    exit 1
fi
