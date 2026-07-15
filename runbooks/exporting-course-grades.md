# Exporting Per-Course Grades to Excel

Export per-course grades (not IPK) for one or more angkatan into an xlsx, one sheet per angkatan+prodi. Typical request: "data semua mahasiswa angkatan X, nilainya murni nilai matakuliah setiap pelajaran".

Read-only — no backup needed.

## Run

```bash
python3 -m venv /tmp/venv && /tmp/venv/bin/pip install openpyxl   # once; openpyxl is not in system python
/tmp/venv/bin/python tools/export-nilai-matakuliah.py 2024 2025 -o "Data Nilai Matakuliah.xlsx"
```

The script prints a per-sheet summary (students, courses, filled cells, fill %). Sanity-check the fill % before sending — see "Interpreting the output".

## Output layout

Four-tier header, data starts at row 5, panes frozen at the first grade column:

| Row | Content |
|-----|---------|
| 1 | Title (`Nilai Matakuliah - <prodi> Angkatan <angkatan>`) |
| 2 | `No` / `NIM` / `Nama` / `Status` (merged down to row 4), then `Semester N` merged across its courses |
| 3 | `<kode>\n<nama matakuliah>` merged across 2 columns |
| 4 | `Nilai` / `Grade` |

One sheet per angkatan+prodi (e.g. `SI 2024`, `TI 2025`). **Cohorts cannot be merged into one sheet** — course sets differ by both prodi and angkatan (2024 cohorts had ~33 courses across semesters 1-4; 2025 cohorts ~16 in semesters 1-2).

## Data rules

These are the decisions the script encodes. Re-confirm them if the request differs.

### Blank cell = not yet graded, NOT a zero

The single most important rule. A course with no grade entered is stored as `nilai_akhir = 0.00` with an empty `grade`. Exporting that 0 verbatim reads as a failing score.

The discriminator is `grade`, never `nilai_akhir`:

- `grade` non-empty → real result, export as-is. **This includes `nilai_akhir = 0` with `grade = 'E'`** — a genuine fail. (Checked 2026-07: all 114 zero-scored rows with a grade were `E`.)
- `grade` empty → not yet marked → leave both cells blank.

```sql
-- confirm the rule still holds before trusting a fresh export
SELECT kd.grade, COUNT(*) FROM krs_detail kd
JOIN mahasiswa m ON kd.id_mahasiswa = m.id
WHERE m.angkatan IN ('2024','2025') AND kd.status = 'AKTIF'
AND kd.nilai_akhir = 0 AND kd.grade IS NOT NULL AND kd.grade <> ''
GROUP BY kd.grade;   -- expect: E only
```

### Konversi rows are included

`krs_detail.status_konversi = 'AKTIF'` marks credit-transfer rows. Most other queries in this codebase exclude them — **this export must not**. For angkatan 2024 they hold the migrated semester-1 grades (122 of 143 SI rows, 159 of 179 TI rows, avg ~80). Excluding them empties semester 1 for that cohort.

### Semester = curriculum semester

The `Semester N` grouping is `matakuliah_kurikulum.semester` (the semester a course is *designed* for), not the student's term of study (`krs.semester`). This is the only reading where course columns are stable across students.

Consequence: a few students appear under a semester they haven't reached (e.g. 36 TI 2025 students carry a semester-3 course). If the requester actually wants term-of-study, the column structure has to be rebuilt — courses then vary per student and columns can't be fixed.

### Roster drives the rows

Students come from `mahasiswa`, not from `krs_detail`. Students with zero KRS rows (all `MENGUNDURKAN_DIRI` as of 2026-07) still get a row with blank grades, so the cohort list stays complete. `Status` is blank for AKTIF, otherwise spelled out.

## Interpreting the output

Fill % is expected to be well under 100 — roughly 60% in July 2026. Grades for the **most recently finished term are largely not yet entered**, so the highest semester on each sheet is mostly blank. Say this explicitly when handing the file over, or the gaps read as missing data.

Cross-check before sending:

```sql
-- total filled cells across all sheets must equal this
SELECT COUNT(*) FROM krs_detail kd
JOIN mahasiswa m ON kd.id_mahasiswa = m.id
WHERE m.angkatan IN ('2024','2025') AND kd.status = 'AKTIF'
AND kd.grade IS NOT NULL AND kd.grade <> '';
```

## Gotchas

- **openpyxl is not installed system-wide** and `pip3 install` fails with `externally-managed-environment` (PEP 668). Use a venv; do not pass `--break-system-packages`.
- **Students are not confined to their prodi's kurikulum.** SI students hold `krs_detail` rows under TI's kurikulum and vice versa (shared MKWU courses). Derive course columns from `krs_detail` as actually taken — never from `matakuliah_kurikulum` filtered by the prodi's kurikulum id.
- **`matakuliah` has no `sks` column** — it lives on `matakuliah_kurikulum`. `SELECT mt.sks` fails with `Unknown column`.
- **Letter grades use +/- variants** (`A-`, `B+`), so `grade` is not a single character.
- **`db-query.sh` breaks on backticks / `$` / nested double quotes.** The queries here are clean, but keep them that way; for anything with shell metacharacters use the SCP+stdin workaround.
- **No duplicate student-course pairs** exist today (verified 2026-07), so the pivot assumes one grade per (nim, semester, kode). If a repeat/retake is ever recorded, the pivot silently keeps the last row — re-check before trusting.

## Verifying a new export

Column misalignment across merged headers is the main risk. Read the workbook back and map columns from the tier-3 header itself, then compare against source rows — do not reuse the builder's column math. A pass looks like:

```
SI 2024: OK (464 nilai cocok persis)
...
Total nilai diperiksa: 1906 | mismatch: 0
```

## History

- 2026-07-15 — first request (Kaprodi, via Alhakim): angkatan 2024+2025, both prodi, nilai angka + huruf, mengundurkan-diri included with a status column. 146 students, 1906 grades. Template requested: `Data smile yang diminta.xlsx` (No/NIM/Nama + Semester 1-4 × matkul).
