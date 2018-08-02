package id.ac.tazkia.akademik.aplikasiakademik.dao;

import id.ac.tazkia.akademik.aplikasiakademik.entity.Program;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.PagingAndSortingRepository;

public interface ProgramDao extends PagingAndSortingRepository<Program,String> {
    Page<Program> findByStatus(String aktif, Pageable page);

//    Page<Program> findbyStatus (String status, Pageable page);

}
