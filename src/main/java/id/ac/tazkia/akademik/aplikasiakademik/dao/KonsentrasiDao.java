package id.ac.tazkia.akademik.aplikasiakademik.dao;

import id.ac.tazkia.akademik.aplikasiakademik.entity.Konsentrasi;
import id.ac.tazkia.akademik.aplikasiakademik.entity.Ruangan;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.PagingAndSortingRepository;

public interface KonsentrasiDao extends PagingAndSortingRepository<Konsentrasi, String> {
    Page<Konsentrasi> findByStatus(String status, Pageable page);

    Iterable<Konsentrasi> findByStatus(String aktif);
}