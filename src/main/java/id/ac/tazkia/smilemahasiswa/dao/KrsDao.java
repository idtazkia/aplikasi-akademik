package id.ac.tazkia.smilemahasiswa.dao;

import java.util.ArrayList;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.PagingAndSortingRepository;

import id.ac.tazkia.smilemahasiswa.dto.EnrollMahasiswaIntDto;
import id.ac.tazkia.smilemahasiswa.dto.elearning.StatusImportNilaiDosenIntDto;
import id.ac.tazkia.smilemahasiswa.dto.tlc.jadwal.MahasiswaDto;
import id.ac.tazkia.smilemahasiswa.dto.user.IpkDto;
import id.ac.tazkia.smilemahasiswa.entity.Jadwal;
import id.ac.tazkia.smilemahasiswa.entity.JenisKelamin;
import id.ac.tazkia.smilemahasiswa.entity.Krs;
import id.ac.tazkia.smilemahasiswa.entity.Mahasiswa;
import id.ac.tazkia.smilemahasiswa.entity.Prodi;
import id.ac.tazkia.smilemahasiswa.entity.Program;
import id.ac.tazkia.smilemahasiswa.entity.StatusRecord;
import id.ac.tazkia.smilemahasiswa.entity.TahunAkademik;

public interface KrsDao extends PagingAndSortingRepository<Krs, String> {
        Krs findByMahasiswaAndTahunAkademikAndStatus(Mahasiswa mahasiswa, TahunAkademik ta, StatusRecord s);

        Long countKrsByTahunAkademikAndMahasiswaStatus(TahunAkademik tahunAkademik, StatusRecord aktif);

        Long countKrsByTahunAkademikAndMahasiswaJenisKelamin(TahunAkademik tahunAkademik, JenisKelamin pria);

        List<Krs> findByTahunAkademikAndStatus(TahunAkademik tahunAkademik, StatusRecord statusRecord);

        Page<Krs> findByTahunAkademikAndProdiAndMahasiswaIdProgramAndMahasiswaAngkatan(TahunAkademik tahunAkademik,
                        Prodi prodi, Program program, String angkatan, Pageable pageable);

        Krs findByTahunAkademikAndMahasiswaAndStatus(TahunAkademik tahunAkademik, Mahasiswa mhsw, StatusRecord aktif);

        @Query(value = "select substr(ta.nama_tahun_akademik, 1, 4) as tahun from krs k inner join tahun_akademik ta on k.id_tahun_akademik=ta.id where id_mahasiswa=?1 and k.status='AKTIF' order by ta.kode_tahun_akademik asc limit 1", nativeQuery = true)
        String masukMahasiswa(Mahasiswa mhs);

        Page<Krs> findByProdiAndTahunAkademikAndStatus(Prodi prodi, TahunAkademik tahunAkademik,
                        StatusRecord statusRecord, Pageable page);

        @Query(value = "SELECT a.* FROM (SELECT a.id AS id_krs,b.nim,b.nama,c.nama_prodi FROM krs AS a INNER JOIN mahasiswa AS b ON a.id_mahasiswa=b.id INNER JOIN prodi AS c ON b.id_prodi=c.id WHERE a.id_tahun_akademik=?1 AND a.status='AKTIF' GROUP BY a.id_mahasiswa)a LEFT JOIN (SELECT * FROM krs_detail WHERE id_tahun_akademik=?1 AND STATUS='AKTIF' AND id_jadwal=?2 GROUP BY id_mahasiswa)b ON a.id_krs=b.id_krs WHERE b.id IS NULL", nativeQuery = true)
        List<Object[]> krsList(TahunAkademik tahun, Jadwal jadwal);

        @Query(value = "select a.id, b.kode_tahun_akademik, b.nama_tahun_akademik,b.jenis from krs as a \n" +
                        "inner join krs_detail as g on a.id = g.id_krs\n" +
                        "inner join jadwal as h on g.id_jadwal = h.id\n" +
                        "inner join matakuliah_kurikulum as i on h.id_matakuliah_kurikulum = i.id\n" +
                        "inner join tahun_akademik as b on a.id_tahun_akademik = b.id\n" +
                        "where a.status = 'AKTIF' and g.status='AKTIF' and i.jumlah_sks > 0 and a.id_mahasiswa = ?1 group by a.id \n"
                        +
                        "order by b.kode_tahun_akademik", nativeQuery = true)
        List<Object[]> semesterTranskript1(String idMahasiswa);

        @Query(value = "(SELECT a.id, b.kode_tahun_akademik, b.nama_tahun_akademik, b.jenis, " +
                        "CASE WHEN b.jenis = 'PENDEK' THEN 0 " +
                        "ELSE (SELECT COUNT(*) FROM tahun_akademik ta " +
                        "WHERE ta.jenis != 'PENDEK' AND ta.kode_tahun_akademik <= b.kode_tahun_akademik " +
                        "AND EXISTS (SELECT 1 FROM krs k2 INNER JOIN krs_detail kd2 ON k2.id = kd2.id_krs " +
                        "INNER JOIN jadwal j2 ON kd2.id_jadwal = j2.id " +
                        "INNER JOIN matakuliah_kurikulum mk2 ON j2.id_matakuliah_kurikulum = mk2.id " +
                        "WHERE k2.id_mahasiswa = ?1 AND k2.status = 'AKTIF' AND kd2.status = 'AKTIF' AND mk2.jumlah_sks > 0 "
                        +
                        "AND k2.id_tahun_akademik = ta.id)) " +
                        "END AS row_num \n" +
                        "FROM krs AS a \n" +
                        "INNER JOIN krs_detail AS g ON a.id = g.id_krs \n" +
                        "INNER JOIN jadwal AS h ON g.id_jadwal = h.id \n" +
                        "INNER JOIN matakuliah_kurikulum AS i ON h.id_matakuliah_kurikulum = i.id \n" +
                        "INNER JOIN tahun_akademik AS b ON a.id_tahun_akademik = b.id \n" +
                        "WHERE a.status = 'AKTIF' AND g.status='AKTIF' AND i.jumlah_sks > 0 AND a.id_mahasiswa = ?1 GROUP BY a.id \n"
                        +
                        "ORDER BY b.kode_tahun_akademik)", nativeQuery = true)
        List<Object[]> semesterTranskript(String idMahasiswa);

        @Query(value = "(SELECT a.id, b.kode_tahun_akademik, b.nama_tahun_akademik, b.jenis, " +
                        "CASE WHEN EXISTS (" +
                        "    SELECT 1 FROM krs_detail kd2 " +
                        "    INNER JOIN jadwal j2 ON kd2.id_jadwal = j2.id " +
                        "    INNER JOIN jadwal_dosen jd2 ON j2.id = jd2.id_jadwal " +
                        "    WHERE kd2.id_krs = a.id " +
                        "    AND kd2.status = 'AKTIF' " +
                        "    AND kd2.status_edom = 'UNDONE' " +
                        "    AND j2.id_hari IS NOT NULL " +
                        "    AND jd2.status_jadwal_dosen IN ('PENGAMPU', 'TEAM') " +
                        "    AND EXISTS (SELECT 1 FROM presensi_dosen pd WHERE pd.id_jadwal = j2.id AND pd.id_dosen = jd2.id_dosen AND pd.status = 'AKTIF')"
                        +
                        ") THEN 'UNDONE' ELSE 'DONE' END AS status_edom, " +
                        "b.id as tahunId, " +
                        "CASE WHEN b.jenis = 'PENDEK' THEN 0 " +
                        "ELSE (SELECT COUNT(*) FROM tahun_akademik ta " +
                        "WHERE ta.jenis != 'PENDEK' AND ta.kode_tahun_akademik <= b.kode_tahun_akademik " +
                        "AND EXISTS (SELECT 1 FROM krs k2 INNER JOIN krs_detail kd2 ON k2.id = kd2.id_krs " +
                        "INNER JOIN jadwal j2 ON kd2.id_jadwal = j2.id " +
                        "INNER JOIN matakuliah_kurikulum mk2 ON j2.id_matakuliah_kurikulum = mk2.id " +
                        "WHERE k2.id_mahasiswa = ?1 AND k2.status = 'AKTIF' AND kd2.status = 'AKTIF' AND mk2.jumlah_sks > 0 "
                        +
                        "AND k2.id_tahun_akademik = ta.id)) " +
                        "END AS nomor_semester \n" +
                        " FROM krs AS a \n" +
                        "INNER JOIN krs_detail AS g ON a.id = g.id_krs \n" +
                        "INNER JOIN jadwal AS h ON g.id_jadwal = h.id \n" +
                        "INNER JOIN matakuliah_kurikulum AS i ON h.id_matakuliah_kurikulum = i.id \n" +
                        "INNER JOIN tahun_akademik AS b ON a.id_tahun_akademik = b.id \n" +
                        "WHERE a.status = 'AKTIF' AND g.status='AKTIF' AND i.jumlah_sks > 0 AND a.id_mahasiswa = ?1  GROUP BY a.id \n"
                        +
                        "ORDER BY b.kode_tahun_akademik)", nativeQuery = true)
        List<Object[]> semesterTranskriptMahasiswa(String idMahasiswa);

