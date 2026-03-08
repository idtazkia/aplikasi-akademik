#!/bin/bash
# Find active kurikulum and course summary for a prodi
# Usage: ./tools/find-courses-by-prodi.sh <prodi-id-or-name>
# Examples:
#   ./tools/find-courses-by-prodi.sh "Sistem Informasi"
#   ./tools/find-courses-by-prodi.sh 621c0232-c1db-4aee-8993-815fdd950412

SCRIPT_DIR="$(cd "$(dirname "$0")" && pwd)"
source "$SCRIPT_DIR/db-credentials.sh"

if [ $# -eq 0 ]; then
    echo "Usage: $0 <prodi-id-or-name>" >&2
    echo "  Known prodi:" >&2
    echo "    SI: 621c0232-c1db-4aee-8993-815fdd950412" >&2
    echo "    TI: 9f64c400-5850-4d18-a039-d2f2807899c8" >&2
    exit 1
fi

PRODI="$1"

# Detect if argument is UUID or name
if [[ "$PRODI" =~ ^[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}$ ]]; then
    WHERE_CLAUSE="p.id = '${PRODI}'"
else
    WHERE_CLAUSE="p.nama_prodi LIKE '%${PRODI}%'"
fi

ssh -o ConnectTimeout=10 "$DB_SSH_USER@$DB_SSH_HOST" \
    "mysql -u $DB_USER -p'$DB_PASS' $DB_NAME -e \"
SELECT p.nama_prodi, k.id as id_kurikulum, k.nama_kurikulum, k.tahun_kurikulum, k.status,
  (SELECT COUNT(*) FROM matakuliah_kurikulum mk WHERE mk.id_kurikulum = k.id AND mk.status = 'AKTIF') as total_mk,
  (SELECT MAX(mk.semester) FROM matakuliah_kurikulum mk WHERE mk.id_kurikulum = k.id AND mk.status = 'AKTIF') as max_sem
FROM kurikulum k
JOIN prodi p ON k.id_prodi = p.id
WHERE ${WHERE_CLAUSE}
AND k.status != 'HAPUS'
ORDER BY k.tahun_kurikulum;
\""
