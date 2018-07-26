package id.ac.tazkia.akademik.aplikasiakademik.dao;

import id.ac.tazkia.akademik.aplikasiakademik.entity.Lembaga;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.PagingAndSortingRepository;

public interface LembagaDao extends PagingAndSortingRepository<Lembaga, String> {
    Page<Lembaga> findByStatus (String status, Pageable page);
}
