package id.ac.tazkia.akademik.aplikasiakademik.dao;


import id.ac.tazkia.akademik.aplikasiakademik.entity.Gedung;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.PagingAndSortingRepository;

public interface GedungDao extends PagingAndSortingRepository<Gedung,String> {
    Page<Gedung> findByStatus (String status, Pageable page);
}
