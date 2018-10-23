package id.ac.tazkia.akademik.aplikasiakademik.dao;

import id.ac.tazkia.akademik.aplikasiakademik.dto.RekapMissAttendance;
import id.ac.tazkia.akademik.aplikasiakademik.entity.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface PresensiMahasiswaDao extends PagingAndSortingRepository<PresensiMahasiswa, String> {

    //@Query(
    //        value ="SELECT u FROM PresensiMahasiswa u WHERE u.krsDetail.krs = ?1 and u.status= ?2 and u.statusPresensi not in ('H') group by u.krsDetail",
    // untuk paging       countQuery = "SELECT count(u) FROM PresensiMahasiswa u WHERE u.krsDetail.krs = ?1 and u.status= ?2 and u.statusPresensi not in ('H') group by u.krsDetail")
    //Page<PresensiMahasiswa> findByKrsDetailIdKrsIdAndStatus(Krs krs, StatusRecord statusRecord, Pageable page);

    @Query("select new id.ac.tazkia.akademik.aplikasiakademik.dto.RekapMissAttendance(p.krsDetail.krs.id, p.krsDetail.jadwal.matakuliahKurikulum.matakuliah.namaMatakuliah, count(p)) " +
            "from PresensiMahasiswa p where p.krsDetail.krs = :krs and p.status = 'AKTIF' and p.statusPresensi not in ('H') " +
            "group by p.krsDetail "+
            "order by p.krsDetail.jadwal.matakuliahKurikulum.matakuliah.namaMatakuliah")
    List<RekapMissAttendance> rekapMissAttendance(@Param("krs")Krs krs);



}
