package id.ac.tazkia.akademik.aplikasiakademik.dao;

import id.ac.tazkia.akademik.aplikasiakademik.entity.Konsentrasi;
import id.ac.tazkia.akademik.aplikasiakademik.entity.StatusRecord;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.PagingAndSortingRepository;

public interface KonsentrasiDao extends PagingAndSortingRepository<Konsentrasi, String> {
    Page<Konsentrasi> findByStatusNotIn(StatusRecord status, Pageable page);
    Page<Konsentrasi> findByStatusNotInAndAndNamaKonsentrasiContainingIgnoreCaseOrderByNamaKonsentrasi(StatusRecord status, String nama, Pageable page);
}