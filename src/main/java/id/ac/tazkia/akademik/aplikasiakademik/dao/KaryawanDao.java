package id.ac.tazkia.akademik.aplikasiakademik.dao;

import id.ac.tazkia.akademik.aplikasiakademik.entity.Karyawan;
import id.ac.tazkia.akademik.aplikasiakademik.entity.StatusRecord;
import id.ac.tazkia.akademik.aplikasiakademik.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.PagingAndSortingRepository;

import java.util.List;

public interface KaryawanDao extends PagingAndSortingRepository<Karyawan,String> {

    Page<Karyawan> findByStatus(StatusRecord status, Pageable page);
    Page<Karyawan> findByStatusAndNamaKaryawanContainingIgnoreCaseOrderByNamaKaryawan(StatusRecord statusRecord, String search, Pageable page);

}
