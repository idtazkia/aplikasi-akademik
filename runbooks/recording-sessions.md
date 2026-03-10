# Recording Session Data (Attendance)

Record lecture sessions: update sesi_kuliah, mark lecturer attendance, and insert student attendance records.

## Tables Involved

- `sesi_kuliah` — 16 pre-created rows per jadwal (one per session)
- `presensi_dosen` — lecturer attendance (one per session)
- `presensi_mahasiswa` — student attendance (one per student per session)

## Required Input

- Session number(s) (pertemuan ke berapa)
- Date conducted
- Berita acara (session topic/report)
- Jenis kegiatan: any combination of `ceramah`, `diskusi`, `presentasi`, `tanyajawab`, `kuis`, `praktikum`
- Student attendance (default all HADIR unless specified)

## Steps

### 1. Find the jadwal and sesi_kuliah

```sql
SELECT sk.id, sk.pertemuan_ke, sk.pertemuan, sk.berita_acara,
  pd.id as presensi_dosen_id, pd.status_presensi
FROM sesi_kuliah sk
LEFT JOIN presensi_dosen pd ON sk.id_presensi_dosen = pd.id
WHERE sk.id_jadwal = '<jadwal_id>'
AND sk.pertemuan_ke IN (<session_numbers>)
ORDER BY sk.pertemuan_ke;
```

### 2. Update sesi_kuliah

```sql
UPDATE sesi_kuliah SET
  berita_acara = '<topic>',
  pertemuan = '<session_number>',
  waktu_mulai = '<date> <start_time>',
  waktu_selesai = '<date> <end_time>',
  ceramah = 'AKTIF',      -- set to 'AKTIF' to check, NULL to uncheck
  diskusi = 'AKTIF',
  presentasi = NULL,
  tanyajawab = NULL,
  kuis = NULL,
  praktikum = NULL
WHERE id = '<sesi_kuliah_id>';
```

### 3. Update presensi_dosen

```sql
UPDATE presensi_dosen SET
  status = 'AKTIF',
  status_presensi = 'HADIR',
  waktu_selesai = '<date> <end_time>'
WHERE id = '<presensi_dosen_id>';
```

Note: `waktu_masuk` is the planned date (set during jadwal creation). Do NOT change it. `sesi_kuliah.waktu_mulai` is the realization date.

### 4. Get student list

```sql
SELECT kd.id as id_krs_detail, m.id as id_mahasiswa, m.nim, m.nama
FROM krs_detail kd
JOIN mahasiswa m ON kd.id_mahasiswa = m.id
WHERE kd.id_jadwal = '<jadwal_id>'
AND kd.status = 'AKTIF'
ORDER BY m.nim;
```

### 5. Insert presensi_mahasiswa

```sql
INSERT INTO presensi_mahasiswa
  (id, id_krs_detail, id_sesi_kuliah, id_mahasiswa, status_presensi, status, waktu_masuk, waktu_keluar)
SELECT UUID(), kd.id, '<sesi_kuliah_id>', m.id, 'HADIR', 'AKTIF',
  '<date> <start_time>', '<date> <end_time>'
FROM krs_detail kd
JOIN mahasiswa m ON kd.id_mahasiswa = m.id
WHERE kd.id_jadwal = '<jadwal_id>'
AND kd.status = 'AKTIF';
```

For students with different status (SAKIT, IZIN, ALPA, TERLAMBAT), update after the bulk insert:

```sql
UPDATE presensi_mahasiswa SET status_presensi = 'SAKIT'
WHERE id_sesi_kuliah = '<sesi_kuliah_id>'
AND id_mahasiswa = '<mahasiswa_id>';
```

## Gotchas

- **`sesi_kuliah.pertemuan`** (varchar): must be set to the session number string (e.g. "1", "2"). If left as default "Belum Mengajar", the UI shows a "BELUM MENGAJAR" badge.
- **Jenis kegiatan columns** (`ceramah`, `diskusi`, etc.) are `StatusRecord` enum. Use `'AKTIF'` to check, `NULL` to uncheck. Do NOT use `'on'` or `'true'` — causes `No enum constant StatusRecord.on` crash.
- **Planned vs Realization dates**: `presensi_dosen.waktu_masuk` = planned. `sesi_kuliah.waktu_mulai` = actual. They differ when rescheduled.
- **`sesi_kuliah.rps`**: must be non-null for the attendance page to work. If null, populate it first (see rps.md in memory).
- **Parallel classes**: Sentul campus has combined TI+SI. Process both jadwal when entering sessions.
