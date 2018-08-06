package id.ac.tazkia.akademik.aplikasiakademik.dao;

import id.ac.tazkia.akademik.aplikasiakademik.entity.Fakultas;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.PagingAndSortingRepository;

public interface FakultasDao extends PagingAndSortingRepository<Fakultas, String> {
    Page<Fakultas> findByStatus(String status, Pageable page);
    Iterable<Fakultas> findByStatusAndNa(String status, String na);
}
