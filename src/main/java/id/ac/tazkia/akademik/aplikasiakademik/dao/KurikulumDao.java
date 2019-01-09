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
    Kurikulum findByProdiAndStatus(Prodi prodi, StatusRecord statusRecord);
    List<Kurikulum> findByStatusNotIn(StatusRecord statusRecord);

}
