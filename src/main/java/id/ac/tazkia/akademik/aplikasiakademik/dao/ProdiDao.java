package id.ac.tazkia.akademik.aplikasiakademik.dao;

import id.ac.tazkia.akademik.aplikasiakademik.entity.Prodi;
import id.ac.tazkia.akademik.aplikasiakademik.entity.StatusRecord;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.PagingAndSortingRepository;

import java.util.List;

public interface ProdiDao extends PagingAndSortingRepository<Prodi, String> {
    Page<Prodi> findByStatusNotInAndAndNamaProdiContainingIgnoreCaseOrderByNamaProdi(StatusRecord status,String nama,Pageable page);
    List<Prodi> findByStatus(StatusRecord status);
    List<Prodi> findByStatusAndIdProdi(StatusRecord status, String id);
    Page<Prodi> findByStatusNotIn(StatusRecord statusRecord,Pageable page);
}
