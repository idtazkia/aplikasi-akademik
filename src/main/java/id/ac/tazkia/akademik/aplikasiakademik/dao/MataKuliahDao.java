package id.ac.tazkia.akademik.aplikasiakademik.dao;

import id.ac.tazkia.akademik.aplikasiakademik.entity.MataKuliah;
import id.ac.tazkia.akademik.aplikasiakademik.entity.StatusRecord;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.PagingAndSortingRepository;

public interface MataKuliahDao extends PagingAndSortingRepository<MataKuliah, String> {
    Page<MataKuliah> findByStatusNotIn(StatusRecord status, Pageable page);
}