        @Query(value = "SELECT COUNT(DISTINCT a.id) AS total_krs FROM krs AS a INNER JOIN tahun_akademik AS b ON a.id_tahun_akademik = b.id INNER JOIN krs_detail AS kd ON a.id = kd.id_krs WHERE a.status = 'AKTIF' AND a.id_mahasiswa = ?1 AND b.jenis != 'PENDEK'", nativeQuery = true)
        Integer countSemester(String id);

        @Query(value = "SELECT id_jadwal, kode_matakuliah, nama_matakuliah, nama_matakuliah_english, jumlah_sks,jam_mulai,jam_selesai, nama_hari, nama_karyawan, prasyarat, bobot_prasyarat, setara, ambil_sebelum,bobot_sebelum,ambil_prasyarat,bobot_ambil_prass, already FROM\n"
                        +
                        "(SELECT a.id AS id_jadwal, c.kode_matakuliah, c.nama_matakuliah, c.nama_matakuliah_english,z.nama_hari as nama_hari,j.nama_karyawan as nama_karyawan,b.jumlah_sks,jam_mulai,jam_selesai, prasyarat, e.nilai AS bobot_prasyarat, setara, g.nama_matakuliah AS ambil_sebelum, g.bobot AS bobot_sebelum, h.nama_matakuliah AS ambil_prasyarat, h.bobot AS bobot_ambil_prass, i.kode_matakuliah AS already FROM jadwal AS a\n"
                        +
                        "INNER JOIN matakuliah_kurikulum AS b ON a.id_matakuliah_kurikulum = b.id\n" +
                        "INNER JOIN matakuliah AS c ON b.id_matakuliah = c.id\n" +
                        "INNER JOIN kelas_mahasiswa AS d ON a.id_kelas = d.id_kelas\n" +
                        "INNER JOIN hari as z ON z.id = a.id_hari\n" +
                        "INNER JOIN karyawan as j ON j.id = a.id_dosen_pengampu\n" +
                        "LEFT JOIN \n" +
                        "\t(SELECT b.nama_matakuliah AS prasyarat,a.id_matakuliah,b.kode_matakuliah,a.nilai FROM prasyarat AS a INNER JOIN matakuliah AS b ON a.id_matakuliah_pras = b.id WHERE a.status = 'AKTIF') e ON c.id = e.id_matakuliah\n"
                        +
                        "LEFT JOIN \n" +
                        "\t(SELECT b.nama_matakuliah AS setara,a.id_matakuliah,b.kode_matakuliah FROM matakuliah_setara AS a INNER JOIN matakuliah AS b ON a.id_matakuliah_setara = b.id WHERE a.status ='AKTIF' ) f ON c.id = f.id_matakuliah\n"
                        +
                        "LEFT JOIN\n" +
                        "\t(SELECT d.id, d.kode_matakuliah, d.nama_matakuliah, d.nama_matakuliah_english, a.bobot FROM krs_detail AS a \n"
                        +
                        "\tINNER JOIN jadwal AS b ON a.id_jadwal = b.id\n" +
                        "\tINNER JOIN matakuliah_kurikulum AS c ON b.id_matakuliah_kurikulum = c.id\n" +
                        "\tINNER JOIN matakuliah AS d ON c.id_matakuliah = d.id\n" +
                        "\tWHERE a.status = 'AKTIF' AND a.id_tahun_akademik <> ?1 AND a.id_mahasiswa = ?2)g \n" +
                        "\tON c.id = g.id OR e.id_matakuliah = g.id\n" +
                        "LEFT JOIN\n" +
                        "\t(SELECT d.id,d.kode_matakuliah, d.nama_matakuliah, d.nama_matakuliah_english, a.bobot FROM krs_detail AS a \n"
                        +
                        "\tINNER JOIN jadwal AS b ON a.id_jadwal = b.id\n" +
                        "\tINNER JOIN matakuliah_kurikulum AS c ON b.id_matakuliah_kurikulum = c.id\n" +
                        "\tINNER JOIN matakuliah AS d ON c.id_matakuliah = d.id\n" +
                        "\tWHERE a.status = 'AKTIF' AND a.id_tahun_akademik <> ?1 AND a.id_mahasiswa = ?2)h \n" +
                        "\tON e.id_matakuliah = h.id OR e.kode_matakuliah = h.kode_matakuliah\n" +
                        "LEFT JOIN\n" +
                        "\t(SELECT d.id,d.kode_matakuliah, d.nama_matakuliah, d.nama_matakuliah_english, a.bobot FROM krs_detail AS a \n"
                        +
                        "\tINNER JOIN jadwal AS b ON a.id_jadwal = b.id\n" +
                        "\tINNER JOIN matakuliah_kurikulum AS c ON b.id_matakuliah_kurikulum = c.id\n" +
                        "\tINNER JOIN matakuliah AS d ON c.id_matakuliah = d.id\n" +
                        "\tWHERE a.status = 'AKTIF' AND a.id_tahun_akademik = ?1 AND a.id_mahasiswa = ?2)i\n" +
                        "\tON c.id = i.id OR c.kode_matakuliah = i.kode_matakuliah\n" +
                        "WHERE a.id_tahun_akademik = ?1 AND a.status = 'AKTIF' AND d.id_mahasiswa = ?2 AND b.jumlah_sks > 0 AND id_hari IS NOT NULL AND jam_mulai IS NOT NULL AND jam_selesai IS NOT NULL\n"
                        +
                        "GROUP BY a.id \n" +
                        "ORDER BY nama_matakuliah)a WHERE already IS NULL", nativeQuery = true)
        List<Object[]> listKrs(TahunAkademik ta, Mahasiswa mahasiswa);

        @Query(value = "SELECT * FROM\n" +
                        "(SELECT a.id AS idKrs, b.id AS idKrsDetail, b.id_jadwal as idJadwal, b.id_mahasiswa as idMahasiswa, COALESCE(e.username,c.email_tazkia) AS email, c.nim, d.id_number_elearning as idNumberElearning, b.status_enroll as statusEnroll\n"
                        +
                        "FROM krs AS a \n" +
                        "INNER JOIN krs_detail AS b ON a.id = b.id_krs\n" +
                        "INNER JOIN mahasiswa AS c ON b.id_mahasiswa = c.id\n" +
                        "INNER JOIN jadwal AS d ON b.id_jadwal = d.id\n" +
                        "INNER JOIN s_user AS e ON c.id_user = e.id\n" +
                        "INNER JOIN matakuliah_kurikulum AS f ON d.id_matakuliah_kurikulum = f.id\n" +
                        "WHERE a.id_tahun_akademik = 'f635cb68-3da5-4e11-8213-7020636034a2' AND a.status = 'AKTIF' AND b.status = 'AKTIF' AND b.status_enroll is NULL) mdl_krs_smile", nativeQuery = true)
        List<EnrollMahasiswaIntDto> findEnrollMahasiswa();

