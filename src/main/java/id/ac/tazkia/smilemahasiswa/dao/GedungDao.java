package id.ac.tazkia.smilemahasiswa.dao;

import id.ac.tazkia.smilemahasiswa.entity.Gedung;
import id.ac.tazkia.smilemahasiswa.entity.StatusRecord;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.PagingAndSortingRepository;

import java.util.List;

public interface GedungDao extends PagingAndSortingRepository<Gedung,String> {
    Page<Gedung> findByStatusNotInAndAndNamaGedungContainingIgnoreCaseOrderByNamaGedung(List<StatusRecord> asList, String search, Pageable page);

    Page<Gedung> findByStatusNotIn(List<StatusRecord> asList, Pageable page);
}
