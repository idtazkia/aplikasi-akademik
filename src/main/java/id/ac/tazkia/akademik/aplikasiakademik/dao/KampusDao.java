package id.ac.tazkia.akademik.aplikasiakademik.dao;


import id.ac.tazkia.akademik.aplikasiakademik.entity.Kampus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.PagingAndSortingRepository;

import java.util.List;

public interface KampusDao extends PagingAndSortingRepository<Kampus, String> {
    Page<Kampus> findByStatus (String status, Pageable page);
    List<Kampus> findByStatus (String status);

}
