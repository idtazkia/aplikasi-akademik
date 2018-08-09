package id.ac.tazkia.akademik.aplikasiakademik.dao;

import id.ac.tazkia.akademik.aplikasiakademik.entity.MhswAkademik;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.PagingAndSortingRepository;

public interface MahasiswaAkademikDao extends PagingAndSortingRepository<MhswAkademik, String> {
    Page<MhswAkademik> findByStatus(String s, Pageable page);
}
