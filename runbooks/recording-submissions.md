# Recording Student Submissions

Record student assignment submissions (YouTube, Instagram, TikTok links) into `jadwal_tugas_jawaban` and set the score from `krs_nilai_tugas`.

## Steps

### 1. Identify the course jadwal(s)

Students are often mixed across prodi (TI and SI) from the same campus. The same lecturer may teach parallel courses with different names:

- TI course codes: `MITI.xxx`, SI course codes: `MISI.xxx`
- Known pairs (Sentul 2025):
  - MITI.101 "Dasar-dasar Pemrograman" / MISI.101 "Logika Pemrograman Bisnis"
  - MITI "Arsitektur & Organisasi Komputer" / MISI.103 "Infrastruktur Teknologi Informasi"

Find the jadwal by course code and class:

```sql
SELECT j.id, mk.nama_matakuliah, mk.kode_matakuliah, k.nama_kelas, p.nama_prodi
FROM jadwal j
INNER JOIN matakuliah_kurikulum mkk ON j.id_matakuliah_kurikulum = mkk.id
INNER JOIN matakuliah mk ON mkk.id_matakuliah = mk.id
INNER JOIN kelas k ON j.id_kelas = k.id
INNER JOIN prodi p ON j.id_prodi = p.id
WHERE mk.kode_matakuliah IN ('<MITI.xxx>', '<MISI.xxx>')
AND j.status = 'AKTIF'
AND k.nama_kelas LIKE '%Sentul%'
AND j.id_tahun_akademik = (SELECT id FROM tahun_akademik WHERE status = 'AKTIF');
```

### 2. Find UTS/UAS JadwalTugas

```sql
SELECT jt.id, jt.nama_tugas, jt.kategori_tugas, sk.id_jadwal
FROM jadwal_tugas jt
INNER JOIN sesi_kuliah sk ON jt.id_sesi_kuliah = sk.id
WHERE sk.id_jadwal IN ('<ti_jadwal_id>', '<si_jadwal_id>')
AND jt.status = 'AKTIF' AND jt.kategori_tugas = '<UTS|UAS>';
```

### 3. Match students to jadwal

List enrolled students from both jadwal:

```sql
SELECT m.id, m.nim, m.nama, kd.id_jadwal
FROM krs_detail kd
INNER JOIN mahasiswa m ON kd.id_mahasiswa = m.id
WHERE kd.id_jadwal IN ('<ti_jadwal_id>', '<si_jadwal_id>')
AND kd.status = 'AKTIF'
ORDER BY kd.id_jadwal, m.nama;
```

Match submitted names to enrolled students:
- Partial name match is often needed (e.g. "Mufidah" → "Zahrotul Mufidah Salim")
- TI students typically have NIM starting with `25155`, SI with `25157`
- Report which enrolled students did NOT submit

### 4. Insert jawaban records

```sql
INSERT INTO jadwal_tugas_jawaban (id, id_mahasiswa, id_jadwal_tugas, jawaban_teks, status, waktu_pengumpulan)
VALUES (UUID(), '<mahasiswa_id>', '<jadwal_tugas_id>', '<link>', 'AKTIF', NOW());
```

Batch multiple students in a single INSERT for efficiency.

### 5. Set nilai from krs_nilai_tugas

```sql
UPDATE jadwal_tugas_jawaban jj
INNER JOIN mahasiswa m ON jj.id_mahasiswa = m.id
INNER JOIN krs_detail kd ON kd.id_mahasiswa = m.id
  AND kd.id_jadwal = '<jadwal_id>' AND kd.status = 'AKTIF'
INNER JOIN krs_nilai_tugas nt ON nt.id_krs_detail = kd.id AND nt.status = 'AKTIF'
INNER JOIN jadwal_bobot_tugas bt ON nt.id_bobot_tugas = bt.id
  AND bt.status = 'AKTIF' AND bt.kategori_tugas = '<UTS|UAS>'
SET jj.nilai = nt.nilai
WHERE jj.id_jadwal_tugas = '<jadwal_tugas_id>' AND jj.status = 'AKTIF';
```

### 6. Recalculate final scores

After updating `krs_nilai_tugas.nilai`, recalculate weighted scores and `krs_detail` totals. This mirrors `ScoreService.reCalculatingNilai()` — run once per jadwal.

**Step 1: Recalculate `krs_nilai_tugas.nilai_akhir`** (weighted = `bobot * nilai / 100`):

```sql
UPDATE krs_nilai_tugas AS a,
(SELECT d.id AS id_krs_nilai_tugas, ROUND((c.bobot * d.nilai) / 100, 2) AS nilai_tugas
 FROM krs_detail AS a
 INNER JOIN jadwal AS b ON a.id_jadwal = b.id AND b.id = '<jadwal_id>' AND a.status = 'AKTIF'
 INNER JOIN jadwal_bobot_tugas AS c ON c.id_jadwal = b.id AND c.status = 'AKTIF'
 INNER JOIN krs_nilai_tugas AS d ON c.id = d.id_bobot_tugas AND d.id_krs_detail = a.id AND d.status = 'AKTIF') AS b
SET a.nilai_akhir = b.nilai_tugas
WHERE a.id = b.id_krs_nilai_tugas;
```

