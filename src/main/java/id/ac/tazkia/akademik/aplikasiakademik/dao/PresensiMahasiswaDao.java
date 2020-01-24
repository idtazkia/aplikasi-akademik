package id.ac.tazkia.akademik.aplikasiakademik.dao;

import id.ac.tazkia.akademik.aplikasiakademik.dto.AbsenDto;
import id.ac.tazkia.akademik.aplikasiakademik.dto.DetailPresensi;
import id.ac.tazkia.akademik.aplikasiakademik.dto.RekapMissAttendance;
import id.ac.tazkia.akademik.aplikasiakademik.entity.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface PresensiMahasiswaDao extends PagingAndSortingRepository<PresensiMahasiswa, String> {


    @Query("select new id.ac.tazkia.akademik.aplikasiakademik.dto.RekapMissAttendance(p.krsDetail.krs.id, p.krsDetail.jadwal.matakuliahKurikulum.matakuliah.namaMatakuliah, count(p)) " +
            "from PresensiMahasiswa p where p.krsDetail.krs = :krs and p.status = 'AKTIF' and p.statusPresensi not in ('HADIR') " +
            "group by p.krsDetail "+
            "order by p.krsDetail.jadwal.matakuliahKurikulum.matakuliah.namaMatakuliah")
    List<RekapMissAttendance> rekapMissAttendance(@Param("krs")Krs krs);

    @Query("select p from PresensiMahasiswa p where p.waktuMasuk >= :mulai and p.waktuMasuk < :sampai")
    Iterable<PresensiMahasiswa> findByTanggal(@Param("mulai") LocalDateTime mulai, @Param("sampai") LocalDateTime sampai);

    List<PresensiMahasiswa> findBySesiKuliah(SesiKuliah sesiKuliah);
    List<PresensiMahasiswa> findBySesiKuliahAndStatus(SesiKuliah sesiKuliah,StatusRecord statusRecord);
    PresensiMahasiswa findByMahasiswaAndSesiKuliahAndStatus(Mahasiswa mahasiswa,SesiKuliah sesiKuliah,StatusRecord statusRecord);

    List<PresensiMahasiswa> findByKrsDetailAndStatusAndStatusPresensi(KrsDetail krsDetail, StatusRecord status,StatusPresensi statusPresensi);

    @Query("select count (pm.id) from PresensiMahasiswa pm where pm.krsDetail.id = :id and pm.status = :status and pm.statusPresensi = :statusPresensi")
    Long countPresensiMahasiswa (@Param("id")String id,@Param("status")StatusRecord statusRecord,@Param("statusPresensi")StatusPresensi statusPresensi);

    List<PresensiMahasiswa> findByKrsDetailJadwalAndStatus(Jadwal jadwal, StatusRecord statusRecord);

    @Query("select count (*) from PresensiMahasiswa pm where pm.krsDetail = :krsDetail and pm.status = :status and pm.statusPresensi not in (:terlambat,:mangkir)")
    Long hitungAbsen(@Param("krsDetail")KrsDetail krsDetail,@Param("status")StatusRecord statusRecord,@Param("terlambat")StatusPresensi statusPresensi,@Param("mangkir")StatusPresensi mangkir);

    @Query("select count (*) from PresensiMahasiswa pm where pm.krsDetail.id = :krsDetail and pm.status = :status and pm.statusPresensi not in (:terlambat,:mangkir)")
    Long hitungAbsensi(@Param("krsDetail")String krsDetail,@Param("status")StatusRecord statusRecord,@Param("terlambat")StatusPresensi statusPresensi,@Param("mangkir")StatusPresensi mangkir);

    @Query("select new id.ac.tazkia.akademik.aplikasiakademik.dto.AbsenDto(pm.mahasiswa.nim,pm.mahasiswa.nama,pm.krsDetail.kodeUts, 1,0) from PresensiMahasiswa pm where pm.krsDetail.jadwal = :id and pm.status = :status and pm.statusPresensi not in (:terlambat,:mangkir)")
    Page<AbsenDto> cariPresensi(@Param("id")Jadwal krsDetail, @Param("status")StatusRecord statusRecord, @Param("terlambat")StatusPresensi statusPresensi, @Param("mangkir")StatusPresensi mangkir, Pageable page);

    @Query("select new id.ac.tazkia.akademik.aplikasiakademik.dto.AbsenDto(pm.mahasiswa.nim,pm.mahasiswa.nama,'', 1,0) from PresensiMahasiswa pm where pm.krsDetail.jadwal = :id and pm.status = :status and pm.statusPresensi not in (:terlambat,:mangkir)")
    Page<AbsenDto> cariPresensiUas(@Param("id")Jadwal krsDetail, @Param("status")StatusRecord statusRecord, @Param("terlambat")StatusPresensi statusPresensi, @Param("mangkir")StatusPresensi mangkir, Pageable page);

    @Query("select new id.ac.tazkia.akademik.aplikasiakademik.dto.DetailPresensi(pm.waktuMasuk,pm.statusPresensi,pm.sesiKuliah.beritaAcara,pm.sesiKuliah.waktuMulai) from PresensiMahasiswa pm where pm.krsDetail = :kd and pm.status = :status and pm.sesiKuliah.id = :id")
    List<DetailPresensi> detailPresensi(@Param("kd")KrsDetail krsDetail,@Param("status")StatusRecord statusRecord,@Param("id") String id);

    @Query(value = "select count(a.id)as jml_mangkir from presensi_mahasiswa as a inner join sesi_kuliah as b on a.id_sesi_kuliah = b.id inner join presensi_dosen as c on b.id_presensi_dosen = c.id where a.id_mahasiswa=?1 and a.id_krs_detail=?2 and a.status='AKTIF' and c.status='AKTIF' and a.status_presensi in ('MANGKIR','TERLAMBAT')", nativeQuery = true)
    Long jumlahMangkir(Mahasiswa mahasiswa, KrsDetail krsDetail);

    Long countByStatusPresensiNotInAndKrsDetailAndStatus(List<StatusPresensi> statusPresensi,KrsDetail krsDetail,StatusRecord statusRecord);

    Long countByStatusAndSesiKuliah_PresensiDosen_StatusAndStatusPresensiNotInAndMahasiswaAndKrsDetailJadwalMatakuliahKurikulumMatakuliahKodeMatakuliahContainingIgnoreCase(StatusRecord statusRecord,StatusRecord statusDosen, List <StatusPresensi> statusPresensis,Mahasiswa mahasiswa, String sds);


}
