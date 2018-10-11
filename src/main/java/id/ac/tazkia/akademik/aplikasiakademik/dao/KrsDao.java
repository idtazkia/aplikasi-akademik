package id.ac.tazkia.akademik.aplikasiakademik.dao;

import id.ac.tazkia.akademik.aplikasiakademik.entity.*;
import org.springframework.data.repository.PagingAndSortingRepository;

import java.util.List;

public interface KrsDao extends PagingAndSortingRepository<Krs,String> {
    Krs findByTahunAkademikStatusAndMahasiswa(StatusRecord statusRecord, Mahasiswa mahasiswa);
    Krs findByTahunAkademikStatus(StatusRecord S);

    Krs findByStatusNotInAndMahasiswaAndTahunAkademikProdi(StatusRecord statusRecord, Mahasiswa mahasiswa, List<TahunAkademikProdi> tahunAkademikProdi);


    List<Krs> findByStatusAndMahasiswaAndTahunAkademik(StatusRecord statusRecord, Mahasiswa mahasiswa, TahunAkademik tahunAkademik);
    Krs findByMahasiswaAndTahunAkademik(Mahasiswa mahasiswa, TahunAkademik tahunAkademik);


}
