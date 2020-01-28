package id.ac.tazkia.smilemahasiswa.dao;

import id.ac.tazkia.smilemahasiswa.entity.Fakultas;
import id.ac.tazkia.smilemahasiswa.entity.StatusRecord;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.PagingAndSortingRepository;

import java.util.List;

public interface FakultasDao extends PagingAndSortingRepository<Fakultas,String> {
    Page<Fakultas> findByStatusNotInAndAndNamaFakultasContainingIgnoreCaseOrderByNamaFakultas(List<StatusRecord> asList, String search, Pageable page);

    Page<Fakultas> findByStatusNotIn(List<StatusRecord> asList, Pageable page);

    List<Fakultas> findByStatus(StatusRecord aktif);
}
