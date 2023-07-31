package id.ac.tazkia.smilemahasiswa.dao;

import id.ac.tazkia.smilemahasiswa.dto.EnrollMahasiswaIntDto;
import id.ac.tazkia.smilemahasiswa.dto.elearning.StatusImportNilaiDosenDto;
import id.ac.tazkia.smilemahasiswa.dto.elearning.StatusImportNilaiDosenIntDto;
import id.ac.tazkia.smilemahasiswa.entity.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.PagingAndSortingRepository;

import java.time.LocalDate;
import java.util.List;

public interface KrsDao extends PagingAndSortingRepository<Krs, String> {
    Krs findByMahasiswaAndTahunAkademikAndStatus(Mahasiswa mahasiswa, TahunAkademik ta, StatusRecord s);

    Long countKrsByTahunAkademikAndMahasiswaStatus(TahunAkademik tahunAkademik, StatusRecord aktif);

    Long countKrsByTahunAkademikAndMahasiswaJenisKelamin(TahunAkademik tahunAkademik, JenisKelamin pria);

    Page<Krs> findByTahunAkademikAndProdiAndMahasiswaIdProgramAndMahasiswaAngkatan(TahunAkademik tahunAkademik, Prodi prodi, Program program, String angkatan, Pageable pageable);

    Krs findByTahunAkademikAndMahasiswaAndStatus(TahunAkademik tahunAkademik, Mahasiswa mhsw, StatusRecord aktif);

    Page<Krs> findByProdiAndTahunAkademikAndStatus(Prodi prodi, TahunAkademik tahunAkademik, StatusRecord statusRecord, Pageable page);

    @Query(value = "SELECT a.* FROM (SELECT a.id AS id_krs,b.nim,b.nama,c.nama_prodi FROM krs AS a INNER JOIN mahasiswa AS b ON a.id_mahasiswa=b.id INNER JOIN prodi AS c ON b.id_prodi=c.id WHERE a.id_tahun_akademik=?1 AND a.status='AKTIF' GROUP BY a.id_mahasiswa)a LEFT JOIN (SELECT * FROM krs_detail WHERE id_tahun_akademik=?1 AND STATUS='AKTIF' AND id_jadwal=?2 GROUP BY id_mahasiswa)b ON a.id_krs=b.id_krs WHERE b.id IS NULL", nativeQuery = true)
    List<Object[]> krsList(TahunAkademik tahun, Jadwal jadwal );



    @Query(value="select a.id, b.kode_tahun_akademik, b.nama_tahun_akademik,b.jenis from krs as a \n" +
            "inner join krs_detail as g on a.id = g.id_krs\n" +
            "inner join jadwal as h on g.id_jadwal = h.id\n" +
            "inner join matakuliah_kurikulum as i on h.id_matakuliah_kurikulum = i.id\n" +
            "inner join tahun_akademik as b on a.id_tahun_akademik = b.id\n" +
            "where a.status = 'AKTIF' and g.status='AKTIF' and i.jumlah_sks > 0 and a.id_mahasiswa = ?1 group by a.id \n" +
            "order by b.kode_tahun_akademik", nativeQuery = true)
    List<Object[]> semesterTranskript1(String idMahasiswa);

    @Query(value = "CAll getsemestertranskript(?1);", nativeQuery = true)
    List<Object[]> semesterTranskript(String idMahasiswa);

    @Query(value = "select count(a.id) from krs as a inner join tahun_akademik as b on a.id_tahun_akademik = b.id where a.status = 'AKTIF' and a.id_mahasiswa = ?1 and b.jenis != 'PENDEK'", nativeQuery = true)
    Integer countSemester(String nim);

