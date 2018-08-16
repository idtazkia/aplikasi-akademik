package id.ac.tazkia.akademik.aplikasiakademik.dao;

import id.ac.tazkia.akademik.aplikasiakademik.entity.Prodi;
import id.ac.tazkia.akademik.aplikasiakademik.entity.Program;
import id.ac.tazkia.akademik.aplikasiakademik.entity.StatusRecord;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.PagingAndSortingRepository;

import java.util.List;

public interface ProgramDao extends PagingAndSortingRepository<Program,String> {
    Page<Program> findByStatus(StatusRecord aktif, Pageable page);
    List<Program> findByStatus(StatusRecord status);
    List<Program> findByStatusAndIdProgram(StatusRecord status, String id);
//    Page<Program> findbyStatus (String status, Pageable page);
}
