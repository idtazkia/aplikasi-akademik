package id.ac.tazkia.smilemahasiswa.dao;

import id.ac.tazkia.smilemahasiswa.entity.Jurusan;
import id.ac.tazkia.smilemahasiswa.entity.StatusRecord;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.PagingAndSortingRepository;

import java.util.List;

public interface JurusanDao extends PagingAndSortingRepository<Jurusan,String> {
    Page<Jurusan> findByStatusNotInAndNamaJurusanContainingIgnoreCaseOrderByNamaJurusan(List<StatusRecord> asList, String search, Pageable page);

    Page<Jurusan> findByStatusNotIn(List<StatusRecord> asList, Pageable page);

    List<Jurusan> findByStatus(StatusRecord aktif);
}
