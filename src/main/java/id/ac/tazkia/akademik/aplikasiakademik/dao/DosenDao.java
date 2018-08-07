package id.ac.tazkia.akademik.aplikasiakademik.dao;


import id.ac.tazkia.akademik.aplikasiakademik.entity.Dosen;
import id.ac.tazkia.akademik.aplikasiakademik.entity.Gedung;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.PagingAndSortingRepository;

public interface DosenDao extends PagingAndSortingRepository<Dosen, String> {
    Page<Dosen> findByStatus (String status, Pageable page);

}
