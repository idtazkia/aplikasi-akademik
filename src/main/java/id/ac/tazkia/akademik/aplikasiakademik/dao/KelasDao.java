package id.ac.tazkia.akademik.aplikasiakademik.dao;

import id.ac.tazkia.akademik.aplikasiakademik.entity.Kelas;
import id.ac.tazkia.akademik.aplikasiakademik.entity.Prodi;
import id.ac.tazkia.akademik.aplikasiakademik.entity.StatusRecord;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.PagingAndSortingRepository;

public interface KelasDao extends PagingAndSortingRepository<Kelas,String> {

    Iterable<Kelas> findByStatusNotIn(StatusRecord statusRecord);
    Page<Kelas> findByStatusNotIn(StatusRecord statusRecord,Pageable pageable);
    Page<Kelas> findByStatusAndNamaKelasContainingIgnoreCaseOrderByNamaKelas(StatusRecord statusRecord,String nama,Pageable page);
    Page<Kelas> findByStatusNotInAndNamaKelasContainingIgnoreCaseOrderByNamaKelas(StatusRecord statusRecord,String nama,Pageable page);
    Page<Kelas> findByStatusAndIdProdiAndNamaKelasContainingIgnoreCaseOrderByNamaKelas(StatusRecord status, Prodi prodi, String nama, Pageable page);
}