        @Query(value = "SELECT kr.nama_karyawan as dosen,jd.id_number_elearning as idNumberElearning,pr.nama_prodi as namaProdi ,mk.kode_matakuliah as kodeMatakuliah,kl.nama_kelas as namaKelas,mk.nama_matakuliah as namaMatakuliah,mk.nama_matakuliah_english as namaMatakuliahEnglish,\n"
                        +
                        "CASE WHEN min(nilai_tugas) > 0 THEN 'SUDAH' WHEN max(nilai_tugas) > 0 and min(nilai_tugas) = 0 THEN 'SUDAH (ada beberapa mahasiswa/i yang masih belum atau tidak dinilai, mohon dicek.)'\n"
                        +
                        "ELSE 'TUGAS BELUM' END AS statusImportTugas, CASE WHEN min(nilai_uts) > 0 THEN 'SUDAH' WHEN max(nilai_uts) > 0 and min(nilai_uts) = 0 THEN 'SUDAH (ada beberapa mahasiswa/i yang masih belum atau tidak dinilai, mohon dicek.)'\n"
                        +
                        "ELSE 'UTS BELUM' END AS statusImportUts, CASE WHEN min(nilai_uas) > 0 THEN 'SUDAH' WHEN max(nilai_uas) > 0 and min(nilai_uas) = 0 THEN 'SUDAH (ada beberapa mahasiswa/i yang masih belum atau tidak dinilai, mohon dicek.)'\n"
                        +
                        "ELSE 'UAS BELUM' END AS statusImportUas FROM krs_detail as kd inner join matakuliah_kurikulum as mku on kd.id_matakuliah_kurikulum = mku.id inner join matakuliah as mk on mku.id_matakuliah = mk.id inner join mahasiswa as ma on kd.id_mahasiswa = ma.id inner join tahun_akademik as ta on kd.id_tahun_akademik = ta.id inner join jadwal as jd on kd.id_jadwal = jd.id inner join kelas as kl on jd.id_kelas = kl.id inner join prodi as pr on jd.id_prodi = pr.id inner join dosen as ds on jd.id_dosen_pengampu = ds.id inner join karyawan as kr on ds.id_karyawan = kr.id left join (SELECT kd.id, pm.id as id_presensi_mahasiswa, jd.id_tahun_akademik, pm.id_krs_detail,pm.id_mahasiswa as idMahasiswa,ma.nim as nim,ma.email_tazkia as email, pm.waktu_masuk, pm.waktu_keluar,pm.status_presensi, pm.status, jd.id as idJadwal,jd.id_number_elearning as idNumberElearning, jd.jam_mulai, jd.jam_selesai,count(pm.id) as absensi\n"
                        +
                        "FROM krs_detail as kd left join presensi_mahasiswa as pm on kd.id = pm.id_krs_detail inner join sesi_kuliah as sk on pm.id_sesi_kuliah = sk.id inner join jadwal as jd on sk.id_jadwal = jd.id inner join mahasiswa as ma on pm.id_mahasiswa = ma.id where jd.id_tahun_akademik = ?1 and pm.status_presensi IN ('MANGKIR','TERLAMBAT') and pm.status = 'AKTIF' and kd.status = 'AKTIF'\n"
                        +
                        "group by pm.id_mahasiswa, jd.id having count(pm.id) > 3 order by count(pm.id) DESC) as excludeMangkir on kd.id = excludeMangkir.id_krs_detail where kd.id_tahun_akademik = ?1 and excludeMangkir.id_krs_detail is NULL group by jd.id_number_elearning order by jd.id_number_elearning DESC\n", countQuery = "SELECT count(kd.id) as jml FROM krs_detail as kd\n"
                                        +
                                        "inner join matakuliah_kurikulum as mku on kd.id_matakuliah_kurikulum = mku.id\n"
                                        +
                                        "inner join matakuliah as mk on mku.id_matakuliah = mk.id inner join mahasiswa as ma on kd.id_mahasiswa = ma.id inner join tahun_akademik as ta on kd.id_tahun_akademik = ta.id inner join jadwal as jd on kd.id_jadwal = jd.id inner join kelas as kl on jd.id_kelas = kl.id inner join prodi as pr on jd.id_prodi = pr.id inner join dosen as ds on jd.id_dosen_pengampu = ds.id inner join karyawan as kr on ds.id_karyawan = kr.id left join (SELECT kd.id, pm.id as id_presensi_mahasiswa, jd.id_tahun_akademik, pm.id_krs_detail,pm.id_mahasiswa as idMahasiswa,ma.nim as nim,ma.email_tazkia as email, pm.waktu_masuk, pm.waktu_keluar,pm.status_presensi, pm.status, jd.id as idJadwal,jd.id_number_elearning as idNumberElearning, jd.jam_mulai, jd.jam_selesai,count(pm.id) as absensi\n"
                                        +
                                        "FROM krs_detail as kd left join presensi_mahasiswa as pm on kd.id = pm.id_krs_detail inner join sesi_kuliah as sk on pm.id_sesi_kuliah = sk.id inner join jadwal as jd on sk.id_jadwal = jd.id inner join mahasiswa as ma on pm.id_mahasiswa = ma.id where jd.id_tahun_akademik = ?1 and pm.status_presensi IN ('MANGKIR','TERLAMBAT') and pm.status = 'AKTIF' and kd.status = 'AKTIF'\n"
                                        +
                                        "group by pm.id_mahasiswa, jd.id having count(pm.id) > 3 order by count(pm.id) DESC) as excludeMangkir on kd.id = excludeMangkir.id_krs_detail\n"
                                        +
                                        "where kd.id_tahun_akademik = ?1  and excludeMangkir.id_krs_detail is NULL group by jd.id_number_elearning order by jd.id_number_elearning DESC", nativeQuery = true)
        Page<StatusImportNilaiDosenIntDto> findStatusImportNilaiDosen(TahunAkademik ta, Pageable page);

