package id.ac.tazkia.smilemahasiswa.dao;

import id.ac.tazkia.smilemahasiswa.entity.Kelas;
import id.ac.tazkia.smilemahasiswa.entity.StatusRecord;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.PagingAndSortingRepository;

import java.util.List;

public interface KelasDao extends PagingAndSortingRepository<Kelas,String> {
    Page<Kelas> findByStatusNotInAndNamaKelasContainingIgnoreCaseOrderByNamaKelas(List<StatusRecord> asList, String search, Pageable page);

    Page<Kelas> findByStatusNotIn(List<StatusRecord> asList, Pageable page);
}
