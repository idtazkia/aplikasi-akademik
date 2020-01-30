package id.ac.tazkia.smilemahasiswa.dao;

import id.ac.tazkia.smilemahasiswa.entity.Program;
import id.ac.tazkia.smilemahasiswa.entity.StatusRecord;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.PagingAndSortingRepository;

import java.util.List;

public interface ProgramDao extends PagingAndSortingRepository<Program,String> {
    Page<Program> findByStatusNotInAndNamaProgramContainingIgnoreCaseOrderByNamaProgram(List<StatusRecord> asList, String search, Pageable page);

    Page<Program> findByStatusNotIn(List<StatusRecord> asList, Pageable page);

    List<Program> findByStatus(StatusRecord aktif);

    Iterable<Program> findByStatusNotIn(List<StatusRecord> asList);
}
