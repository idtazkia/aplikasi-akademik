package id.ac.tazkia.akademik.aplikasiakademik.dao;

import id.ac.tazkia.akademik.aplikasiakademik.entity.Kurikulum;
import id.ac.tazkia.akademik.aplikasiakademik.entity.Prodi;
import id.ac.tazkia.akademik.aplikasiakademik.entity.StatusRecord;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Persistable;
import org.springframework.data.repository.PagingAndSortingRepository;

import java.util.List;

public interface KurikulumDao extends PagingAndSortingRepository<Kurikulum, String> {

    Page<Kurikulum> findByStatusNotInAndProdi(StatusRecord status,Prodi prodi, Pageable page);
    Page<Kurikulum> findByStatus(StatusRecord status,Pageable page);
    Page<Kurikulum> findByStatusAndNamaKurikulumContainingIgnoreCaseOrderByNamaKurikulum(StatusRecord status,String search, Pageable page);
    Kurikulum findByProdiAndStatus(Prodi prodi, StatusRecord statusRecord);
    List<Kurikulum> findByStatusNotIn(StatusRecord statusRecord);
    List<Kurikulum> findByProdiAndNamaKurikulumContainingIgnoreCaseOrderByNamaKurikulum(Prodi prodi, String search);

}
