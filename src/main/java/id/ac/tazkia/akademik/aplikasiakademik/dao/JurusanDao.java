package id.ac.tazkia.akademik.aplikasiakademik.dao;

import id.ac.tazkia.akademik.aplikasiakademik.entity.Jurusan;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.PagingAndSortingRepository;


public interface JurusanDao extends PagingAndSortingRepository<Jurusan, String> {
    Page<Jurusan> findByStatus(String status, Pageable page);
    Iterable<Jurusan> findByStatusAndNa(String status, String na);
}
