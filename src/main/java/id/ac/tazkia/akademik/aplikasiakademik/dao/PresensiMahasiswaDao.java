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
    @Query(value = "SELECT u FROM PresensiMahasiswa u where u.idMahasiswa= :idmahasiswa")
    List<PresensiMahasiswa> findAllPresensi(@Param("idmahasiswa")Mahasiswa mahasiswa);

    List<PresensiMahasiswa> countAllByIdKrsDetailAndStatusPresensiNotInAndStatusNotIn(KrsDetail krsDetail,String statusPresensi,StatusRecord statusRecord);

}
