package id.ac.tazkia.akademik.aplikasiakademik.dao;

import id.ac.tazkia.akademik.aplikasiakademik.entity.StatusRecord;
import id.ac.tazkia.akademik.aplikasiakademik.entity.TahunAkademik;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.PagingAndSortingRepository;

import java.util.List;

public interface TahunAkademikDao extends PagingAndSortingRepository<TahunAkademik, String> {
    Page<TahunAkademik> findByStatusNotInOrderByKodeTahunAkademikDesc(StatusRecord status, Pageable page);
    Iterable<TahunAkademik> findByStatusNotInOrderByKodeTahunAkademikDesc(StatusRecord status);
    TahunAkademik findByStatus(StatusRecord status);
    Page<TahunAkademik> findByStatusNotInAndNamaTahunAkademikContainingIgnoreCaseOrderByKodeTahunAkademikDesc(StatusRecord status,String search, Pageable page);
    Iterable<TahunAkademik> findByStatusNotIn(StatusRecord statusRecord);
    Iterable<TahunAkademik> findByStatusNotInOrderByTahunDesc(StatusRecord statusRecord);
    Iterable<TahunAkademik> findByStatusNotInOrderByNamaTahunAkademikDesc(StatusRecord statusRecord);
}