    @Query(value = "SELECT id_jadwal, kode_matakuliah, nama_matakuliah, nama_matakuliah_english, jumlah_sks,jam_mulai,jam_selesai, nama_hari, nama_karyawan, prasyarat, bobot_prasyarat, setara, ambil_sebelum,bobot_sebelum,ambil_prasyarat,bobot_ambil_prass, already FROM\n" +
            "(SELECT a.id AS id_jadwal, c.kode_matakuliah, c.nama_matakuliah, c.nama_matakuliah_english,z.nama_hari as nama_hari,j.nama_karyawan as nama_karyawan,b.jumlah_sks,jam_mulai,jam_selesai, prasyarat, e.nilai AS bobot_prasyarat, setara, g.nama_matakuliah AS ambil_sebelum, g.bobot AS bobot_sebelum, h.nama_matakuliah AS ambil_prasyarat, h.bobot AS bobot_ambil_prass, i.kode_matakuliah AS already FROM jadwal AS a\n" +
            "INNER JOIN matakuliah_kurikulum AS b ON a.id_matakuliah_kurikulum = b.id\n" +
            "INNER JOIN matakuliah AS c ON b.id_matakuliah = c.id\n" +
            "INNER JOIN kelas_mahasiswa AS d ON a.id_kelas = d.id_kelas\n" +
            "INNER JOIN hari as z ON z.id = a.id_hari\n" +
            "INNER JOIN karyawan as j ON j.id = a.id_dosen_pengampu\n" +
            "LEFT JOIN \n" +
            "\t(SELECT b.nama_matakuliah AS prasyarat,a.id_matakuliah,b.kode_matakuliah,a.nilai FROM prasyarat AS a INNER JOIN matakuliah AS b ON a.id_matakuliah_pras = b.id WHERE a.status = 'AKTIF') e ON c.id = e.id_matakuliah\n" +
            "LEFT JOIN \n" +
            "\t(SELECT b.nama_matakuliah AS setara,a.id_matakuliah,b.kode_matakuliah FROM matakuliah_setara AS a INNER JOIN matakuliah AS b ON a.id_matakuliah_setara = b.id WHERE a.status ='AKTIF' ) f ON c.id = f.id_matakuliah\n" +
            "LEFT JOIN\n" +
            "\t(SELECT d.id, d.kode_matakuliah, d.nama_matakuliah, d.nama_matakuliah_english, a.bobot FROM krs_detail AS a \n" +
            "\tINNER JOIN jadwal AS b ON a.id_jadwal = b.id\n" +
            "\tINNER JOIN matakuliah_kurikulum AS c ON b.id_matakuliah_kurikulum = c.id\n" +
            "\tINNER JOIN matakuliah AS d ON c.id_matakuliah = d.id\n" +
            "\tWHERE a.status = 'AKTIF' AND a.id_tahun_akademik <> ?1 AND a.id_mahasiswa = ?2)g \n" +
            "\tON c.id = g.id OR e.id_matakuliah = g.id\n" +
            "LEFT JOIN\n" +
            "\t(SELECT d.id,d.kode_matakuliah, d.nama_matakuliah, d.nama_matakuliah_english, a.bobot FROM krs_detail AS a \n" +
            "\tINNER JOIN jadwal AS b ON a.id_jadwal = b.id\n" +
            "\tINNER JOIN matakuliah_kurikulum AS c ON b.id_matakuliah_kurikulum = c.id\n" +
            "\tINNER JOIN matakuliah AS d ON c.id_matakuliah = d.id\n" +
            "\tWHERE a.status = 'AKTIF' AND a.id_tahun_akademik <> ?1 AND a.id_mahasiswa = ?2)h \n" +
            "\tON e.id_matakuliah = h.id OR e.kode_matakuliah = h.kode_matakuliah\n" +
            "LEFT JOIN\n" +
            "\t(SELECT d.id,d.kode_matakuliah, d.nama_matakuliah, d.nama_matakuliah_english, a.bobot FROM krs_detail AS a \n" +
            "\tINNER JOIN jadwal AS b ON a.id_jadwal = b.id\n" +
            "\tINNER JOIN matakuliah_kurikulum AS c ON b.id_matakuliah_kurikulum = c.id\n" +
            "\tINNER JOIN matakuliah AS d ON c.id_matakuliah = d.id\n" +
            "\tWHERE a.status = 'AKTIF' AND a.id_tahun_akademik = ?1 AND a.id_mahasiswa = ?2)i\n" +
            "\tON c.id = i.id OR c.kode_matakuliah = i.kode_matakuliah\n" +
            "WHERE a.id_tahun_akademik = ?1 AND a.status = 'AKTIF' AND d.id_mahasiswa = ?2 AND b.jumlah_sks > 0 AND id_hari IS NOT NULL AND jam_mulai IS NOT NULL AND jam_selesai IS NOT NULL\n" +
            "GROUP BY a.id \n" +
            "ORDER BY nama_matakuliah)a WHERE already IS NULL", nativeQuery = true)
    List<Object[]> listKrs(TahunAkademik ta, Mahasiswa mahasiswa);


