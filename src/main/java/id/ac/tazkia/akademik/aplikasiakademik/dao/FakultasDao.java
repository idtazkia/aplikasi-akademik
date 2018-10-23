package id.ac.tazkia.akademik.aplikasiakademik.dao;

import id.ac.tazkia.akademik.aplikasiakademik.entity.Fakultas;
import id.ac.tazkia.akademik.aplikasiakademik.entity.StatusRecord;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.PagingAndSortingRepository;

import java.util.List;

public interface FakultasDao extends PagingAndSortingRepository<Fakultas, String> {
    Page<Fakultas> findByStatusNotInAndAndNamaFakultasContainingIgnoreCaseOrderByNamaFakultas(StatusRecord statusRecord,String nama,Pageable page);

    Page<Fakultas> findByStatusNotIn(StatusRecord hapus, Pageable page);
    Iterable<Fakultas> findByStatus(StatusRecord statusRecord);
    List<Fakultas> findByStatusNotIn(StatusRecord statusRecord);
}
