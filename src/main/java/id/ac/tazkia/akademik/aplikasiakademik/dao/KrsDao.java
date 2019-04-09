package id.ac.tazkia.akademik.aplikasiakademik.dao;

import id.ac.tazkia.akademik.aplikasiakademik.entity.*;
import org.springframework.data.repository.PagingAndSortingRepository;

public interface KrsDao extends PagingAndSortingRepository<Krs,String> {
    Krs findByTahunAkademikStatusAndMahasiswa(StatusRecord statusRecord, Mahasiswa mahasiswa);
    Krs findByTahunAkademikAndMahasiswa(TahunAkademik tahunAkademik, Mahasiswa mahasiswa);
    Krs findByMahasiswaAndTahunAkademik(Mahasiswa mahasiswa, TahunAkademik tahunAkademik);
    Iterable<Krs> findByMahasiswa(Mahasiswa mahasiswa);
    Long countKrsByTahunAkademikAndMahasiswa(TahunAkademik tahunAkademik,Mahasiswa mahasiswa);
    Long countKrsByTahunAkademikAndMahasiswaStatus(TahunAkademik tahunAkademik,StatusRecord statusRecord);
    Long countKrsByTahunAkademikAndMahasiswaJenisKelamin (TahunAkademik tahunAkademik, JenisKelamin jenisKelamin);
    Krs findByMahasiswaAndTahunAkademikStatus(Mahasiswa mahasiswa, StatusRecord aktif);

}
