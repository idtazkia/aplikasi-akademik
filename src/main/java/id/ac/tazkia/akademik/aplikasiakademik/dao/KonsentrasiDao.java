package id.ac.tazkia.akademik.aplikasiakademik.dao;

import id.ac.tazkia.akademik.aplikasiakademik.entity.Fakultas;
import id.ac.tazkia.akademik.aplikasiakademik.entity.Konsentrasi;
import id.ac.tazkia.akademik.aplikasiakademik.entity.Prodi;
import id.ac.tazkia.akademik.aplikasiakademik.entity.StatusRecord;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.PagingAndSortingRepository;

public interface KonsentrasiDao extends PagingAndSortingRepository<Konsentrasi, String> {
    Page<Konsentrasi> findByStatusNotInAndAndNamaKonsentrasiContainingIgnoreCaseOrderByNamaKonsentrasi(StatusRecord statusRecord, String nama, Pageable page);
    Page<Konsentrasi> findByStatusNotIn(StatusRecord hapus, Pageable page);
    Iterable<Konsentrasi> findByStatus(StatusRecord statusRecord);
    Iterable<Konsentrasi> findByIdProdiAndStatus(Prodi prodi, StatusRecord statusRecord);
}