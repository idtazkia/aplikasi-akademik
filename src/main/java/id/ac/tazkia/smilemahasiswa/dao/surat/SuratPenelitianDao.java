package id.ac.tazkia.smilemahasiswa.dao.surat;

import id.ac.tazkia.smilemahasiswa.entity.Prodi;
import id.ac.tazkia.smilemahasiswa.entity.surat.SuratPenelitian;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SuratPenelitianDao extends JpaRepository<SuratPenelitian, String> {

    List<SuratPenelitian> findByMahasiswaNimOrderByTanggalDibuatDesc(String nim);

    List<SuratPenelitian> findByNamaPerusahaanContainingIgnoreCase(String namaPerusahaan);

    List<SuratPenelitian> findByStatusOrderByTanggalDibuatDesc(String status);

    List<SuratPenelitian> findByNamaPerusahaanContainingIgnoreCase(String namaPerusahaan, Pageable pageable);

    List<SuratPenelitian> findByStatusOrderByTanggalDibuatDesc(String status, Pageable pageable);

    @Query("SELECT sp FROM SuratPenelitian sp WHERE sp.mahasiswa.id = ?1 AND sp.status = ?1")
    List<SuratPenelitian> findByMahasiswaIdAndStatus(String mahasiswaId, String status);

    List<SuratPenelitian> findByJudulSkripsiContainingIgnoreCase(String judulSkripsi);

    Optional<SuratPenelitian> findByMahasiswaNimAndStatus(String nim, String status);

    boolean existsByMahasiswaNimAndStatus(String nim, String status);

    // Filter by prodi for KPS
    @Query("SELECT sp FROM SuratPenelitian sp WHERE sp.mahasiswa.idProdi = :prodi")
    Page<SuratPenelitian> findByMahasiswaIdProdi(@Param("prodi") Prodi prodi, Pageable pageable);

    // Filter by NIM
    @Query("SELECT sp FROM SuratPenelitian sp WHERE LOWER(sp.mahasiswa.nim) LIKE LOWER(CONCAT('%', :nim, '%'))")
    Page<SuratPenelitian> findByNimContainingIgnoreCase(@Param("nim") String nim, Pageable pageable);

    // Filter by Status Approval
    Page<SuratPenelitian> findByStatusApproval(id.ac.tazkia.smilemahasiswa.entity.StatusRecord statusApproval, Pageable pageable);

    // Filter by NIM and Status Approval
    @Query("SELECT sp FROM SuratPenelitian sp WHERE LOWER(sp.mahasiswa.nim) LIKE LOWER(CONCAT('%', :nim, '%')) AND sp.statusApproval = :statusApproval")
    Page<SuratPenelitian> findByNimContainingIgnoreCaseAndStatusApproval(@Param("nim") String nim, @Param("statusApproval") id.ac.tazkia.smilemahasiswa.entity.StatusRecord statusApproval, Pageable pageable);

    // Filter by Prodi and NIM
    @Query("SELECT sp FROM SuratPenelitian sp WHERE sp.mahasiswa.idProdi = :prodi AND LOWER(sp.mahasiswa.nim) LIKE LOWER(CONCAT('%', :nim, '%'))")
    Page<SuratPenelitian> findByMahasiswaIdProdiAndNimContainingIgnoreCase(@Param("prodi") Prodi prodi, @Param("nim") String nim, Pageable pageable);

    // Filter by Prodi and Status Approval
    @Query("SELECT sp FROM SuratPenelitian sp WHERE sp.mahasiswa.idProdi = :prodi AND sp.statusApproval = :statusApproval")
    Page<SuratPenelitian> findByMahasiswaIdProdiAndStatusApproval(@Param("prodi") Prodi prodi, @Param("statusApproval") id.ac.tazkia.smilemahasiswa.entity.StatusRecord statusApproval, Pageable pageable);

    // Filter by Prodi, NIM, and Status Approval
    @Query("SELECT sp FROM SuratPenelitian sp WHERE sp.mahasiswa.idProdi = :prodi AND LOWER(sp.mahasiswa.nim) LIKE LOWER(CONCAT('%', :nim, '%')) AND sp.statusApproval = :statusApproval")
    Page<SuratPenelitian> findByMahasiswaIdProdiAndNimContainingIgnoreCaseAndStatusApproval(@Param("prodi") Prodi prodi, @Param("nim") String nim, @Param("statusApproval") id.ac.tazkia.smilemahasiswa.entity.StatusRecord statusApproval, Pageable pageable);
}
