package id.ac.tazkia.akademik.aplikasiakademik.dao;

import id.ac.tazkia.akademik.aplikasiakademik.entity.Prodi;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.PagingAndSortingRepository;

import java.util.List;

public interface ProdiDao extends PagingAndSortingRepository<Prodi, String> {
    Page<Prodi> findByStatus(String status, Pageable page);
    List<Prodi> findByStatusAndNa(String status,String na);
}
