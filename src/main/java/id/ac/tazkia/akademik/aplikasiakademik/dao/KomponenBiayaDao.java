package id.ac.tazkia.akademik.aplikasiakademik.dao;

import id.ac.tazkia.akademik.aplikasiakademik.entity.KomponenBiaya;
import id.ac.tazkia.akademik.aplikasiakademik.entity.StatusRecord;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.PagingAndSortingRepository;

public interface KomponenBiayaDao extends PagingAndSortingRepository<KomponenBiaya, String> {
    Page<KomponenBiaya> findByStatusNotInOrderByNamaAsc(StatusRecord status, Pageable page);
    Page<KomponenBiaya> findByStatusNotInAndNamaContainingIgnoreCaseOrderByNamaAsc(StatusRecord status,String nama, Pageable page);

    Iterable<KomponenBiaya> findByStatusNotIn(StatusRecord hapus);
}
