package id.ac.tazkia.smilemahasiswa.dao;

import id.ac.tazkia.smilemahasiswa.entity.*;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.PagingAndSortingRepository;

import java.util.List;
import java.util.Optional;

public interface SesiKuliahDao extends PagingAndSortingRepository<SesiKuliah, String> {
    List<SesiKuliah> findByJadwalAndPresensiDosenStatusOrderByWaktuMulai(Jadwal jadwal, StatusRecord aktif);

    SesiKuliah findByPresensiDosen(PresensiDosen pd);

    @Query(value = "select * from sesi_kuliah as sk inner join presensi_dosen as pd on sk.id_presensi_dosen=pd.id inner join jadwal as j on pd.id_jadwal=j.id where " +
            "j.id=?1 and date(sk.waktu_mulai)=?2 and sk.pertemuan = 'Online' or sk.pertemuan = 'Offline' order by sk.waktu_mulai", nativeQuery = true)
    SesiKuliah cariPresensiHariIni(String idJadwal, String hariIni);

    @Query(value = "SELECT COUNT(*) as kelasOnline, COUNT(*) * 100 / (SELECT COUNT(*)\n" +
            "\tFROM sesi_kuliah as sk\n" +
            "\tINNER JOIN jadwal as jd on sk.id_jadwal = jd.id\n" +
            "\tINNER JOIN presensi_dosen as pd on sk.id_presensi_dosen = pd.id\n" +
            "\twhere pd.id_tahun_akademik = ?1 \n" +
            "\tand pd.waktu_masuk < CURDATE() and sk.waktu_mulai >= '2023-09-11')\n" +
            "as persentase\n" +
            "FROM sesi_kuliah as sk\n" +
            "INNER JOIN presensi_dosen as pd on sk.id_presensi_dosen = pd.id\n" +
            "INNER JOIN jadwal as jd on sk.id_jadwal = jd.id\n" +
            "where jd.id_tahun_akademik = ?1 and pd.waktu_masuk < CURDATE() and sk.waktu_mulai >= '2023-09-11' \n" +
            "and sk.pertemuan in ('Online','Pengganti Online')", nativeQuery = true)
    Object jmlKelasOnline(TahunAkademik tahunAkademik);

    @Query(value = "SELECT COUNT(*) as kelasOnline, COUNT(*) * 100 / (SELECT COUNT(*)\n" +
            "\tFROM sesi_kuliah as sk\n" +
            "\tINNER JOIN jadwal as jd on sk.id_jadwal = jd.id\n" +
            "\tINNER JOIN presensi_dosen as pd on sk.id_presensi_dosen = pd.id\n" +
            "\twhere pd.id_tahun_akademik = 'f635cb68-3da5-4e11-8213-7020636034a2' \n" +
            "\tand pd.waktu_masuk < CURDATE() and sk.waktu_mulai >= '2023-09-11')\n" +
            "as persentase\n" +
            "FROM sesi_kuliah as sk\n" +
            "INNER JOIN presensi_dosen as pd on sk.id_presensi_dosen = pd.id\n" +
            "INNER JOIN jadwal as jd on sk.id_jadwal = jd.id\n" +
            "where jd.id_tahun_akademik = 'f635cb68-3da5-4e11-8213-7020636034a2' and pd.waktu_masuk < CURDATE() and sk.waktu_mulai >= '2023-09-11' \n" +
            "and sk.pertemuan in ('Offline','Pengganti Offline')", nativeQuery = true)
    Object jmlKelasOffline(TahunAkademik tahunAkademik);


    @Query(value = "SELECT sk.* FROM sesi_kuliah as sk inner join presensi_dosen as pd on sk.id_presensi_dosen = pd.id where sk.id_jadwal = ?1 " +
            " and pd.status = 'AKTIF' and sk.pertemuan in ('Offline','Online') and pd.waktu_masuk = now() order by sk.waktu_mulai desc limit 1", nativeQuery = true)
    SesiKuliah cariSesiKuliahHariIni(Jadwal jadwal);

    Optional<SesiKuliah> findByJadwalAndPertemuanKe(Jadwal jadwal, Integer pertemuan);

    List<SesiKuliah> findByJadwalAndPresensiDosenStatusAndRps(Jadwal jadwal, StatusRecord statusRecord, String rps);

    List<SesiKuliah> findByJadwalAndPresensiDosenStatusNotInAndRpsIsNull(Jadwal jadwal, List<StatusRecord> status);

    @Modifying
    @Query(value = "update sesi_kuliah set rps = '' where id_jadwal = ?1", nativeQuery = true)
    void updateRps(String idJadwal);

    @Query(value = "SELECT COUNT(*) as semuaKelas\n" +
            "FROM sesi_kuliah as sk\n" +
            "INNER JOIN jadwal as jd on sk.id_jadwal = jd.id\n" +
            "INNER JOIN presensi_dosen as pd on sk.id_presensi_dosen = pd.id\n" +
            "where pd.id_tahun_akademik = ?1 \n" +
            "and pd.waktu_masuk < CURDATE() and sk.waktu_mulai >= '2023-09-11'", nativeQuery = true)
    Object jmlSemuaKelas(TahunAkademik tahunAkademik);

    @Query(value = "SELECT COUNT(*) as jumlahKelasKosong, COUNT(*) * 100 / (SELECT COUNT(*)\n" +
            "\tFROM sesi_kuliah as sk\n" +
            "\tINNER JOIN jadwal as jd on sk.id_jadwal = jd.id\n" +
            "\tINNER JOIN presensi_dosen as pd on sk.id_presensi_dosen = pd.id\n" +
            "\twhere pd.id_tahun_akademik = ?1 \n" +
            "\tand pd.waktu_masuk < CURDATE() and sk.waktu_mulai >= '2023-09-11')\n" +
            "as persentase\n" +
            "FROM sesi_kuliah as sk\n" +
            "inner join presensi_dosen as pd on sk.id_presensi_dosen = pd.id\n" +
            "INNER JOIN jadwal as jd on sk.id_jadwal = jd.id\n" +
            "INNER JOIN prodi as pr on jd.id_prodi = pr.id\n" +
            "INNER JOIN kelas as kl on jd.id_kelas = kl.id\n" +
            "INNER JOIN matakuliah_kurikulum as mku on jd.id_matakuliah_kurikulum = mku.id\n" +
            "INNER JOIN matakuliah as mk on mku.id_matakuliah = mk.id\n" +
            "INNER JOIN dosen as ds on pd.id_dosen = ds.id\n" +
            "INNER JOIN karyawan as kr on ds.id_karyawan = kr.id\n" +
            "where pd.id_tahun_akademik = ?1 and pd.status_presensi in ('BELUM_MENGAJAR','TERLAMBAT')\n" +
            "and mk.nama_matakuliah not in ('Skripsi','Tesis','Magang','Student Dynamic Session','Ujian Proposal','Ujian Tesis') \n" +
            "and pd.waktu_masuk < CURDATE() and sk.waktu_mulai >= '2023-09-11'\n" +
            "and (sk.rps = '' or sk.rps is null)\n" +
            "ORDER BY sk.waktu_mulai ASC\n", nativeQuery = true)
    Object jmlKelasKosong(TahunAkademik tahunAkademik);

    @Query(value = "select * from sesi_kuliah as sk inner join presensi_dosen as pd on sk.id_presensi_dosen = pd.id\n" +
            "where date(sk.waktu_mulai)= date(now()) and pd.status not in ('HAPUS') and pd.status_presensi = 'BELUM_MENGAJAR'", nativeQuery = true)
    List<SesiKuliah> cariBelumMengajar();

}
