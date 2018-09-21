package id.ac.tazkia.akademik.aplikasiakademik.dao;

import id.ac.tazkia.akademik.aplikasiakademik.entity.Lembaga;
import id.ac.tazkia.akademik.aplikasiakademik.entity.StatusRecord;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.PagingAndSortingRepository;

public interface LembagaDao extends PagingAndSortingRepository<Lembaga, String> {
    Page<Lembaga> findByStatusNotInAndNamaLembagaContainingIgnoreCaseOrderByNamaLembaga(StatusRecord statusRecord, String nama,Pageable page);
    Page<Lembaga> findByStatusNotIn(StatusRecord statusRecord,Pageable page);

}
