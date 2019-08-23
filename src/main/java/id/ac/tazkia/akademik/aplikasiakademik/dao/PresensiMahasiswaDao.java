package id.ac.tazkia.akademik.aplikasiakademik.dao;

import id.ac.tazkia.akademik.aplikasiakademik.dto.RekapMissAttendance;
import id.ac.tazkia.akademik.aplikasiakademik.entity.*;
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

    List<PresensiMahasiswa> findByKrsDetailAndStatusAndStatusPresensi(KrsDetail krsDetail, StatusRecord status,StatusPresensi statusPresensi);

    List<PresensiMahasiswa> findByKrsDetailJadwalAndStatus(Jadwal jadwal, StatusRecord statusRecord);

}
