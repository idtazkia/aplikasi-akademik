#!/bin/bash
# Find students enrolled in a jadwal (via krs_detail)
# Usage: ./tools/find-students-by-course.sh <jadwal-id>

SCRIPT_DIR="$(cd "$(dirname "$0")" && pwd)"
source "$SCRIPT_DIR/db-credentials.sh"

if [ $# -eq 0 ]; then
    echo "Usage: $0 <jadwal-id>" >&2
    exit 1
fi

JADWAL_ID="$1"

ssh -o ConnectTimeout=10 "$DB_SSH_USER@$DB_SSH_HOST" \
    "mysql -u $DB_USER -p'$DB_PASS' $DB_NAME -e \"
SELECT mhs.nim, mhs.nama, kd.status_edom, kd.nilai_akhir, kd.grade, kd.bobot
FROM krs_detail kd
JOIN mahasiswa mhs ON kd.id_mahasiswa = mhs.id
WHERE kd.id_jadwal = '${JADWAL_ID}'
AND kd.status = 'AKTIF'
ORDER BY mhs.nim;
\""
