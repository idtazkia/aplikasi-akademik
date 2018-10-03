package id.ac.tazkia.akademik.aplikasiakademik.dao;

import id.ac.tazkia.akademik.aplikasiakademik.entity.Mahasiswa;
import id.ac.tazkia.akademik.aplikasiakademik.entity.StatusRecord;
import id.ac.tazkia.akademik.aplikasiakademik.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.PagingAndSortingRepository;

public interface MahasiswaDao extends PagingAndSortingRepository<Mahasiswa,String> {
    Mahasiswa findByUser(User u);

    Page<Mahasiswa> findByStatusNotInAndNamaContainingIgnoreCaseOrNimContainingIgnoreCaseOrderByNama(StatusRecord statusRecord, String status,String nim, Pageable page);

    Page<Mahasiswa> findByStatusNotIn(StatusRecord statusRecord,Pageable page);
}
