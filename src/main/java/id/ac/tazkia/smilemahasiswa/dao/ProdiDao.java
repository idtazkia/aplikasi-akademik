package id.ac.tazkia.smilemahasiswa.dao;

import id.ac.tazkia.smilemahasiswa.entity.Prodi;
import id.ac.tazkia.smilemahasiswa.entity.StatusRecord;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.PagingAndSortingRepository;

import java.util.List;

public interface ProdiDao extends PagingAndSortingRepository<Prodi,String> {
    List<Prodi> findByStatus(StatusRecord aktif);

    List<Prodi> findByStatusIn(List<StatusRecord> status);

    List<Prodi> findByStatusOrderByNamaProdi(StatusRecord statusRecord);

    Prodi findByKodeSpmb(String kode);

    List<Prodi> findByStatusNotIn(List<StatusRecord> asList);
    Page<Prodi> findByStatusNotIn(List<StatusRecord> asList,Pageable page);

    Page<Prodi> findByStatus(StatusRecord aktif, Pageable page);

    Page<Prodi> findByStatusAndNamaProdiContainingIgnoreCaseOrderByNamaProdi(StatusRecord aktif, String search, Pageable page);

    Page<Prodi> findByStatusNotInAndAndNamaProdiContainingIgnoreCaseOrderByNamaProdi(List<StatusRecord> asList, String search, Pageable page);
}
