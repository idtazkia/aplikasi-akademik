package id.ac.tazkia.akademik.aplikasiakademik.dao;

import id.ac.tazkia.akademik.aplikasiakademik.entity.Ruangan;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.PagingAndSortingRepository;

public interface RuanganDao extends PagingAndSortingRepository<Ruangan, String> {
    Page<Ruangan> findByStatus(String status, Pageable page);
}