    @Query(value = "SELECT * FROM\n" +
            "(SELECT a.id AS idKrs, b.id AS idKrsDetail, b.id_jadwal as idJadwal, b.id_mahasiswa as idMahasiswa, COALESCE(e.username,c.email_tazkia) AS email, c.nim, d.id_number_elearning as idNumberElearning, b.status_enroll as statusEnroll\n" +
            "FROM krs AS a \n" +
            "INNER JOIN krs_detail AS b ON a.id = b.id_krs\n" +
            "INNER JOIN mahasiswa AS c ON b.id_mahasiswa = c.id\n" +
            "INNER JOIN jadwal AS d ON b.id_jadwal = d.id\n" +
            "INNER JOIN s_user AS e ON c.id_user = e.id\n" +
            "INNER JOIN matakuliah_kurikulum AS f ON d.id_matakuliah_kurikulum = f.id\n" +
            "WHERE a.id_tahun_akademik = 'f635cb68-3da5-4e11-8213-7020636034a2' AND a.status = 'AKTIF' AND b.status = 'AKTIF' AND b.status_enroll is NULL) mdl_krs_smile", nativeQuery = true)
    List<EnrollMahasiswaIntDto> findEnrollMahasiswa();


    @Query(value = "SELECT kr.nama_karyawan as dosen,jd.id_number_elearning as idNumberElearning,pr.nama_prodi as namaProdi ,mk.kode_matakuliah as kodeMatakuliah,kl.nama_kelas as namaKelas,mk.nama_matakuliah as namaMatakuliah,mk.nama_matakuliah_english as namaMatakuliahEnglish,\n" +
            "CASE\n" +
            "        WHEN min(nilai_tugas) > 0 THEN 'SUDAH'\n" +
            "        WHEN max(nilai_tugas) > 0 and min(nilai_tugas) = 0 THEN 'SUDAH (ada beberapa mahasiswa/i yang masih belum atau tidak dinilai, mohon dicek.)'\n" +
            "        ELSE 'TUGAS BELUM'\n" +
            "    END AS statusImportTugas,\n" +
            "CASE\n" +
            "        WHEN min(nilai_uts) > 0 THEN 'SUDAH'\n" +
            "        WHEN max(nilai_uts) > 0 and min(nilai_uts) = 0 THEN 'SUDAH (ada beberapa mahasiswa/i yang masih belum atau tidak dinilai, mohon dicek.)'\n" +
            "        ELSE 'UTS BELUM'\n" +
            "    END AS statusImportUts,\n" +
            "CASE\n" +
            "        WHEN min(nilai_uas) > 0 THEN 'SUDAH'\n" +
            "        WHEN max(nilai_uas) > 0 and min(nilai_uas) = 0 THEN 'SUDAH (ada beberapa mahasiswa/i yang masih belum atau tidak dinilai, mohon dicek.)'\n" +
            "        ELSE 'UAS BELUM'\n" +
            "    END AS statusImportUas\n" +
            "FROM krs_detail as kd \n" +
            "inner join matakuliah_kurikulum as mku on kd.id_matakuliah_kurikulum = mku.id\n" +
            "inner join matakuliah as mk on mku.id_matakuliah = mk.id\n" +
            "inner join mahasiswa as ma on kd.id_mahasiswa = ma.id\n" +
            "inner join tahun_akademik as ta on kd.id_tahun_akademik = ta.id\n" +
            "inner join jadwal as jd on kd.id_jadwal = jd.id\n" +
            "inner join kelas as kl on jd.id_kelas = kl.id\n" +
            "inner join prodi as pr on jd.id_prodi = pr.id\n" +
            "inner join dosen as ds on jd.id_dosen_pengampu = ds.id\n" +
            "inner join karyawan as kr on ds.id_karyawan = kr.id\n" +
            "left join (SELECT kd.id, pm.id as id_presensi_mahasiswa, jd.id_tahun_akademik, pm.id_krs_detail,pm.id_mahasiswa as idMahasiswa,ma.nim as nim,ma.email_tazkia as email, pm.waktu_masuk, pm.waktu_keluar,pm.status_presensi, pm.status, jd.id as idJadwal,jd.id_number_elearning as idNumberElearning, jd.jam_mulai, jd.jam_selesai,count(pm.id) as absensi\n" +
            "FROM krs_detail as kd\n" +
            "left join presensi_mahasiswa as pm on kd.id = pm.id_krs_detail\n" +
            "inner join sesi_kuliah as sk on pm.id_sesi_kuliah = sk.id\n" +
            "inner join jadwal as jd on sk.id_jadwal = jd.id\n" +
            "inner join mahasiswa as ma on pm.id_mahasiswa = ma.id\n" +
            "where jd.id_tahun_akademik = ?1 and pm.status_presensi IN ('MANGKIR','TERLAMBAT') and pm.status = 'AKTIF' and kd.status = 'AKTIF'\n" +
            "group by pm.id_mahasiswa, jd.id\n" +
            "having count(pm.id) > 3\n" +
            "order by count(pm.id) DESC) as excludeMangkir on kd.id = excludeMangkir.id_krs_detail\n" +
            "where kd.id_tahun_akademik = ?1 and excludeMangkir.id_krs_detail is NULL\n" +
            "group by jd.id_number_elearning\n" +
            "order by jd.id_number_elearning DESC",countQuery = "SELECT count(kd.id) as jml FROM krs_detail as kd \n" +
            "inner join matakuliah_kurikulum as mku on kd.id_matakuliah_kurikulum = mku.id\n" +
            "inner join matakuliah as mk on mku.id_matakuliah = mk.id\n" +
            "inner join mahasiswa as ma on kd.id_mahasiswa = ma.id\n" +
            "inner join tahun_akademik as ta on kd.id_tahun_akademik = ta.id\n" +
            "inner join jadwal as jd on kd.id_jadwal = jd.id\n" +
            "inner join kelas as kl on jd.id_kelas = kl.id\n" +
            "inner join prodi as pr on jd.id_prodi = pr.id\n" +
            "inner join dosen as ds on jd.id_dosen_pengampu = ds.id\n" +
            "inner join karyawan as kr on ds.id_karyawan = kr.id\n" +
            "left join (SELECT kd.id, pm.id as id_presensi_mahasiswa, jd.id_tahun_akademik, pm.id_krs_detail,pm.id_mahasiswa as idMahasiswa,ma.nim as nim,ma.email_tazkia as email, pm.waktu_masuk, pm.waktu_keluar,pm.status_presensi, pm.status, jd.id as idJadwal,jd.id_number_elearning as idNumberElearning, jd.jam_mulai, jd.jam_selesai,count(pm.id) as absensi\n" +
            "FROM krs_detail as kd\n" +
            "left join presensi_mahasiswa as pm on kd.id = pm.id_krs_detail\n" +
            "inner join sesi_kuliah as sk on pm.id_sesi_kuliah = sk.id\n" +
            "inner join jadwal as jd on sk.id_jadwal = jd.id\n" +
            "inner join mahasiswa as ma on pm.id_mahasiswa = ma.id\n" +
            "where jd.id_tahun_akademik = ?1 and pm.status_presensi IN ('MANGKIR','TERLAMBAT') and pm.status = 'AKTIF' and kd.status = 'AKTIF'\n" +
            "group by pm.id_mahasiswa, jd.id\n" +
            "having count(pm.id) > 3\n" +
            "order by count(pm.id) DESC) as excludeMangkir on kd.id = excludeMangkir.id_krs_detail\n" +
            "where kd.id_tahun_akademik = ?1  and excludeMangkir.id_krs_detail is NULL\n" +
            "group by jd.id_number_elearning\n" +
            "order by jd.id_number_elearning DESC" ,nativeQuery = true)
    Page<StatusImportNilaiDosenIntDto> findStatusImportNilaiDosen(TahunAkademik ta, Pageable page);

