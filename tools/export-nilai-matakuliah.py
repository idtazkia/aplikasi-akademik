#!/usr/bin/env python3
"""Export per-course grades to xlsx, one sheet per angkatan+prodi.

Usage:  ./tools/export-nilai-matakuliah.py 2024 2025 [-o output.xlsx]

Requires openpyxl. See runbooks/exporting-course-grades.md for the venv setup
and for what the output means.
"""
import argparse
import csv
import io
import subprocess
import sys
from collections import defaultdict, OrderedDict
from pathlib import Path

from openpyxl import Workbook
from openpyxl.styles import Font, Alignment, Border, Side, PatternFill
from openpyxl.utils import get_column_letter

DB_QUERY = Path(__file__).parent / "db-query.sh"
PRODI_SHORT = {"Sistem Informasi": "SI", "Teknik Informatika": "TI"}

FIXED = ["No", "NIM", "Nama", "Status"]


def query(sql):
    r = subprocess.run([str(DB_QUERY), sql], capture_output=True, text=True)
    if r.returncode != 0:
        sys.exit(f"db-query.sh failed:\n{r.stderr or r.stdout}")
    rows = list(csv.DictReader(io.StringIO(r.stdout), delimiter="\t"))
    if not rows:
        sys.exit(f"query returned no rows:\n{sql}")
    return rows


def fetch(angkatan):
    lst = ",".join(f"'{a}'" for a in angkatan)
    roster = query(
        "SELECT m.angkatan, p.nama_prodi, m.nim, m.nama, m.status_aktif "
        "FROM mahasiswa m JOIN prodi p ON m.id_prodi = p.id "
        f"WHERE m.angkatan IN ({lst}) "
        "ORDER BY m.angkatan, p.nama_prodi, m.nim"
    )
    # konversi rows are included on purpose: for angkatan 2024 they carry the
    # migrated semester-1 grades, and excluding them empties that semester
    nilai = query(
        "SELECT m.angkatan, p.nama_prodi, m.nim, mk.semester, mt.kode_matakuliah, "
        "mt.nama_matakuliah, kd.nilai_akhir, kd.grade "
        "FROM krs_detail kd "
        "JOIN mahasiswa m ON kd.id_mahasiswa = m.id "
        "JOIN prodi p ON m.id_prodi = p.id "
        "JOIN matakuliah_kurikulum mk ON kd.id_matakuliah_kurikulum = mk.id "
        "JOIN matakuliah mt ON mk.id_matakuliah = mt.id "
        f"WHERE m.angkatan IN ({lst}) AND kd.status = 'AKTIF' "
        "ORDER BY m.angkatan, p.nama_prodi, m.nim, mk.semester, mt.kode_matakuliah"
    )
    return roster, nilai