        @Query(value = "SELECT kr.nama_karyawan as dosen,jd.id_number_elearning as idNumberElearning,pr.nama_prodi as namaProdi ,mk.kode_matakuliah as kodeMatakuliah,kl.nama_kelas as namaKelas,mk.nama_matakuliah as namaMatakuliah,mk.nama_matakuliah_english as namaMatakuliahEnglish,\n"
                        +
                        "CASE WHEN min(nilai_tugas) > 0 THEN 'SUDAH' WHEN max(nilai_tugas) > 0 and min(nilai_tugas) = 0 THEN 'SUDAH (ada beberapa mahasiswa/i yang masih belum atau tidak dinilai, mohon dicek.)'\n"
                        +
                        "ELSE 'TUGAS BELUM' END AS statusImportTugas, CASE WHEN min(nilai_uts) > 0 THEN 'SUDAH' WHEN max(nilai_uts) > 0 and min(nilai_uts) = 0 THEN 'SUDAH (ada beberapa mahasiswa/i yang masih belum atau tidak dinilai, mohon dicek.)'\n"
                        +
                        "ELSE 'UTS BELUM' END AS statusImportUts, CASE WHEN min(nilai_uas) > 0 THEN 'SUDAH' WHEN max(nilai_uas) > 0 and min(nilai_uas) = 0 THEN 'SUDAH (ada beberapa mahasiswa/i yang masih belum atau tidak dinilai, mohon dicek.)'\n"
                        +
                        "ELSE 'UAS BELUM' END AS statusImportUas FROM krs_detail as kd inner join matakuliah_kurikulum as mku on kd.id_matakuliah_kurikulum = mku.id inner join matakuliah as mk on mku.id_matakuliah = mk.id inner join mahasiswa as ma on kd.id_mahasiswa = ma.id inner join tahun_akademik as ta on kd.id_tahun_akademik = ta.id inner join jadwal as jd on kd.id_jadwal = jd.id inner join kelas as kl on jd.id_kelas = kl.id inner join prodi as pr on jd.id_prodi = pr.id inner join dosen as ds on jd.id_dosen_pengampu = ds.id inner join karyawan as kr on ds.id_karyawan = kr.id left join (SELECT kd.id, pm.id as id_presensi_mahasiswa, jd.id_tahun_akademik, pm.id_krs_detail,pm.id_mahasiswa as idMahasiswa,ma.nim as nim,ma.email_tazkia as email, pm.waktu_masuk, pm.waktu_keluar,pm.status_presensi, pm.status, jd.id as idJadwal,jd.id_number_elearning as idNumberElearning, jd.jam_mulai, jd.jam_selesai,count(pm.id) as absensi\n"
                        +
                        "FROM krs_detail as kd left join presensi_mahasiswa as pm on kd.id = pm.id_krs_detail inner join sesi_kuliah as sk on pm.id_sesi_kuliah = sk.id inner join jadwal as jd on sk.id_jadwal = jd.id inner join mahasiswa as ma on pm.id_mahasiswa = ma.id where jd.id_tahun_akademik = ?1 and pm.status_presensi IN ('MANGKIR','TERLAMBAT') and pm.status = 'AKTIF' and kd.status = 'AKTIF'\n"
                        +
                        "group by pm.id_mahasiswa, jd.id having count(pm.id) > 3 order by count(pm.id) DESC) as excludeMangkir on kd.id = excludeMangkir.id_krs_detail where kd.id_tahun_akademik = ?1 and excludeMangkir.id_krs_detail is NULL and kr.nama_karyawan LIKE %?2% group by jd.id_number_elearning order by jd.id_number_elearning DESC", countQuery = "SELECT count(kd.id) as jml FROM krs_detail as kd\n"
                                        +
                                        "inner join matakuliah_kurikulum as mku on kd.id_matakuliah_kurikulum = mku.id\n"
                                        +
                                        "inner join matakuliah as mk on mku.id_matakuliah = mk.id inner join mahasiswa as ma on kd.id_mahasiswa = ma.id inner join tahun_akademik as ta on kd.id_tahun_akademik = ta.id inner join jadwal as jd on kd.id_jadwal = jd.id inner join kelas as kl on jd.id_kelas = kl.id inner join prodi as pr on jd.id_prodi = pr.id inner join dosen as ds on jd.id_dosen_pengampu = ds.id inner join karyawan as kr on ds.id_karyawan = kr.id left join (SELECT kd.id, pm.id as id_presensi_mahasiswa, jd.id_tahun_akademik, pm.id_krs_detail,pm.id_mahasiswa as idMahasiswa,ma.nim as nim,ma.email_tazkia as email, pm.waktu_masuk, pm.waktu_keluar,pm.status_presensi, pm.status, jd.id as idJadwal,jd.id_number_elearning as idNumberElearning, jd.jam_mulai, jd.jam_selesai,count(pm.id) as absensi\n"
                                        +
                                        "FROM krs_detail as kd left join presensi_mahasiswa as pm on kd.id = pm.id_krs_detail inner join sesi_kuliah as sk on pm.id_sesi_kuliah = sk.id inner join jadwal as jd on sk.id_jadwal = jd.id inner join mahasiswa as ma on pm.id_mahasiswa = ma.id where jd.id_tahun_akademik = ?1 and pm.status_presensi IN ('MANGKIR','TERLAMBAT') and pm.status = 'AKTIF' and kd.status = 'AKTIF'\n"
                                        +
                                        "group by pm.id_mahasiswa, jd.id having count(pm.id) > 3 order by count(pm.id) DESC) as excludeMangkir on kd.id = excludeMangkir.id_krs_detail\n"
                                        +
                                        "where kd.id_tahun_akademik = ?1 and excludeMangkir.id_krs_detail is NULL and kr.nama_karyawan LIKE %?2% group by jd.id_number_elearning order by jd.id_number_elearning DESC", nativeQuery = true)
        Page<StatusImportNilaiDosenIntDto> findStatusImportNilaiDosenByDosen(TahunAkademik ta, String dosen,
                        Pageable page);

        List<Krs> findByMahasiswaAndStatusOrderByTahunAkademikKodeTahunAkademik(Mahasiswa mahasiswa,
                        StatusRecord status);

        @Query(value = "SELECT coalesce(ROUND(SUM(COALESCE(a.bobot,0)*b.jumlah_sks)/SUM(b.jumlah_sks),2), 0)AS ipk \n" +
                        "FROM krs_detail AS a INNER JOIN matakuliah_kurikulum AS b  ON a.id_matakuliah_kurikulum=b.id inner join mahasiswa as c on a.id_mahasiswa=c.id \n"
                        +
                        "inner join krs as d on a.id_krs = d.id inner join tahun_akademik as e on d.id_tahun_akademik = e.id\n"
                        +
                        "WHERE a.status='AKTIF' AND d.id_tahun_akademik=?1 \n" +
                        "AND b.jumlah_sks > 0 AND a.id_mahasiswa = ?2", nativeQuery = true)
        IpkDto cariIpSemester(String idTahunAkademik, String idMahasiswa);

        @Query(value = "SELECT COALESCE(ROUND(SUM(COALESCE(a.bobot,0)*b.jumlah_sks)/SUM(b.jumlah_sks),2), 0)AS ipk \n" +
                        "FROM krs_detail AS a INNER JOIN matakuliah_kurikulum AS b ON a.id_matakuliah_kurikulum=b.id \n"
                        +
                        "inner join krs as c on a.id_krs = c.id inner join tahun_akademik as d on c.id_tahun_akademik = d.id\n"
                        +
                        "WHERE a.status='AKTIF' AND b.jumlah_sks > 0 AND a.finalisasi = 'FINAL' AND a.id_mahasiswa = ?1 and d.kode_tahun_akademik <= ?2", nativeQuery = true)
        IpkDto cariIpkTotalPerSemester(String idMahasiswa, String kodeTahunAkademik);

        @Query(value = "select a.*, coalesce(ka.status, '-'), if(b.idKrs is not null, 'KRS AKTIF', 'NONAKTIF') as statusKrs, if(kd.id is not null, 'Sudah mengambil matkul', 'Belum mengambil matkul') as statusKrsDetail from\n"
                        +
                        "(select m.id, m.nim, m.nama, p.nama_prodi, m.angkatan, pr.nama_program, k.nama_karyawan from mahasiswa m inner join prodi p on m.id_prodi=p.id "
                        +
                        "inner join dosen d on m.id_dosen_wali=d.id inner join karyawan k on d.id_karyawan=k.id inner join program pr on m.id_program=pr.id "
                        +
                        "where m.status_aktif='AKTIF' and m.angkatan=?1 and m.id_prodi=?2)a\n" +
                        "left join\n" +
                        "(select k.id as idKrs, k.id_mahasiswa from krs k inner join tahun_akademik ta on k.id_tahun_akademik=ta.id "
                        +
                        "where k.id_tahun_akademik=?3 and k.status='AKTIF')b on a.id=b.id_mahasiswa\n" +
                        "left join krs_approval ka on b.idKrs=ka.id_krs\n" +
                        "left join krs_detail kd on b.idKrs=kd.id_krs where kd.status='AKTIF' group by a.id", nativeQuery = true, countQuery = "select count(a.id) from\n"
                                        +
                                        "(select m.id, m.nim, m.nama, p.nama_prodi, m.angkatan, pr.nama_program, k.nama_karyawan from mahasiswa m inner join prodi p on m.id_prodi=p.id "
                                        +
                                        "inner join dosen d on m.id_dosen_wali=d.id inner join karyawan k on d.id_karyawan=k.id inner join program pr on m.id_program=pr.id "
                                        +
                                        "where m.status_aktif='AKTIF' and m.angkatan=?1 and m.id_prodi=?2)a\n" +
                                        "left join\n" +
                                        "(select k.id as idKrs, k.id_mahasiswa from krs k inner join tahun_akademik ta on k.id_tahun_akademik=ta.id "
                                        +
                                        "where k.id_tahun_akademik=?3 and k.status='AKTIF')b on a.id=b.id_mahasiswa\n" +
                                        "left join krs_approval ka on b.idKrs=ka.id_krs\n" +
                                        "left join krs_detail kd on b.idKrs=kd.id_krs where kd.status='AKTIF' group by a.id")
        Page<Object[]> listKrsAdmin(String angkatan, String idProdi, String idTahun, Pageable page);

