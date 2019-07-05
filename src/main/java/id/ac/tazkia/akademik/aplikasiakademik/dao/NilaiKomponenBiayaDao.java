package id.ac.tazkia.akademik.aplikasiakademik.dao;

import id.ac.tazkia.akademik.aplikasiakademik.entity.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.PagingAndSortingRepository;

import java.util.List;

public interface NilaiKomponenBiayaDao extends PagingAndSortingRepository<NilaiKomponenBiaya, String> {
    Page<NilaiKomponenBiaya> findByStatusNotInAndTahunAkademikAndIdAngkatanMahasiswaAndProdiAndProgram(StatusRecord status, TahunAkademik tahunAkademik, String angkatan, Prodi prodi, Program program, Pageable page);
    List<NilaiKomponenBiaya> findByStatusAndTahunAkademikAndIdAngkatanMahasiswaAndProdiAndProgram(StatusRecord status, TahunAkademik tahunAkademik, String angkatan, Prodi prodi, Program program);

}
