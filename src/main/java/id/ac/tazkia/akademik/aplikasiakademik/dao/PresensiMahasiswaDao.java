package id.ac.tazkia.akademik.aplikasiakademik.dao;

import id.ac.tazkia.akademik.aplikasiakademik.entity.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface PresensiMahasiswaDao extends PagingAndSortingRepository<PresensiMahasiswa, String> {


    @Query(
            value ="SELECT u FROM PresensiMahasiswa u WHERE u.krsDetail.krs = ?1 and u.status= ?2 and u.statusPresensi not in ('H') group by u.krsDetail",
            countQuery = "SELECT count(u) FROM PresensiMahasiswa u WHERE u.krsDetail.krs = ?1 and u.status= ?2 and u.statusPresensi not in ('H') group by u.krsDetail")
    Page<PresensiMahasiswa> findByKrsDetailIdKrsIdAndStatus(Krs krs, StatusRecord statusRecord, Pageable page);

    @Query(
            value = "SELECT * FROM presensi_mahasiswa  WHERE id_krs_detail = ?1 and status= ?2 and status_presensi not in ('H') group by id_krs_detail",
            countQuery = "SELECT count(*) FROM presensi_mahasiswa  WHERE id_krs_detail.id_krs = ?1 and status= ?2 and status_presensi not in ('H') group by id_krs_detail",
            nativeQuery = true)
    List<PresensiMahasiswa> findByKrsDetailIdKrsIdAndStatus(Krs krs, StatusRecord statusRecord);
}