        @Query(value = "select a.*, coalesce(ka.status, '-'), if(b.idKrs is not null, 'KRS AKTIF', 'NONAKTIF') as statusKrs, if(kd.id is not null, 'Sudah mengambil matkul', 'Belum mengambil matkul') as statusKrsDetail from\n"
                        +
                        "(select m.id, m.nim, m.nama, p.nama_prodi, m.angkatan, pr.nama_program, k.nama_karyawan from mahasiswa m inner join prodi p on m.id_prodi=p.id "
                        +
                        "inner join dosen d on m.id_dosen_wali=d.id inner join karyawan k on d.id_karyawan=k.id inner join program pr on m.id_program=pr.id "
                        +
                        "where m.status_aktif='AKTIF' and m.angkatan=?1 and m.id_prodi=?2 and m.id_dosen_wali=?4)a\n" +
                        "left join\n" +
                        "(select k.id as idKrs, k.id_mahasiswa from krs k inner join tahun_akademik ta on k.id_tahun_akademik=ta.id "
                        +
                        "where k.id_tahun_akademik=?3 and k.status='AKTIF')b on a.id=b.id_mahasiswa\n" +
                        "left join krs_approval ka on b.idKrs=ka.id_krs\n" +
                        "left join krs_detail kd on b.idKrs=kd.id_krs where kd.status='AKTIF' group by a.id", nativeQuery = true, countQuery = "select count(a.id) from\n"
                                        +
                                        "(select m.id, m.nim, m.nama, p.nama_prodi, m.angkatan, pr.nama_program, k.nama_karyawan from mahasiswa m inner join prodi p on m.id_prodi=p.id "
                                        +
                                        "inner join dosen d on m.id_dosen_wali=d.id inner join karyawan k on d.id_karyawan=k.id inner join program pr on m.id_program=pr.id "
                                        +
                                        "where m.status_aktif='AKTIF' and m.angkatan=?1 and m.id_prodi=?2 and m.id_dosen_wali=?4)a\n"
                                        +
                                        "left join\n" +
                                        "(select k.id as idKrs, k.id_mahasiswa from krs k inner join tahun_akademik ta on k.id_tahun_akademik=ta.id "
                                        +
                                        "where k.id_tahun_akademik=?3 and k.status='AKTIF')b on a.id=b.id_mahasiswa\n" +
                                        "left join krs_approval ka on b.idKrs=ka.id_krs\n" +
                                        "left join krs_detail kd on b.idKrs=kd.id_krs where kd.status='AKTIF' group by a.id")
        Page<Object[]> listKrsAdminByDosenWali(String angkatan, String idProdi, String idTahun, String idDosen,
                        Pageable page);

