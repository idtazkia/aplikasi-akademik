package id.ac.tazkia.smilemahasiswa.dao.surat;

import id.ac.tazkia.smilemahasiswa.entity.Prodi;
import id.ac.tazkia.smilemahasiswa.entity.surat.SuratMagang;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
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

    // Filter by prodi for KPS
    @Query("SELECT sm FROM SuratMagang sm WHERE sm.mahasiswa.idProdi = :prodi")
    Page<SuratMagang> findByMahasiswaIdProdi(@Param("prodi") Prodi prodi, Pageable pageable);

    @Query("SELECT sm FROM SuratMagang sm WHERE sm.mahasiswa.idProdi = :prodi AND LOWER(sm.namaPerusahaan) LIKE LOWER(CONCAT('%', :search, '%'))")
    Page<SuratMagang> findByMahasiswaIdProdiAndNamaPerusahaanContainingIgnoreCase(@Param("prodi") Prodi prodi, @Param("search") String search, Pageable pageable);

    // Filter by NIM
    @Query("SELECT sm FROM SuratMagang sm WHERE LOWER(sm.mahasiswa.nim) LIKE LOWER(CONCAT('%', :nim, '%'))")
    Page<SuratMagang> findByNimContainingIgnoreCase(@Param("nim") String nim, Pageable pageable);

    // Filter by Status Approval
    Page<SuratMagang> findByStatusApproval(id.ac.tazkia.smilemahasiswa.entity.StatusRecord statusApproval, Pageable pageable);

    // Filter by NIM and Status Approval
    @Query("SELECT sm FROM SuratMagang sm WHERE LOWER(sm.mahasiswa.nim) LIKE LOWER(CONCAT('%', :nim, '%')) AND sm.statusApproval = :statusApproval")
    Page<SuratMagang> findByNimContainingIgnoreCaseAndStatusApproval(@Param("nim") String nim, @Param("statusApproval") id.ac.tazkia.smilemahasiswa.entity.StatusRecord statusApproval, Pageable pageable);

    // Filter by Prodi and NIM
    @Query("SELECT sm FROM SuratMagang sm WHERE sm.mahasiswa.idProdi = :prodi AND LOWER(sm.mahasiswa.nim) LIKE LOWER(CONCAT('%', :nim, '%'))")
    Page<SuratMagang> findByMahasiswaIdProdiAndNimContainingIgnoreCase(@Param("prodi") Prodi prodi, @Param("nim") String nim, Pageable pageable);

    // Filter by Prodi and Status Approval
    @Query("SELECT sm FROM SuratMagang sm WHERE sm.mahasiswa.idProdi = :prodi AND sm.statusApproval = :statusApproval")
    Page<SuratMagang> findByMahasiswaIdProdiAndStatusApproval(@Param("prodi") Prodi prodi, @Param("statusApproval") id.ac.tazkia.smilemahasiswa.entity.StatusRecord statusApproval, Pageable pageable);

    // Filter by Prodi, NIM, and Status Approval
    @Query("SELECT sm FROM SuratMagang sm WHERE sm.mahasiswa.idProdi = :prodi AND LOWER(sm.mahasiswa.nim) LIKE LOWER(CONCAT('%', :nim, '%')) AND sm.statusApproval = :statusApproval")
    Page<SuratMagang> findByMahasiswaIdProdiAndNimContainingIgnoreCaseAndStatusApproval(@Param("prodi") Prodi prodi, @Param("nim") String nim, @Param("statusApproval") id.ac.tazkia.smilemahasiswa.entity.StatusRecord statusApproval, Pageable pageable);
}