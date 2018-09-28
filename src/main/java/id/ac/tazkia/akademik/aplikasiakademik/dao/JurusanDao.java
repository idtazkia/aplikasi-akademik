package id.ac.tazkia.akademik.aplikasiakademik.dao;

import id.ac.tazkia.akademik.aplikasiakademik.entity.Jurusan;
import id.ac.tazkia.akademik.aplikasiakademik.entity.StatusRecord;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.PagingAndSortingRepository;

import java.util.List;


public interface JurusanDao extends PagingAndSortingRepository<Jurusan, String> {
    List<Jurusan> findByStatus(StatusRecord statusRecord);
    Page<Jurusan> findByStatusNotIn(StatusRecord statusRecord,Pageable page);
    Page<Jurusan> findByStatusAndNamaJurusanContainingIgnoreCaseOrderByNamaJurusan(StatusRecord statusRecord, String nama, Pageable page);
    Page<Jurusan> findByStatusNotInAndNamaJurusanContainingIgnoreCaseOrderByNamaJurusan(StatusRecord statusRecord, String nama, Pageable page);


}
