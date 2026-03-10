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

## Gotchas

- **kategori_tugas NULL**: `krs_nilai_tugas.kategori_tugas` may be NULL (especially for SI entries). Always join on `jadwal_bobot_tugas.kategori_tugas` instead.
- **nilai column**: `jadwal_tugas_jawaban.nilai` should match `krs_nilai_tugas.nilai` (raw score 0-100), not `nilai_akhir` (weighted score).
- **Mixed prodi**: A single submission list from the lecturer may contain students from different prodi/jadwal. Split them into the correct jadwalTugas per prodi.