        @Query(value = "SELECT dosen, idNumberElearning, namaProdi, kodeMatakuliah, namaKelas, namaMatakuliah, namaMatakuliahEnglish, statusImportTugas, statusImportUts, statusImportUas\n"
                        +
                        "FROM(SELECT kr.nama_karyawan AS dosen, jd.id_number_elearning AS idNumberElearning, pr.nama_prodi AS namaProdi, mk.kode_matakuliah AS kodeMatakuliah, kl.nama_kelas AS namaKelas, mk.nama_matakuliah AS namaMatakuliah, mk.nama_matakuliah_english AS namaMatakuliahEnglish, \n"
                        +
                        "CASE WHEN MIN(kd.nilai_tugas) > 0 THEN 'SUDAH' WHEN  MAX(kd.nilai_tugas) > 0 AND MIN(kd.nilai_tugas) = 0 THEN          'SUDAH (ada beberapa mahasiswa/i yang masih belum atau tidak dinilai, mohon dicek.)' ELSE 'TUGAS BELUM' END AS statusImportTugas, CASE WHEN MIN(kd.nilai_uts) > 0 THEN 'SUDAH' WHEN  MAX(kd.nilai_uts) > 0 AND MIN(kd.nilai_uts) = 0 THEN 'SUDAH (ada beberapa mahasiswa/i yang masih belum atau tidak dinilai, mohon dicek.)' ELSE 'UTS BELUM' END AS statusImportUts, CASE WHEN MIN(kd.nilai_uas) > 0 THEN 'SUDAH' WHEN  MAX(kd.nilai_uas) > 0 AND MIN(kd.nilai_uas) = 0 THEN 'SUDAH (ada beberapa mahasiswa/i yang masih belum atau tidak dinilai, mohon dicek.)' ELSE 'UAS BELUM' END AS statusImportUas\n"
                        +
                        "FROM krs_detail AS kd INNER JOIN matakuliah_kurikulum AS mku ON kd.id_matakuliah_kurikulum = mku.id INNER JOIN matakuliah AS mk ON mku.id_matakuliah = mk.id INNER JOIN mahasiswa AS ma ON kd.id_mahasiswa = ma.id INNER JOIN tahun_akademik AS ta ON kd.id_tahun_akademik = ta.id INNER JOIN jadwal AS jd ON kd.id_jadwal = jd.id INNER JOIN kelas AS kl ON jd.id_kelas = kl.id INNER JOIN prodi AS pr ON jd.id_prodi = pr.id INNER JOIN dosen AS ds ON jd.id_dosen_pengampu = ds.id INNER JOIN karyawan AS kr ON ds.id_karyawan = kr.id\n"
                        +
                        "LEFT JOIN (SELECT kd.id, pm.id AS id_presensi_mahasiswa, jd.id_tahun_akademik, pm.id_krs_detail, pm.id_mahasiswa AS idMahasiswa, ma.nim AS nim, ma.email_tazkia AS email, pm.waktu_masuk, pm.waktu_keluar, pm.status_presensi, pm.status, jd.id AS idJadwal, jd.id_number_elearning AS idNumberElearning, jd.jam_mulai, jd.jam_selesai, COUNT(pm.id) AS absensi\n"
                        +
                        "FROM krs_detail AS kd LEFT JOIN presensi_mahasiswa AS pm ON kd.id = pm.id_krs_detail INNER JOIN sesi_kuliah AS sk ON pm.id_sesi_kuliah = sk.id INNER JOIN jadwal AS jd ON sk.id_jadwal = jd.id INNER JOIN mahasiswa AS ma ON pm.id_mahasiswa = ma.id \n"
                        +
                        "WHERE jd.id_tahun_akademik = ?1 AND pm.status_presensi IN ('MANGKIR' , 'TERLAMBAT') AND pm.status = 'AKTIF' AND kd.status = 'AKTIF' GROUP BY pm.id_mahasiswa , jd.id HAVING COUNT(pm.id) > 3 ORDER BY COUNT(pm.id) DESC) AS excludeMangkir ON kd.id = excludeMangkir.id_krs_detail\n"
                        +
                        "WHERE kd.id_tahun_akademik = ?1  and excludeMangkir.id_krs_detail is NULL and kr.nama_karyawan LIKE %?2% GROUP BY jd.id_number_elearning ORDER BY jd.id_number_elearning DESC) AS subquery WHERE  subquery.statusImportTugas = 'TUGAS BELUM' OR subquery.statusImportUts = 'UTS BELUM' OR subquery.statusImportUas = 'UAS BELUM' ORDER BY subquery.namaProdi ASC , subquery.namaMatakuliah ASC , subquery.dosen ASC", countQuery = "SELECT COUNT(*) AS jml FROM (SELECT kr.nama_karyawan AS dosen, jd.id_number_elearning AS idNumberElearning, pr.nama_prodi AS namaProdi, mk.kode_matakuliah AS kodeMatakuliah, kl.nama_kelas AS namaKelas, mk.nama_matakuliah AS namaMatakuliah, mk.nama_matakuliah_english AS namaMatakuliahEnglish, CASE WHEN MIN(kd.nilai_tugas) > 0 THEN 'SUDAH' WHEN  MAX(kd.nilai_tugas) > 0 AND MIN(kd.nilai_tugas) = 0 THEN 'SUDAH (ada beberapa mahasiswa/i yang masih belum atau tidak dinilai, mohon dicek.)' ELSE 'TUGAS BELUM' END AS statusImportTugas, CASE WHEN MIN(kd.nilai_uts) > 0 THEN 'SUDAH' WHEN  MAX(kd.nilai_uts) > 0 AND MIN(kd.nilai_uts) = 0 THEN 'SUDAH (ada beberapa mahasiswa/i yang masih belum atau tidak dinilai, mohon dicek.)' ELSE 'UTS BELUM' END AS statusImportUts, CASE WHEN MIN(kd.nilai_uas) > 0 THEN 'SUDAH' WHEN  MAX(kd.nilai_uas) > 0 AND MIN(kd.nilai_uas) = 0 THEN 'SUDAH (ada beberapa mahasiswa/i yang masih belum atau tidak dinilai, mohon dicek.)' ELSE 'UAS BELUM' END AS statusImportUas  FROM krs_detail AS kd INNER JOIN matakuliah_kurikulum AS mku ON kd.id_matakuliah_kurikulum = mku.id INNER JOIN matakuliah AS mk ON mku.id_matakuliah = mk.id INNER JOIN mahasiswa AS ma ON kd.id_mahasiswa = ma.id INNER JOIN tahun_akademik AS ta ON kd.id_tahun_akademik = ta.id INNER JOIN jadwal AS jd ON kd.id_jadwal = jd.id INNER JOIN kelas AS kl ON jd.id_kelas = kl.id INNER JOIN prodi AS pr ON jd.id_prodi = pr.id INNER JOIN dosen AS ds ON jd.id_dosen_pengampu = ds.id INNER JOIN karyawan AS kr ON ds.id_karyawan = kr.id LEFT JOIN (SELECT  kd.id,  pm.id AS id_presensi_mahasiswa,  jd.id_tahun_akademik,  pm.id_krs_detail,  pm.id_mahasiswa AS idMahasiswa,  ma.nim AS nim,  ma.email_tazkia AS email,  pm.waktu_masuk,  pm.waktu_keluar,  pm.status_presensi,  pm.status,  jd.id AS idJadwal,  jd.id_number_elearning AS idNumberElearning,  jd.jam_mulai,  jd.jam_selesai,  COUNT(pm.id) AS absensi FROM krs_detail AS kd LEFT JOIN presensi_mahasiswa AS pm ON kd.id = pm.id_krs_detail INNER JOIN sesi_kuliah AS sk ON pm.id_sesi_kuliah = sk.id INNER JOIN jadwal AS jd ON sk.id_jadwal = jd.id INNER JOIN mahasiswa AS ma ON pm.id_mahasiswa = ma.id  WHERE jd.id_tahun_akademik = ?1 AND pm.status_presensi IN ('MANGKIR' , 'TERLAMBAT') AND pm.status = 'AKTIF' AND kd.status = 'AKTIF' GROUP BY pm.id_mahasiswa , jd.id HAVING COUNT(pm.id) > 3 ORDER BY COUNT(pm.id) DESC) AS excludeMangkir ON kd.id = excludeMangkir.id_krs_detail  WHERE kd.id_tahun_akademik = ?1 and excludeMangkir.id_krs_detail is NULL and kr.nama_karyawan LIKE %?2% GROUP BY jd.id_number_elearning ORDER BY jd.id_number_elearning DESC) AS subquery WHERE subquery.statusImportTugas = 'TUGAS BELUM' OR subquery.statusImportUts = 'UTS BELUM' OR subquery.statusImportUas = 'UAS BELUM'\n", nativeQuery = true)
        Page<StatusImportNilaiDosenIntDto> findStatusImportNilaiDosenByDosen2(TahunAkademik ta, String dosen,
                        Pageable page);

