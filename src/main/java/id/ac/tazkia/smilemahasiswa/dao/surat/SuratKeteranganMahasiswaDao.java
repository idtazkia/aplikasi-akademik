package id.ac.tazkia.smilemahasiswa.dao.surat;

import id.ac.tazkia.smilemahasiswa.entity.Karyawan;
import id.ac.tazkia.smilemahasiswa.entity.Prodi;
import id.ac.tazkia.smilemahasiswa.entity.surat.SuratKeteranganMahasiswa;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SuratKeteranganMahasiswaDao extends JpaRepository<SuratKeteranganMahasiswa, String> {

    List<SuratKeteranganMahasiswa> findByMahasiswaNimOrderByTanggalDibuatDesc(String nim);

    List<SuratKeteranganMahasiswa> findByStatusOrderByTanggalDibuatDesc(String status);

    // Filter by approver (karyawan yang login)
    Page<SuratKeteranganMahasiswa> findByUserApprovalOrderByTanggalDibuatDesc(Karyawan userApproval, Pageable pageable);

    // Filter by approver and status approval
    @Query("SELECT skm FROM SuratKeteranganMahasiswa skm WHERE skm.userApproval = :userApproval AND skm.statusApproval = :statusApproval")
    Page<SuratKeteranganMahasiswa> findByUserApprovalAndStatusApprovalOrderByTanggalDibuatDesc(@Param("userApproval") Karyawan userApproval, @Param("statusApproval") id.ac.tazkia.smilemahasiswa.entity.StatusRecord statusApproval, Pageable pageable);

    // Filter by prodi for KPS
    @Query("SELECT skm FROM SuratKeteranganMahasiswa skm WHERE skm.mahasiswa.idProdi = :prodi")
    Page<SuratKeteranganMahasiswa> findByMahasiswaIdProdi(@Param("prodi") Prodi prodi, Pageable pageable);

    // Filter by NIM
    @Query("SELECT skm FROM SuratKeteranganMahasiswa skm WHERE LOWER(skm.mahasiswa.nim) LIKE LOWER(CONCAT('%', :nim, '%'))")
    Page<SuratKeteranganMahasiswa> findByNimContainingIgnoreCase(@Param("nim") String nim, Pageable pageable);

    // Filter by Status Approval
    Page<SuratKeteranganMahasiswa> findByStatusApproval(id.ac.tazkia.smilemahasiswa.entity.StatusRecord statusApproval, Pageable pageable);

    // Filter by NIM and Status Approval
    @Query("SELECT skm FROM SuratKeteranganMahasiswa skm WHERE LOWER(skm.mahasiswa.nim) LIKE LOWER(CONCAT('%', :nim, '%')) AND skm.statusApproval = :statusApproval")
    Page<SuratKeteranganMahasiswa> findByNimContainingIgnoreCaseAndStatusApproval(@Param("nim") String nim, @Param("statusApproval") id.ac.tazkia.smilemahasiswa.entity.StatusRecord statusApproval, Pageable pageable);

    // Filter by Prodi and NIM
    @Query("SELECT skm FROM SuratKeteranganMahasiswa skm WHERE skm.mahasiswa.idProdi = :prodi AND LOWER(skm.mahasiswa.nim) LIKE LOWER(CONCAT('%', :nim, '%'))")
    Page<SuratKeteranganMahasiswa> findByMahasiswaIdProdiAndNimContainingIgnoreCase(@Param("prodi") Prodi prodi, @Param("nim") String nim, Pageable pageable);

    // Filter by Prodi and Status Approval
    @Query("SELECT skm FROM SuratKeteranganMahasiswa skm WHERE skm.mahasiswa.idProdi = :prodi AND skm.statusApproval = :statusApproval")
    Page<SuratKeteranganMahasiswa> findByMahasiswaIdProdiAndStatusApproval(@Param("prodi") Prodi prodi, @Param("statusApproval") id.ac.tazkia.smilemahasiswa.entity.StatusRecord statusApproval, Pageable pageable);

    // Filter by Prodi, NIM, and Status Approval
    @Query("SELECT skm FROM SuratKeteranganMahasiswa skm WHERE skm.mahasiswa.idProdi = :prodi AND LOWER(skm.mahasiswa.nim) LIKE LOWER(CONCAT('%', :nim, '%')) AND skm.statusApproval = :statusApproval")
    Page<SuratKeteranganMahasiswa> findByMahasiswaIdProdiAndNimContainingIgnoreCaseAndStatusApproval(@Param("prodi") Prodi prodi, @Param("nim") String nim, @Param("statusApproval") id.ac.tazkia.smilemahasiswa.entity.StatusRecord statusApproval, Pageable pageable);
}
