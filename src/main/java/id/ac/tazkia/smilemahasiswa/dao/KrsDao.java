package id.ac.tazkia.smilemahasiswa.dao;

import id.ac.tazkia.smilemahasiswa.entity.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.PagingAndSortingRepository;

public interface KrsDao extends PagingAndSortingRepository<Krs, String> {
    Krs findByMahasiswaAndTahunAkademikAndStatus(Mahasiswa mahasiswa, TahunAkademik ta, StatusRecord s);

    Long countKrsByTahunAkademikAndMahasiswaStatus(TahunAkademik tahunAkademik, StatusRecord aktif);

    Long countKrsByTahunAkademikAndMahasiswaJenisKelamin(TahunAkademik tahunAkademik, JenisKelamin pria);

    Page<Krs> findByTahunAkademikAndProdiAndMahasiswaIdProgramAndMahasiswaAngkatan(TahunAkademik tahunAkademik, Prodi prodi, Program program, String angkatan, Pageable pageable);

    Krs findByTahunAkademikAndMahasiswaAndStatus(TahunAkademik tahunAkademik, Mahasiswa mhsw, StatusRecord aktif);
}
