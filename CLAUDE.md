# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Build & Run

```bash
mvn clean package                    # build JAR
mvn spring-boot:run                  # run locally
mvn test                             # run all tests
mvn test -Dtest=ClassName            # run single test
```

Java 11, Spring Boot 2.7.18, Maven. Produces executable JAR.

## Production

- Server: `smile.stmik.tazkia.ac.id` (103.150.226.183), systemd service `aplikasi-akademik`
- App config: `/var/lib/aplikasi-akademik/application.properties` (profile: production)
- Database: MariaDB `db_smile_stmik`, uploads at `/var/lib/aplikasi-akademik/upload/`
- DB helper scripts in `tools/` folder (see `tools/README.md`), credentials in gitignored `tools/db-credentials.sh`
- Always do full gzipped backup before any production data changes

## Architecture

Spring Boot app managing academic lifecycle (SMILE - Sistem Informasi Manajemen Akademik). Package: `id.ac.tazkia.smilemahasiswa`.

**Layering**: Controller → Service → DAO (Spring Data JPA) → Entity → MariaDB

**Key patterns**:
- UUID primary keys (`@GenericGenerator(strategy = "uuid2")`)
- Soft deletes via `StatusRecord` enum (AKTIF/NONAKTIF/HAPUS) — queries filter by status
- Audit trail: `createdBy`, `updatedBy`, `createdAt`, `updatedAt` via JPA auditing
- Lombok `@Data` on entities and DTOs
- Thymeleaf templates with layout dialect, security dialect, and Spring Data pagination dialect

**Authentication**: Google OAuth2 for web users, JWT for API clients (`/api/**`). Role-based access with authorities like `VIEW_SUPERUSER`, `VIEW_AKADEMIK1`, `VIEW_DOSEN`, `VIEW_MAHASISWA`, etc.

**Async**: Kafka for billing/payment messaging (`KafkaSender`, `KafkaListenerService`). `@EnableScheduling` for cron jobs. `@EnableAsync` for background tasks.

**External integrations**: Feeder PDDIKTI (national education database sync), Gmail API (notifications), accounting system (via Kafka).

## Core Domain Relationships

```
Prodi → Kurikulum → MatakuliahKurikulum → Matakuliah
                                        ↓
Mahasiswa → KRS → KrsDetail ←→ MatakuliahKurikulum
                             ↓
                           Jadwal → SesiKuliah (16 per jadwal, has rps field)
                             ↓
                     Dosen (pengampu), Kelas, Hari
```

- `Jadwal` is the central scheduling entity linking course, lecturer, class, and time
- `SesiKuliah.rps` must be non-null for attendance to work
- `KrsDetail` links students to jadwal (enrollment)
- `RiwayatStatusMahasiswa` tracks student status changes historically

## Database

- Flyway migrations in `src/main/resources/db/migration/`
- `ddl-auto=none` — schema changes only via migrations
- Key prodi IDs: SI `621c0232-c1db-4aee-8993-815fdd950412`, TI `9f64c400-5850-4d18-a039-d2f2807899c8`

## Runbooks

Step-by-step procedures for common production data operations in `runbooks/`:
- `recording-submissions.md` — record student assignment submissions (external links) and set scores
- `recording-sessions.md` — record lecture sessions and student attendance
- `exporting-course-grades.md` — export per-course grades (not IPK) to xlsx per angkatan/prodi

## CI/CD

GitLab CI (`.gitlab-ci.yml`): Maven build with MySQL 5 service container. Deploy on `*-RELEASE` tags via SSH.
