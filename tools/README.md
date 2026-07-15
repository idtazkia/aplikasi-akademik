# Database Tools

Helper scripts for production database operations.

## Setup

Copy the credentials template and fill in your values:

```bash
cp tools/db-credentials.sh.example tools/db-credentials.sh
# Edit tools/db-credentials.sh with actual credentials
```

`db-credentials.sh` is gitignored and must not be committed.

## Scripts

### db-backup.sh
Full database backup with gzip. Saves to server home folder and local `backup/` directory.

```bash
./tools/db-backup.sh
```

### db-query.sh
Run a single SQL query against production.

```bash
./tools/db-query.sh "SELECT COUNT(*) FROM matakuliah_kurikulum"
```

### db-run-sql.sh
Execute a SQL file against production. Uploads the file to the server then runs it.

```bash
./tools/db-run-sql.sh backup/rps_update.sql
```

## Query Scripts

### find-lecturer.sh
Search for a lecturer by name.

```bash
./tools/find-lecturer.sh "Endy"
```

### find-courses-by-lecturer.sh
List all active courses (jadwal) for a lecturer.

```bash
./tools/find-courses-by-lecturer.sh "Endy Muhardin"
```

### find-courses-by-semester.sh
List courses in a curriculum, optionally filtered by semester.

```bash
./tools/find-courses-by-semester.sh 35a90e39-1a25-49e2-8cdb-2aa8df463ee0       # all semesters
./tools/find-courses-by-semester.sh 35a90e39-1a25-49e2-8cdb-2aa8df463ee0 4     # semester 4 only
```

### find-courses-by-prodi.sh
List kurikulum and course summary for a prodi (by ID or name).

```bash
./tools/find-courses-by-prodi.sh "Sistem Informasi"
./tools/find-courses-by-prodi.sh 621c0232-c1db-4aee-8993-815fdd950412
```

### find-rps-status.sh
Check RPS completion status for a lecturer's courses.

```bash
./tools/find-rps-status.sh "Endy Muhardin"
```

### find-students-by-course.sh
List students enrolled in a jadwal.

```bash
./tools/find-students-by-course.sh 4fef1c9f-3120-4b41-9b6e-178177aa4832
```

## Export Scripts

### export-nilai-matakuliah.py
Export per-course grades (not IPK) to xlsx, one sheet per angkatan+prodi. Needs `openpyxl`, which is not in system python — use a venv.

```bash
python3 -m venv /tmp/venv && /tmp/venv/bin/pip install openpyxl
/tmp/venv/bin/python tools/export-nilai-matakuliah.py 2024 2025 -o "Data Nilai Matakuliah.xlsx"
```

Blank cells mean "not yet graded", not zero. See `runbooks/exporting-course-grades.md` before sending the output to anyone.
