package id.ac.tazkia.smilemahasiswa.dao;

import id.ac.tazkia.smilemahasiswa.entity.Gedung;
import id.ac.tazkia.smilemahasiswa.entity.Ruangan;
import id.ac.tazkia.smilemahasiswa.entity.StatusRecord;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.PagingAndSortingRepository;

import java.util.List;

public interface RuanganDao extends PagingAndSortingRepository<Ruangan, String> {
    Iterable<Ruangan> findByStatus(StatusRecord aktif);

    Iterable<Ruangan> findByStatusAndGedung(StatusRecord aktif, Gedung gedung);

    Page<Ruangan> findByStatusNotInAndAndNamaRuanganContainingIgnoreCaseOrderByNamaRuangan(List<StatusRecord> asList, String search, Pageable page);

    Page<Ruangan> findByStatusNotIn(List<StatusRecord> asList, Pageable page);
}
