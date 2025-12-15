package id.ac.tazkia.smilemahasiswa.dao.surat;

import id.ac.tazkia.smilemahasiswa.entity.surat.SuratMagang;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface SuratMagangDao extends JpaRepository<SuratMagang, String> {

    List<SuratMagang> findByMahasiswaNimOrderByTanggalDibuatDesc(String nim);

    List<SuratMagang> findByNamaPerusahaanContainingIgnoreCase(String namaPerusahaan);

    List<SuratMagang> findByStatusOrderByTanggalDibuatDesc(String status);

    List<SuratMagang> findByNamaPerusahaanContainingIgnoreCase(String namaPerusahaan, org.springframework.data.domain.Pageable pageable);

    List<SuratMagang> findByStatusOrderByTanggalDibuatDesc(String status, org.springframework.data.domain.Pageable pageable);

    List<SuratMagang> findByTanggalMulaiMagangBetweenOrderByTanggalMulaiMagang(
            LocalDate startDate, LocalDate endDate);

    @Query("SELECT sm FROM SuratMagang sm WHERE sm.mahasiswa.id = ?1 AND sm.status = ?1")
    List<SuratMagang> findByMahasiswaIdAndStatus(String mahasiswaId, String status);

    List<SuratMagang> findByDivisiContainingIgnoreCase(String divisi);

    @Query("SELECT sm FROM SuratMagang sm WHERE FUNCTION('DATE', sm.tanggalDibuat) = CURRENT_DATE")
    List<SuratMagang> findTodaySurat();

    Optional<SuratMagang> findByMahasiswaNimAndStatus(String nim, String status);

    boolean existsByMahasiswaNimAndStatus(String nim, String status);
}