def build(roster, nilai, out):
    students = defaultdict(OrderedDict)               # cohort -> nim -> (nama, status)
    courses = defaultdict(lambda: defaultdict(dict))  # cohort -> semester -> kode -> nama
    grades = defaultdict(dict)                        # cohort -> (nim, sem, kode) -> (nilai, grade)

    for r in roster:
        students[(r["angkatan"], r["nama_prodi"])][r["nim"]] = (r["nama"], r["status_aktif"])

    for r in nilai:
        ck = (r["angkatan"], r["nama_prodi"])
        sem = int(r["semester"])
        courses[ck][sem][r["kode_matakuliah"]] = r["nama_matakuliah"]
        g = (r["grade"] or "").strip()
        if g in ("", "NULL"):
            # empty grade means not yet marked; nilai_akhir is 0/NULL and is not a real score
            continue
        raw = (r["nilai_akhir"] or "").strip()
        grades[ck][(r["nim"], sem, r["kode_matakuliah"])] = (
            None if raw in ("", "NULL") else float(raw), g)

    thin = Side(style="thin", color="B0B0B0")
    border = Border(left=thin, right=thin, top=thin, bottom=thin)
    hdr_fill = PatternFill("solid", fgColor="DDEBF7")
    sem_fill = PatternFill("solid", fgColor="BDD7EE")
    center = Alignment(horizontal="center", vertical="center", wrap_text=True)
    mid = Alignment(horizontal="center")

    wb = Workbook()
    wb.remove(wb.active)
    stats = []

    for ck in sorted(students.keys()):
        angkatan, prodi = ck
        ws = wb.create_sheet(f"{PRODI_SHORT.get(prodi, prodi[:3])} {angkatan}")

        flat = [(s, k, courses[ck][s][k])
                for s in sorted(courses[ck])
                for k in sorted(courses[ck][s])]

        ws.cell(row=1, column=1,
                value=f"Nilai Matakuliah - {prodi} Angkatan {angkatan}").font = Font(bold=True, size=13)

        for i, h in enumerate(FIXED, start=1):
            c = ws.cell(row=2, column=i, value=h)
            c.font = Font(bold=True); c.alignment = center; c.fill = hdr_fill
            ws.merge_cells(start_row=2, start_column=i, end_row=4, end_column=i)

        col = len(FIXED) + 1
        for s in sorted(courses[ck]):
            n = len(courses[ck][s])
            c = ws.cell(row=2, column=col, value=f"Semester {s}")
            c.font = Font(bold=True); c.alignment = center; c.fill = sem_fill
            ws.merge_cells(start_row=2, start_column=col, end_row=2, end_column=col + n * 2 - 1)
            col += n * 2

        col = len(FIXED) + 1
        for (_s, kode, nama) in flat:
            c = ws.cell(row=3, column=col, value=f"{kode}\n{nama}")
            c.font = Font(bold=True, size=9); c.alignment = center; c.fill = hdr_fill
            ws.merge_cells(start_row=3, start_column=col, end_row=3, end_column=col + 1)
            for j, sub in enumerate(("Nilai", "Grade")):
                sc = ws.cell(row=4, column=col + j, value=sub)
                sc.font = Font(bold=True, size=9); sc.alignment = center; sc.fill = hdr_fill
            col += 2

        r = 5
        filled = 0
        for i, (nim, (nama, status)) in enumerate(students[ck].items(), start=1):
            ws.cell(row=r, column=1, value=i).alignment = mid
            ws.cell(row=r, column=2, value=nim)
            ws.cell(row=r, column=3, value=nama)
            ws.cell(row=r, column=4,
                    value="" if status == "AKTIF" else status.replace("_", " ").title())
            col = len(FIXED) + 1
            for (s, kode, _n) in flat:
                hit = grades[ck].get((nim, s, kode))
                if hit is not None:
                    nc = ws.cell(row=r, column=col, value=hit[0])
                    nc.number_format = "0.00"; nc.alignment = mid
                    ws.cell(row=r, column=col + 1, value=hit[1]).alignment = mid
                    filled += 1
                col += 2
            r += 1

        last_col = len(FIXED) + len(flat) * 2
        for rr in range(2, r):
            for cc in range(1, last_col + 1):
                ws.cell(row=rr, column=cc).border = border

        for letter, w in (("A", 4), ("B", 14), ("C", 32), ("D", 16)):
            ws.column_dimensions[letter].width = w
        for cc in range(len(FIXED) + 1, last_col + 1):
            ws.column_dimensions[get_column_letter(cc)].width = 7.5
        ws.row_dimensions[3].height = 58
        ws.freeze_panes = ws.cell(row=5, column=len(FIXED) + 1)

        stats.append((ws.title, len(students[ck]), len(flat), filled,
                      len(students[ck]) * len(flat)))

    wb.save(out)
    return stats


def main():
    ap = argparse.ArgumentParser()
    ap.add_argument("angkatan", nargs="+")
    ap.add_argument("-o", "--output", default="nilai-matakuliah.xlsx")
    a = ap.parse_args()

    roster, nilai = fetch(a.angkatan)
    stats = build(roster, nilai, a.output)

    print(f"{'Sheet':<10} {'Mhs':>5} {'Matkul':>7} {'Terisi':>7} {'Sel':>7} {'%':>6}")
    for name, nm, nc, g, t in stats:
        print(f"{name:<10} {nm:>5} {nc:>7} {g:>7} {t:>7} {100 * g / t:>5.1f}%")
    print(f"\nSaved: {a.output}")


if __name__ == "__main__":
    main()
