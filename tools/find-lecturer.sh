#!/bin/bash
# Search for a lecturer by name
# Usage: ./tools/find-lecturer.sh "Endy"

SCRIPT_DIR="$(cd "$(dirname "$0")" && pwd)"
source "$SCRIPT_DIR/db-credentials.sh"

if [ $# -eq 0 ]; then
    echo "Usage: $0 <name>" >&2
    exit 1
fi

NAME="$1"

ssh -o ConnectTimeout=10 "$DB_SSH_USER@$DB_SSH_HOST" \
    "mysql -u $DB_USER -p'$DB_PASS' $DB_NAME -e \"
SELECT d.id as id_dosen, k.id as id_karyawan, k.nik, k.nama_karyawan, k.gelar
FROM dosen d
JOIN karyawan k ON d.id_karyawan = k.id
WHERE k.nama_karyawan LIKE '%${NAME}%'
AND d.status = 'AKTIF'
ORDER BY k.nama_karyawan;
\""
