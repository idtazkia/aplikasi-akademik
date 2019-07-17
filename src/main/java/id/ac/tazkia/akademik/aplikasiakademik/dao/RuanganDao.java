package id.ac.tazkia.akademik.aplikasiakademik.dao;

import id.ac.tazkia.akademik.aplikasiakademik.entity.Ruangan;
import id.ac.tazkia.akademik.aplikasiakademik.entity.StatusRecord;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.PagingAndSortingRepository;

import java.util.List;

public interface RuanganDao extends PagingAndSortingRepository<Ruangan, String> {
    Page<Ruangan> findByStatusNotInAndAndNamaRuanganContainingIgnoreCaseOrderByNamaRuangan(StatusRecord statusRecord,String search,Pageable page);

    Page<Ruangan> findByStatusNotIn(StatusRecord hapus, Pageable page);
    Page<Ruangan> findByStatusAndNamaRuanganContainingIgnoreCase(StatusRecord statusRecord, String nama,Pageable page);
    List<Ruangan> findByStatusAndNamaRuanganContainingIgnoreCase(StatusRecord statusRecord, String nama);
    Iterable<Ruangan> findByStatusNotIn(StatusRecord hapus);
    Iterable<Ruangan> findByStatus(StatusRecord hapus);
}