    @Query(value = "SELECT kr.nama_karyawan as dosen,jd.id_number_elearning as idNumberElearning,pr.nama_prodi as namaProdi ,mk.kode_matakuliah as kodeMatakuliah,kl.nama_kelas as namaKelas,mk.nama_matakuliah as namaMatakuliah,mk.nama_matakuliah_english as namaMatakuliahEnglish,\n" +
            "CASE\n" +
            "        WHEN min(nilai_tugas) > 0 THEN 'SUDAH'\n" +
            "        WHEN max(nilai_tugas) > 0 and min(nilai_tugas) = 0 THEN 'SUDAH (ada beberapa mahasiswa/i yang masih belum atau tidak dinilai, mohon dicek.)'\n" +
            "        ELSE 'TUGAS BELUM'\n" +
            "    END AS statusImportTugas,\n" +
            "CASE\n" +
            "        WHEN min(nilai_uts) > 0 THEN 'SUDAH'\n" +
            "        WHEN max(nilai_uts) > 0 and min(nilai_uts) = 0 THEN 'SUDAH (ada beberapa mahasiswa/i yang masih belum atau tidak dinilai, mohon dicek.)'\n" +
            "        ELSE 'UTS BELUM'\n" +
            "    END AS statusImportUts,\n" +
            "CASE\n" +
            "        WHEN min(nilai_uas) > 0 THEN 'SUDAH'\n" +
            "        WHEN max(nilai_uas) > 0 and min(nilai_uas) = 0 THEN 'SUDAH (ada beberapa mahasiswa/i yang masih belum atau tidak dinilai, mohon dicek.)'\n" +
            "        ELSE 'UAS BELUM'\n" +
            "    END AS statusImportUas\n" +
            "FROM krs_detail as kd \n" +
            "inner join matakuliah_kurikulum as mku on kd.id_matakuliah_kurikulum = mku.id\n" +
            "inner join matakuliah as mk on mku.id_matakuliah = mk.id\n" +
            "inner join mahasiswa as ma on kd.id_mahasiswa = ma.id\n" +
            "inner join tahun_akademik as ta on kd.id_tahun_akademik = ta.id\n" +
            "inner join jadwal as jd on kd.id_jadwal = jd.id\n" +
            "inner join kelas as kl on jd.id_kelas = kl.id\n" +
            "inner join prodi as pr on jd.id_prodi = pr.id\n" +
            "inner join dosen as ds on jd.id_dosen_pengampu = ds.id\n" +
            "inner join karyawan as kr on ds.id_karyawan = kr.id\n" +
            "left join (SELECT kd.id, pm.id as id_presensi_mahasiswa, jd.id_tahun_akademik, pm.id_krs_detail,pm.id_mahasiswa as idMahasiswa,ma.nim as nim,ma.email_tazkia as email, pm.waktu_masuk, pm.waktu_keluar,pm.status_presensi, pm.status, jd.id as idJadwal,jd.id_number_elearning as idNumberElearning, jd.jam_mulai, jd.jam_selesai,count(pm.id) as absensi\n" +
            "FROM krs_detail as kd\n" +
            "left join presensi_mahasiswa as pm on kd.id = pm.id_krs_detail\n" +
            "inner join sesi_kuliah as sk on pm.id_sesi_kuliah = sk.id\n" +
            "inner join jadwal as jd on sk.id_jadwal = jd.id\n" +
            "inner join mahasiswa as ma on pm.id_mahasiswa = ma.id\n" +
            "where jd.id_tahun_akademik = ?1 and pm.status_presensi IN ('MANGKIR','TERLAMBAT') and pm.status = 'AKTIF' and kd.status = 'AKTIF'\n" +
            "group by pm.id_mahasiswa, jd.id\n" +
            "having count(pm.id) > 3\n" +
            "order by count(pm.id) DESC) as excludeMangkir on kd.id = excludeMangkir.id_krs_detail\n" +
            "where kd.id_tahun_akademik = ?1 and excludeMangkir.id_krs_detail is NULL and kr.nama_karyawan LIKE %?2%\n" +
            "group by jd.id_number_elearning\n" +
            "order by jd.id_number_elearning DESC",countQuery = "SELECT count(kd.id) as jml FROM krs_detail as kd \n" +
            "inner join matakuliah_kurikulum as mku on kd.id_matakuliah_kurikulum = mku.id\n" +
            "inner join matakuliah as mk on mku.id_matakuliah = mk.id\n" +
            "inner join mahasiswa as ma on kd.id_mahasiswa = ma.id\n" +
            "inner join tahun_akademik as ta on kd.id_tahun_akademik = ta.id\n" +
            "inner join jadwal as jd on kd.id_jadwal = jd.id\n" +
            "inner join kelas as kl on jd.id_kelas = kl.id\n" +
            "inner join prodi as pr on jd.id_prodi = pr.id\n" +
            "inner join dosen as ds on jd.id_dosen_pengampu = ds.id\n" +
            "inner join karyawan as kr on ds.id_karyawan = kr.id\n" +
            "left join (SELECT kd.id, pm.id as id_presensi_mahasiswa, jd.id_tahun_akademik, pm.id_krs_detail,pm.id_mahasiswa as idMahasiswa,ma.nim as nim,ma.email_tazkia as email, pm.waktu_masuk, pm.waktu_keluar,pm.status_presensi, pm.status, jd.id as idJadwal,jd.id_number_elearning as idNumberElearning, jd.jam_mulai, jd.jam_selesai,count(pm.id) as absensi\n" +
            "FROM krs_detail as kd\n" +
            "left join presensi_mahasiswa as pm on kd.id = pm.id_krs_detail\n" +
            "inner join sesi_kuliah as sk on pm.id_sesi_kuliah = sk.id\n" +
            "inner join jadwal as jd on sk.id_jadwal = jd.id\n" +
            "inner join mahasiswa as ma on pm.id_mahasiswa = ma.id\n" +
            "where jd.id_tahun_akademik = ?1 and pm.status_presensi IN ('MANGKIR','TERLAMBAT') and pm.status = 'AKTIF' and kd.status = 'AKTIF'\n" +
            "group by pm.id_mahasiswa, jd.id\n" +
            "having count(pm.id) > 3\n" +
            "order by count(pm.id) DESC) as excludeMangkir on kd.id = excludeMangkir.id_krs_detail\n" +
            "where kd.id_tahun_akademik = ?1  and excludeMangkir.id_krs_detail is NULL and kr.nama_karyawan LIKE %?2%\n" +
            "group by jd.id_number_elearning\n" +
            "order by jd.id_number_elearning DESC" ,nativeQuery = true)
    Page<StatusImportNilaiDosenIntDto> findStatusImportNilaiDosenByDosen(TahunAkademik ta, String dosen, Pageable page);


}
