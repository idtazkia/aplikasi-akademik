#!/bin/bash
# Find courses in a curriculum by semester
# Usage: ./tools/find-courses-by-semester.sh <kurikulum-id> [semester-number]
# If semester is omitted, shows all semesters

SCRIPT_DIR="$(cd "$(dirname "$0")" && pwd)"
source "$SCRIPT_DIR/db-credentials.sh"

if [ $# -eq 0 ]; then
    echo "Usage: $0 <kurikulum-id> [semester-number]" >&2
    echo "  Known kurikulum IDs:" >&2
    echo "    SI 2024-2025: 35a90e39-1a25-49e2-8cdb-2aa8df463ee0" >&2
    echo "    TI 2024-2025: ded1b986-1931-11f1-a1c0-bc2411b4c729" >&2
    exit 1
fi

KURIKULUM_ID="$1"
SEMESTER_FILTER=""
if [ $# -ge 2 ]; then
    SEMESTER_FILTER="AND mk.semester = $2"
fi

ssh -o ConnectTimeout=10 "$DB_SSH_USER@$DB_SSH_HOST" \
    "mysql -u $DB_USER -p'$DB_PASS' $DB_NAME -e \"
SELECT mk.semester, m.kode_matakuliah, m.nama_matakuliah, m.jumlah_sks, mk.status
FROM matakuliah_kurikulum mk
JOIN matakuliah m ON mk.id_matakuliah = m.id
WHERE mk.id_kurikulum = '${KURIKULUM_ID}'
${SEMESTER_FILTER}
ORDER BY mk.semester, m.kode_matakuliah;
\""
