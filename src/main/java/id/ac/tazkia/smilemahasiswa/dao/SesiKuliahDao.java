package id.ac.tazkia.smilemahasiswa.dao;

import id.ac.tazkia.smilemahasiswa.entity.*;
import org.hibernate.sql.Update;
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

//    @Query(value = "SELECT COUNT(*) as kelasPengganti, COUNT(*) * 100 / (SELECT COUNT(*) as semuaKelas\n" +
//            "\tFROM sesi_kuliah as sk\n" +
//            "\tINNER JOIN jadwal as jd on sk.id_jadwal = jd.id\n" +
//            "\twhere jd.id_tahun_akademik = ?1)\n" +
//            "as persentase\n" +
//            "FROM sesi_kuliah as sk\n" +
//            "INNER JOIN jadwal as jd on sk.id_jadwal = jd.id\n" +
//            "where jd.id_tahun_akademik = ?1 and sk.pertemuan NOT IN ('Online', 'Offline', null)", nativeQuery = true)
//    Object jadwalKelasPengganti(String tahunAkademik,String pertemuan);

    @Query(value = "SELECT COUNT(*) as kelasOnline, COUNT(*) * 100 / (SELECT COUNT(*) as semuaKelas\n" +
            "\tFROM sesi_kuliah as sk\n" +
            "\tINNER JOIN jadwal as jd on sk.id_jadwal = jd.id\n" +
            "\twhere jd.id_tahun_akademik = ?1)\n" +
            "as persentase\n" +
            "FROM sesi_kuliah as sk\n" +
            "INNER JOIN jadwal as jd on sk.id_jadwal = jd.id\n" +
            "where jd.id_tahun_akademik = ?1 and sk.pertemuan = ?2", nativeQuery = true)
    Object jmlKelasOnlineOffline(TahunAkademik tahunAkademik, String pertemuan);


    @Query(value = "SELECT sk.* FROM sesi_kuliah as sk inner join presensi_dosen as pd on sk.id_presensi_dosen = pd.id where sk.id_jadwal = ?1 " +
            " and pd.status = 'AKTIF' and sk.pertemuan in ('Offline','Online') and pd.waktu_masuk = now() order by sk.waktu_mulai desc limit 1", nativeQuery = true)
    SesiKuliah cariSesiKuliahHariIni(Jadwal jadwal);

    Optional<SesiKuliah> findByJadwalAndPertemuanKe(Jadwal jadwal, Integer pertemuan);

    List<SesiKuliah> findByJadwalAndPresensiDosenStatusAndRps(Jadwal jadwal, StatusRecord statusRecord, String rps);

    List<SesiKuliah> findByJadwalAndPresensiDosenStatusAndRpsIsNull(Jadwal jadwal, StatusRecord statusRecord);

    @Modifying
    @Query(value = "update sesi_kuliah set rps = '' where id_jadwal = ?1", nativeQuery = true)
    void updateRps(String idJadwal);

}
