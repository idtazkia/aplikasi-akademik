package id.ac.tazkia.smilemahasiswa.dao.surat;

import id.ac.tazkia.smilemahasiswa.entity.Mahasiswa;
import id.ac.tazkia.smilemahasiswa.entity.Prodi;
import id.ac.tazkia.smilemahasiswa.entity.StatusApprove;
import id.ac.tazkia.smilemahasiswa.entity.StatusRecord;
import id.ac.tazkia.smilemahasiswa.entity.surat.SuratTugasKuliah;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.PagingAndSortingRepository;

import java.util.List;

public interface SuratTugasKuliahDao extends PagingAndSortingRepository<SuratTugasKuliah, String> {

    SuratTugasKuliah findByMahasiswaAndStatus(Mahasiswa mahasiswa, StatusRecord status);

    List<SuratTugasKuliah> findByMahasiswaAndStatusOrderByTanggalPengajuanDesc(Mahasiswa mahasiswa, StatusRecord status);

    Page<SuratTugasKuliah> findByStatus(StatusRecord status, Pageable pageable);

    Page<SuratTugasKuliah> findByMahasiswaNamaContainingIgnoreCaseAndStatus(String nama,
                                                                             StatusRecord status,
                                                                             Pageable pageable);

    @Query("SELECT s FROM SuratTugasKuliah s WHERE s.status = ?1 ORDER BY s.tanggalPengajuan DESC")
    Page<SuratTugasKuliah> findAllByStatusOrderByTanggalPengajuanDesc(StatusRecord status, Pageable pageable);

    // Query untuk mencari surat berdasarkan prodi mahasiswa (untuk approval kaprodi)
    @Query("SELECT s FROM SuratTugasKuliah s WHERE s.mahasiswa.idProdi = ?1 AND s.status = ?2 AND s.statusApproveKaprodi = ?3 ORDER BY s.tanggalPengajuan DESC")
    Page<SuratTugasKuliah> findByProdiAndStatusAndStatusApproveKaprodi(Prodi prodi, StatusRecord status, StatusApprove statusApprove, Pageable pageable);

    // Query untuk mencari surat yang sudah approve oleh kaprodi tertentu
    @Query("SELECT s FROM SuratTugasKuliah s WHERE s.userApprovalKaprodi.id = ?1 ORDER BY s.tanggalApproveKaprodi DESC")
    List<SuratTugasKuliah> findByUserApprovalKaprodiOrderByTanggalApproveKaprodiDesc(String kaprodiId);
}