        @Query(value = "SELECT dosen, idNumberElearning, namaProdi, kodeMatakuliah, namaKelas, namaMatakuliah, namaMatakuliahEnglish, statusImportTugas, statusImportUts, statusImportUas\n"
                        +
                        "FROM(SELECT kr.nama_karyawan AS dosen, jd.id_number_elearning AS idNumberElearning, pr.nama_prodi AS namaProdi, mk.kode_matakuliah AS kodeMatakuliah, kl.nama_kelas AS namaKelas, mk.nama_matakuliah AS namaMatakuliah, mk.nama_matakuliah_english AS namaMatakuliahEnglish, \n"
                        +
                        "CASE WHEN MIN(kd.nilai_tugas) > 0 THEN 'SUDAH' WHEN  MAX(kd.nilai_tugas) > 0 AND MIN(kd.nilai_tugas) = 0 THEN          'SUDAH (ada beberapa mahasiswa/i yang masih belum atau tidak dinilai, mohon dicek.)' ELSE 'TUGAS BELUM' END AS statusImportTugas, CASE WHEN MIN(kd.nilai_uts) > 0 THEN 'SUDAH' WHEN  MAX(kd.nilai_uts) > 0 AND MIN(kd.nilai_uts) = 0 THEN 'SUDAH (ada beberapa mahasiswa/i yang masih belum atau tidak dinilai, mohon dicek.)' ELSE 'UTS BELUM' END AS statusImportUts, CASE WHEN MIN(kd.nilai_uas) > 0 THEN 'SUDAH' WHEN  MAX(kd.nilai_uas) > 0 AND MIN(kd.nilai_uas) = 0 THEN 'SUDAH (ada beberapa mahasiswa/i yang masih belum atau tidak dinilai, mohon dicek.)' ELSE 'UAS BELUM' END AS statusImportUas\n"
                        +
                        "FROM krs_detail AS kd INNER JOIN matakuliah_kurikulum AS mku ON kd.id_matakuliah_kurikulum = mku.id INNER JOIN matakuliah AS mk ON mku.id_matakuliah = mk.id INNER JOIN mahasiswa AS ma ON kd.id_mahasiswa = ma.id INNER JOIN tahun_akademik AS ta ON kd.id_tahun_akademik = ta.id INNER JOIN jadwal AS jd ON kd.id_jadwal = jd.id INNER JOIN kelas AS kl ON jd.id_kelas = kl.id INNER JOIN prodi AS pr ON jd.id_prodi = pr.id INNER JOIN dosen AS ds ON jd.id_dosen_pengampu = ds.id INNER JOIN karyawan AS kr ON ds.id_karyawan = kr.id\n"
                        +
                        "LEFT JOIN (SELECT kd.id, pm.id AS id_presensi_mahasiswa, jd.id_tahun_akademik, pm.id_krs_detail, pm.id_mahasiswa AS idMahasiswa, ma.nim AS nim, ma.email_tazkia AS email, pm.waktu_masuk, pm.waktu_keluar, pm.status_presensi, pm.status, jd.id AS idJadwal, jd.id_number_elearning AS idNumberElearning, jd.jam_mulai, jd.jam_selesai, COUNT(pm.id) AS absensi\n"
                        +
                        "FROM krs_detail AS kd LEFT JOIN presensi_mahasiswa AS pm ON kd.id = pm.id_krs_detail INNER JOIN sesi_kuliah AS sk ON pm.id_sesi_kuliah = sk.id INNER JOIN jadwal AS jd ON sk.id_jadwal = jd.id INNER JOIN mahasiswa AS ma ON pm.id_mahasiswa = ma.id \n"
                        +
                        "WHERE jd.id_tahun_akademik = ?1 AND pm.status_presensi IN ('MANGKIR' , 'TERLAMBAT') AND pm.status = 'AKTIF' AND kd.status = 'AKTIF' GROUP BY pm.id_mahasiswa , jd.id HAVING COUNT(pm.id) > 3 ORDER BY COUNT(pm.id) DESC) AS excludeMangkir ON kd.id = excludeMangkir.id_krs_detail\n"
                        +
                        "WHERE kd.id_tahun_akademik = ?1 AND excludeMangkir.id_krs_detail IS NULL GROUP BY jd.id_number_elearning ORDER BY jd.id_number_elearning DESC) AS subquery WHERE  subquery.statusImportTugas = 'TUGAS BELUM' OR subquery.statusImportUts = 'UTS BELUM' OR subquery.statusImportUas = 'UAS BELUM' ORDER BY subquery.namaProdi ASC , subquery.namaMatakuliah ASC , subquery.dosen ASC", countQuery = "SELECT COUNT(*) AS jml FROM (SELECT kr.nama_karyawan AS dosen, jd.id_number_elearning AS idNumberElearning, pr.nama_prodi AS namaProdi, mk.kode_matakuliah AS kodeMatakuliah, kl.nama_kelas AS namaKelas, mk.nama_matakuliah AS namaMatakuliah, mk.nama_matakuliah_english AS namaMatakuliahEnglish, CASE WHEN MIN(kd.nilai_tugas) > 0 THEN 'SUDAH' WHEN  MAX(kd.nilai_tugas) > 0 AND MIN(kd.nilai_tugas) = 0 THEN 'SUDAH (ada beberapa mahasiswa/i yang masih belum atau tidak dinilai, mohon dicek.)' ELSE 'TUGAS BELUM' END AS statusImportTugas, CASE WHEN MIN(kd.nilai_uts) > 0 THEN 'SUDAH' WHEN  MAX(kd.nilai_uts) > 0 AND MIN(kd.nilai_uts) = 0 THEN 'SUDAH (ada beberapa mahasiswa/i yang masih belum atau tidak dinilai, mohon dicek.)' ELSE 'UTS BELUM' END AS statusImportUts, CASE WHEN MIN(kd.nilai_uas) > 0 THEN 'SUDAH' WHEN  MAX(kd.nilai_uas) > 0 AND MIN(kd.nilai_uas) = 0 THEN 'SUDAH (ada beberapa mahasiswa/i yang masih belum atau tidak dinilai, mohon dicek.)' ELSE 'UAS BELUM' END AS statusImportUas  FROM krs_detail AS kd INNER JOIN matakuliah_kurikulum AS mku ON kd.id_matakuliah_kurikulum = mku.id INNER JOIN matakuliah AS mk ON mku.id_matakuliah = mk.id INNER JOIN mahasiswa AS ma ON kd.id_mahasiswa = ma.id INNER JOIN tahun_akademik AS ta ON kd.id_tahun_akademik = ta.id INNER JOIN jadwal AS jd ON kd.id_jadwal = jd.id INNER JOIN kelas AS kl ON jd.id_kelas = kl.id INNER JOIN prodi AS pr ON jd.id_prodi = pr.id INNER JOIN dosen AS ds ON jd.id_dosen_pengampu = ds.id INNER JOIN karyawan AS kr ON ds.id_karyawan = kr.id LEFT JOIN (SELECT  kd.id,  pm.id AS id_presensi_mahasiswa,  jd.id_tahun_akademik,  pm.id_krs_detail,  pm.id_mahasiswa AS idMahasiswa,  ma.nim AS nim,  ma.email_tazkia AS email,  pm.waktu_masuk,  pm.waktu_keluar,  pm.status_presensi,  pm.status,  jd.id AS idJadwal,  jd.id_number_elearning AS idNumberElearning,  jd.jam_mulai,  jd.jam_selesai,  COUNT(pm.id) AS absensi FROM krs_detail AS kd LEFT JOIN presensi_mahasiswa AS pm ON kd.id = pm.id_krs_detail INNER JOIN sesi_kuliah AS sk ON pm.id_sesi_kuliah = sk.id INNER JOIN jadwal AS jd ON sk.id_jadwal = jd.id INNER JOIN mahasiswa AS ma ON pm.id_mahasiswa = ma.id  WHERE jd.id_tahun_akademik = ?1 AND pm.status_presensi IN ('MANGKIR' , 'TERLAMBAT') AND pm.status = 'AKTIF' AND kd.status = 'AKTIF' GROUP BY pm.id_mahasiswa , jd.id HAVING COUNT(pm.id) > 3 ORDER BY COUNT(pm.id) DESC) AS excludeMangkir ON kd.id = excludeMangkir.id_krs_detail  WHERE kd.id_tahun_akademik = ?1 AND excludeMangkir.id_krs_detail IS NULL GROUP BY jd.id_number_elearning ORDER BY jd.id_number_elearning DESC) AS subquery WHERE subquery.statusImportTugas = 'TUGAS BELUM' OR subquery.statusImportUts = 'UTS BELUM' OR subquery.statusImportUas = 'UAS BELUM'\n", nativeQuery = true)
        Page<StatusImportNilaiDosenIntDto> findStatusImportNilaiDosen2(TahunAkademik ta, Pageable page);

