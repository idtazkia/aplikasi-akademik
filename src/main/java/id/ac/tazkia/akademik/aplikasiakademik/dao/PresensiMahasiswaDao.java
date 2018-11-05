package id.ac.tazkia.akademik.aplikasiakademik.dao;

import id.ac.tazkia.akademik.aplikasiakademik.dto.RekapMissAttendance;
import id.ac.tazkia.akademik.aplikasiakademik.entity.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public interface PresensiMahasiswaDao extends PagingAndSortingRepository<PresensiMahasiswa, String> {

    //@Query(
    //        value ="SELECT u FROM PresensiMahasiswa u WHERE u.krsDetail.krs = ?1 and u.status= ?2 and u.statusPresensi not in ('H') group by u.krsDetail",
    // untuk paging       countQuery = "SELECT count(u) FROM PresensiMahasiswa u WHERE u.krsDetail.krs = ?1 and u.status= ?2 and u.statusPresensi not in ('H') group by u.krsDetail")
    //Page<PresensiMahasiswa> findByKrsDetailIdKrsIdAndStatus(Krs krs, StatusRecord statusRecord, Pageable page);

    @Query("select new id.ac.tazkia.akademik.aplikasiakademik.dto.RekapMissAttendance(p.krsDetail.krs.id, p.krsDetail.jadwal.matakuliahKurikulum.matakuliah.namaMatakuliah, count(p)) " +
            "from PresensiMahasiswa p where p.krsDetail.krs = :krs and p.status = 'AKTIF' and p.statusPresensi not in ('HADIR') " +
            "group by p.krsDetail "+
            "order by p.krsDetail.jadwal.matakuliahKurikulum.matakuliah.namaMatakuliah")
    List<RekapMissAttendance> rekapMissAttendance(@Param("krs")Krs krs);

    /*
        @Query(value = "select c.id, f.nama_matakuliah, count(a.id) " +
                "from " +
                "(select * from Presensi_Mahasiswa where status = 'AKTIF' and status_presensi not in ('H'))a inner join " +
                "(select id,id_krs,id_jadwal from krs_detail)b on a.id_krs_detail=b.id inner join " +
                "(select id,id_matakuliah_kurikulum from jadwal)c on b.id_jadwal=c.id inner join" +
                "(select id from krs where id= ?1)d on b.id_krs=d.id inner join "+
                "(select id,id_matakuliah from matakuliah_kurikulum)e on c.id_matakuliah_kurikulum=e.id inner join" +
                "(select * from matakuliah)f on e.id_matakuliah=f.id " +
                "group by a.id_krs_detail "+
                "order by f.nama_matakuliah",
        nativeQuery = true)
        List<RekapMissAttendance> rekapMissAttendance1(Krs krs);
    */


    @Query("select p from PresensiMahasiswa p where p.waktuMasuk >= :mulai and p.waktuMasuk < :sampai")
    Iterable<PresensiMahasiswa> findByTanggal(@Param("mulai") LocalDateTime mulai, @Param("sampai") LocalDateTime sampai);

}
