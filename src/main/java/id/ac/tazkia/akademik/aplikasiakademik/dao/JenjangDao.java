package id.ac.tazkia.akademik.aplikasiakademik.dao;

import id.ac.tazkia.akademik.aplikasiakademik.entity.Jenjang;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.PagingAndSortingRepository;

public interface JenjangDao extends PagingAndSortingRepository<Jenjang,String> {
    Page<Jenjang> findByStatus(String status, Pageable page);
    Iterable<Jenjang> findByStatusAndNa(String status, String na);
    Iterable<Jenjang> findByStatus(String s);
}
