package id.ac.tazkia.akademik.aplikasiakademik.dao;

import id.ac.tazkia.akademik.aplikasiakademik.entity.Mahasiswa;
import id.ac.tazkia.akademik.aplikasiakademik.entity.StatusRecord;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.PagingAndSortingRepository;

public interface MahasiswaDao extends PagingAndSortingRepository<Mahasiswa,String> {
    Page<Mahasiswa> findByStatusNotInAndNamaContainingIgnoreCaseOrNimContainingIgnoreCaseOrderByNama(StatusRecord statusRecord, String status,String nim, Pageable page);

    Page<Mahasiswa> findByStatusNotIn(StatusRecord statusRecord,Pageable page);
}
