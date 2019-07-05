package id.ac.tazkia.akademik.aplikasiakademik.dao;

import id.ac.tazkia.akademik.aplikasiakademik.entity.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.PagingAndSortingRepository;

public interface KrsDao extends PagingAndSortingRepository<Krs,String> {
    Krs findByTahunAkademikStatusAndMahasiswa(StatusRecord statusRecord, Mahasiswa mahasiswa);
    Krs findByTahunAkademikAndMahasiswa(TahunAkademik tahunAkademik, Mahasiswa mahasiswa);
    Krs findByMahasiswaAndTahunAkademik(Mahasiswa mahasiswa, TahunAkademik tahunAkademik);
    Iterable<Krs> findByMahasiswa(Mahasiswa mahasiswa);
    Long countKrsByTahunAkademikAndMahasiswa(TahunAkademik tahunAkademik,Mahasiswa mahasiswa);
    Long countKrsByTahunAkademikAndMahasiswaStatus(TahunAkademik tahunAkademik,StatusRecord statusRecord);
    Long countKrsByTahunAkademikAndMahasiswaJenisKelamin (TahunAkademik tahunAkademik, JenisKelamin jenisKelamin);
    Page<Krs> findByTahunAkademikAndProdiAndMahasiswaIdProgramAndMahasiswaAngkatan(TahunAkademik tahunAkademik, Prodi prodi, Program program, String angkatan, Pageable pageable);

}
