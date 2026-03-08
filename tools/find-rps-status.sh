#!/bin/bash
# Check RPS completion status for a lecturer's courses
# Usage: ./tools/find-rps-status.sh "Endy Muhardin"

SCRIPT_DIR="$(cd "$(dirname "$0")" && pwd)"
source "$SCRIPT_DIR/db-credentials.sh"

if [ $# -eq 0 ]; then
    echo "Usage: $0 <lecturer-name>" >&2
    exit 1
fi

LECTURER="$1"

ssh -o ConnectTimeout=10 "$DB_SSH_USER@$DB_SSH_HOST" \
    "mysql -u $DB_USER -p'$DB_PASS' $DB_NAME -e \"
SELECT j.id as id_jadwal, m.kode_matakuliah, m.nama_matakuliah, kl.nama_kelas,
  SUM(CASE WHEN sk.rps IS NULL THEN 1 ELSE 0 END) as rps_null,
  SUM(CASE WHEN sk.rps IS NOT NULL THEN 1 ELSE 0 END) as rps_filled
FROM jadwal j
JOIN matakuliah_kurikulum mk ON j.id_matakuliah_kurikulum = mk.id
JOIN matakuliah m ON mk.id_matakuliah = m.id
JOIN kelas kl ON j.id_kelas = kl.id
JOIN dosen d ON j.id_dosen_pengampu = d.id
JOIN karyawan k ON d.id_karyawan = k.id
LEFT JOIN sesi_kuliah sk ON sk.id_jadwal = j.id
WHERE k.nama_karyawan LIKE '%${LECTURER}%'
AND j.status = 'AKTIF'
GROUP BY j.id, m.kode_matakuliah, m.nama_matakuliah, kl.nama_kelas
ORDER BY m.kode_matakuliah;
\""