**Step 2: Recalculate `krs_detail` totals** (nilai_akhir, grade, bobot):

```sql
UPDATE krs_detail AS a,
(SELECT id, nilai_presensi, nilai_tugas, nilai_uts, nilai_uas, nilai_akhir,
  IF(nilai_akhir >= 85,'A', IF(nilai_akhir >= 80,'A-', IF(nilai_akhir >= 75,'B+',
  IF(nilai_akhir >= 70,'B', IF(nilai_akhir >= 65,'B-', IF(nilai_akhir >= 60,'C+',
  IF(nilai_akhir >= 55,'C', IF(nilai_akhir >= 50,'D','E')))))))) AS grade,
  IF(nilai_akhir >= 85,4.00, IF(nilai_akhir >= 80,3.70, IF(nilai_akhir >= 75,3.30,
  IF(nilai_akhir >= 70,3.00, IF(nilai_akhir >= 65,2.70, IF(nilai_akhir >= 60,2.30,
  IF(nilai_akhir >= 55,2.00, IF(nilai_akhir >= 50,1.00,0.00)))))))) AS bobot
 FROM
  (SELECT id,
    ROUND(COALESCE(nilai_presensi,0),2) AS nilai_presensi,
    ROUND(COALESCE(nilai_tugas,0),2) AS nilai_tugas,
    ROUND(COALESCE(nilai_uts,0),2) AS nilai_uts,
    ROUND(nilai_uas,2) AS nilai_uas,
    ROUND(COALESCE(nilai_tugas,0) + COALESCE(nilai_uts,0) + COALESCE(nilai_uas,0) + COALESCE(nilai_presensi,0),2) AS nilai_akhir
   FROM
    (SELECT a.id,
      SUM(IF(c.kategori_tugas = 'TUGAS', (c.bobot * d.nilai) / 100, 0)) AS nilai_tugas,
      SUM(IF(c.kategori_tugas = 'UTS', (c.bobot * d.nilai) / 100, 0)) AS nilai_uts,
      SUM(IF(c.kategori_tugas = 'UAS', (c.bobot * d.nilai) / 100, 0)) AS nilai_uas,
      (x.nilai_presensi * b.bobot_presensi) / 100 AS nilai_presensi
     FROM krs_detail AS a
     INNER JOIN jadwal AS b ON a.id_jadwal = b.id AND b.id = '<jadwal_id>' AND a.status = 'AKTIF'
     INNER JOIN jadwal_bobot_tugas AS c ON c.id_jadwal = b.id AND c.status = 'AKTIF'
     INNER JOIN krs_nilai_tugas AS d ON c.id = d.id_bobot_tugas AND d.id_krs_detail = a.id AND d.status = 'AKTIF'
     LEFT JOIN
      (SELECT b.id AS id_krs_detail, presdos, presensi, (presensi / presdos) * 100 AS nilai_presensi
       FROM
        (SELECT id_jadwal, COUNT(a.id) AS presdos FROM presensi_dosen AS a
         WHERE id_jadwal = '<jadwal_id>' AND status_presensi <> 'MANGKIR' AND status = 'AKTIF') AS a
       LEFT JOIN
        (SELECT COUNT(a.id) AS presensi, a.id, a.id_jadwal FROM krs_detail AS a
         INNER JOIN presensi_mahasiswa AS b ON b.id_krs_detail = a.id
           AND a.id_jadwal = '<jadwal_id>' AND a.status = 'AKTIF' AND b.status = 'AKTIF'
           AND b.status_presensi NOT IN ('MANGKIR')
         GROUP BY a.id) AS b
       ON a.id_jadwal = b.id_jadwal) AS x
     ON a.id = x.id_krs_detail
     GROUP BY a.id) AS a) AS a) AS b
SET a.nilai_presensi = b.nilai_presensi, a.nilai_tugas = b.nilai_tugas,
    a.nilai_uts = b.nilai_uts, a.nilai_uas = b.nilai_uas,
    a.nilai_akhir = b.nilai_akhir, a.grade = b.grade, a.bobot = b.bobot
WHERE a.id = b.id AND a.id_jadwal = '<jadwal_id>';
```

Replace `<jadwal_id>` with the jadwal ID (not jadwal_tugas). Run both queries for each affected jadwal.

## Gotchas

- **kategori_tugas NULL**: `krs_nilai_tugas.kategori_tugas` may be NULL (especially for SI entries). Always join on `jadwal_bobot_tugas.kategori_tugas` instead.
- **nilai column**: `jadwal_tugas_jawaban.nilai` should match `krs_nilai_tugas.nilai` (raw score 0-100), not `nilai_akhir` (weighted score).
- **Mixed prodi**: A single submission list from the lecturer may contain students from different prodi/jadwal. Split them into the correct jadwalTugas per prodi.
