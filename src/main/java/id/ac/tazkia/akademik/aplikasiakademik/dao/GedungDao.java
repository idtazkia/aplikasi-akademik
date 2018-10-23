package id.ac.tazkia.akademik.aplikasiakademik.dao;


import id.ac.tazkia.akademik.aplikasiakademik.entity.Gedung;
import id.ac.tazkia.akademik.aplikasiakademik.entity.StatusRecord;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.PagingAndSortingRepository;

public interface GedungDao extends PagingAndSortingRepository<Gedung,String> {
    Iterable<Gedung> findByStatus(StatusRecord statusRecord);
    Page<Gedung> findByStatusNotIn(StatusRecord statusRecord,Pageable page);
    Page<Gedung> findByStatusNotInAndAndNamaGedungContainingIgnoreCaseOrderByNamaGedung(StatusRecord statusRecord, String search,Pageable pageable);

}