        @Query(value = "SELECT dosen, idNumberElearning, namaProdi, kodeMatakuliah, namaKelas, namaMatakuliah, namaMatakuliahEnglish, statusImportTugas, statusImportUts, statusImportUas\n"
                        +
                        "FROM(SELECT kr.nama_karyawan AS dosen, jd.id_number_elearning AS idNumberElearning, pr.nama_prodi AS namaProdi, mk.kode_matakuliah AS kodeMatakuliah, kl.nama_kelas AS namaKelas, mk.nama_matakuliah AS namaMatakuliah, mk.nama_matakuliah_english AS namaMatakuliahEnglish, \n"
                        +
                        "CASE WHEN MIN(kd.nilai_tugas) > 0 THEN 'SUDAH' WHEN  MAX(kd.nilai_tugas) > 0 AND MIN(kd.nilai_tugas) = 0 THEN          'SUDAH (ada beberapa mahasiswa/i yang masih belum atau tidak dinilai, mohon dicek.)' ELSE 'TUGAS BELUM' END AS statusImportTugas, CASE WHEN MIN(kd.nilai_uts) > 0 THEN 'SUDAH' WHEN  MAX(kd.nilai_uts) > 0 AND MIN(kd.nilai_uts) = 0 THEN 'SUDAH (ada beberapa mahasiswa/i yang masih belum atau tidak dinilai, mohon dicek.)' ELSE 'UTS BELUM' END AS statusImportUts, CASE WHEN MIN(kd.nilai_uas) > 0 THEN 'SUDAH' WHEN  MAX(kd.nilai_uas) > 0 AND MIN(kd.nilai_uas) = 0 THEN 'SUDAH (ada beberapa mahasiswa/i yang masih belum atau tidak dinilai, mohon dicek.)' ELSE 'UAS BELUM' END AS statusImportUas\n"
                        +
                        "FROM krs_detail AS kd INNER JOIN matakuliah_kurikulum AS mku ON kd.id_matakuliah_kurikulum = mku.id INNER JOIN matakuliah AS mk ON mku.id_matakuliah = mk.id INNER JOIN mahasiswa AS ma ON kd.id_mahasiswa = ma.id INNER JOIN tahun_akademik AS ta ON kd.id_tahun_akademik = ta.id INNER JOIN jadwal AS jd ON kd.id_jadwal = jd.id INNER JOIN kelas AS kl ON jd.id_kelas = kl.id INNER JOIN prodi AS pr ON jd.id_prodi = pr.id INNER JOIN dosen AS ds ON jd.id_dosen_pengampu = ds.id INNER JOIN karyawan AS kr ON ds.id_karyawan = kr.id\n"
                        +
                        "LEFT JOIN (SELECT kd.id, pm.id AS id_presensi_mahasiswa, jd.id_tahun_akademik, pm.id_krs_detail, pm.id_mahasiswa AS idMahasiswa, ma.nim AS nim, ma.email_tazkia AS email, pm.waktu_masuk, pm.waktu_keluar, pm.status_presensi, pm.status, jd.id AS idJadwal, jd.id_number_elearning AS idNumberElearning, jd.jam_mulai, jd.jam_selesai, COUNT(pm.id) AS absensi\n"
                        +
                        "FROM krs_detail AS kd LEFT JOIN presensi_mahasiswa AS pm ON kd.id = pm.id_krs_detail INNER JOIN sesi_kuliah AS sk ON pm.id_sesi_kuliah = sk.id INNER JOIN jadwal AS jd ON sk.id_jadwal = jd.id INNER JOIN mahasiswa AS ma ON pm.id_mahasiswa = ma.id \n"
                        +
                        "WHERE jd.id_tahun_akademik = ?1 AND pm.status_presensi IN ('MANGKIR' , 'TERLAMBAT') AND pm.status = 'AKTIF' AND kd.status = 'AKTIF' GROUP BY pm.id_mahasiswa , jd.id HAVING COUNT(pm.id) > 3 ORDER BY COUNT(pm.id) DESC) AS excludeMangkir ON kd.id = excludeMangkir.id_krs_detail\n"
                        +
                        "WHERE kd.id_tahun_akademik = ?1 AND excludeMangkir.id_krs_detail IS NULL GROUP BY jd.id_number_elearning ORDER BY jd.id_number_elearning DESC) AS subquery WHERE  subquery.statusImportTugas = 'TUGAS BELUM' OR subquery.statusImportUts = 'UTS BELUM' OR subquery.statusImportUas = 'UAS BELUM' ORDER BY subquery.namaProdi ASC , subquery.namaMatakuliah ASC , subquery.dosen ASC", countQuery = "SELECT COUNT(*) AS jml FROM (SELECT kr.nama_karyawan AS dosen, jd.id_number_elearning AS idNumberElearning, pr.nama_prodi AS namaProdi, mk.kode_matakuliah AS kodeMatakuliah, kl.nama_kelas AS namaKelas, mk.nama_matakuliah AS namaMatakuliah, mk.nama_matakuliah_english AS namaMatakuliahEnglish, CASE WHEN MIN(kd.nilai_tugas) > 0 THEN 'SUDAH' WHEN  MAX(kd.nilai_tugas) > 0 AND MIN(kd.nilai_tugas) = 0 THEN 'SUDAH (ada beberapa mahasiswa/i yang masih belum atau tidak dinilai, mohon dicek.)' ELSE 'TUGAS BELUM' END AS statusImportTugas, CASE WHEN MIN(kd.nilai_uts) > 0 THEN 'SUDAH' WHEN  MAX(kd.nilai_uts) > 0 AND MIN(kd.nilai_uts) = 0 THEN 'SUDAH (ada beberapa mahasiswa/i yang masih belum atau tidak dinilai, mohon dicek.)' ELSE 'UTS BELUM' END AS statusImportUts, CASE WHEN MIN(kd.nilai_uas) > 0 THEN 'SUDAH' WHEN  MAX(kd.nilai_uas) > 0 AND MIN(kd.nilai_uas) = 0 THEN 'SUDAH (ada beberapa mahasiswa/i yang masih belum atau tidak dinilai, mohon dicek.)' ELSE 'UAS BELUM' END AS statusImportUas  FROM krs_detail AS kd INNER JOIN matakuliah_kurikulum AS mku ON kd.id_matakuliah_kurikulum = mku.id INNER JOIN matakuliah AS mk ON mku.id_matakuliah = mk.id INNER JOIN mahasiswa AS ma ON kd.id_mahasiswa = ma.id INNER JOIN tahun_akademik AS ta ON kd.id_tahun_akademik = ta.id INNER JOIN jadwal AS jd ON kd.id_jadwal = jd.id INNER JOIN kelas AS kl ON jd.id_kelas = kl.id INNER JOIN prodi AS pr ON jd.id_prodi = pr.id INNER JOIN dosen AS ds ON jd.id_dosen_pengampu = ds.id INNER JOIN karyawan AS kr ON ds.id_karyawan = kr.id LEFT JOIN (SELECT  kd.id,  pm.id AS id_presensi_mahasiswa,  jd.id_tahun_akademik,  pm.id_krs_detail,  pm.id_mahasiswa AS idMahasiswa,  ma.nim AS nim,  ma.email_tazkia AS email,  pm.waktu_masuk,  pm.waktu_keluar,  pm.status_presensi,  pm.status,  jd.id AS idJadwal,  jd.id_number_elearning AS idNumberElearning,  jd.jam_mulai,  jd.jam_selesai,  COUNT(pm.id) AS absensi FROM krs_detail AS kd LEFT JOIN presensi_mahasiswa AS pm ON kd.id = pm.id_krs_detail INNER JOIN sesi_kuliah AS sk ON pm.id_sesi_kuliah = sk.id INNER JOIN jadwal AS jd ON sk.id_jadwal = jd.id INNER JOIN mahasiswa AS ma ON pm.id_mahasiswa = ma.id  WHERE jd.id_tahun_akademik = ?1 AND pm.status_presensi IN ('MANGKIR' , 'TERLAMBAT') AND pm.status = 'AKTIF' AND kd.status = 'AKTIF' GROUP BY pm.id_mahasiswa , jd.id HAVING COUNT(pm.id) > 3 ORDER BY COUNT(pm.id) DESC) AS excludeMangkir ON kd.id = excludeMangkir.id_krs_detail  WHERE kd.id_tahun_akademik = ?1 AND excludeMangkir.id_krs_detail IS NULL GROUP BY jd.id_number_elearning ORDER BY jd.id_number_elearning DESC) AS subquery WHERE subquery.statusImportTugas = 'TUGAS BELUM' OR subquery.statusImportUts = 'UTS BELUM' OR subquery.statusImportUas = 'UAS BELUM'\n", nativeQuery = true)
        List<StatusImportNilaiDosenIntDto> findStatusImportNilaiDosenList(TahunAkademik ta);

        @Query(value = "select m.id as id, m.nim as nim, m.nama as nama, p.nama_prodi as prodi from krs as k inner join mahasiswa as m on k.id_mahasiswa = m.id inner join prodi as p on m.id_prodi = p.id where (m.nim like %?1% or m.nama like %?1%) and k.status = 'AKTIF' and k.id_tahun_akademik = ?2 order by m.nim asc", nativeQuery = true)
        List<MahasiswaDto> cariMahasiswa(String search, String tahunAkademik);

        @Modifying
        @Query(value = "UPDATE krs AS kd\n" +
                        "INNER JOIN tahun_akademik AS ta \n" +
                        "ON kd.id_tahun_akademik = ta.id\n" +
                        "SET kd.status = 'MENGUNDURKAN_DIRI'\n" +
                        "WHERE kd.id_mahasiswa = ?1 \n" +
                        "  AND kd.status = 'AKTIF'\n" +
                        "  AND ta.kode_tahun_akademik > ?2 ", nativeQuery = true)
        void updateStatusKrsMengundurkanDiri(Mahasiswa mahasiswa, String kodeTahunAkademik);

}
