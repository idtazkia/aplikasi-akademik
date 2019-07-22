package id.ac.tazkia.akademik.aplikasiakademik.dao;

import id.ac.tazkia.akademik.aplikasiakademik.entity.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface KrsDao extends PagingAndSortingRepository<Krs,String> {
    Krs findByTahunAkademikStatusAndMahasiswa(StatusRecord statusRecord, Mahasiswa mahasiswa);
    Krs findByTahunAkademikAndMahasiswa(TahunAkademik tahunAkademik, Mahasiswa mahasiswa);
    Krs findByMahasiswaAndTahunAkademik(Mahasiswa mahasiswa, TahunAkademik tahunAkademik);
    Iterable<Krs> findByMahasiswa(Mahasiswa mahasiswa);
    Long countKrsByTahunAkademikAndMahasiswa(TahunAkademik tahunAkademik,Mahasiswa mahasiswa);
    Long countKrsByTahunAkademikAndMahasiswaStatus(TahunAkademik tahunAkademik,StatusRecord statusRecord);
    Long countKrsByTahunAkademikAndMahasiswaJenisKelamin (TahunAkademik tahunAkademik, JenisKelamin jenisKelamin);
    Page<Krs> findByTahunAkademikAndProdiAndMahasiswaIdProgramAndMahasiswaAngkatan(TahunAkademik tahunAkademik, Prodi prodi, Program program, String angkatan, Pageable pageable);

    List<Krs> findByTahunAkademikAndStatusAndKrsDetailsNotNull(TahunAkademik tahunAkademik,StatusRecord statusRecord);


    @Query("select k.id.id from Krs k where k.tahunAkademik= :tahun and k.status = :status")
    List<Krs> cariMahasiswa(@Param("tahun")TahunAkademik t, @Param("status")StatusRecord status);

}