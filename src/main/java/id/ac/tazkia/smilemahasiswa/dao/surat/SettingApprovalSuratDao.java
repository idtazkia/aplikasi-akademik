package id.ac.tazkia.smilemahasiswa.dao.surat;

import id.ac.tazkia.smilemahasiswa.entity.surat.SettingApprovalSurat;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SettingApprovalSuratDao extends JpaRepository<SettingApprovalSurat, String> {

    // Cari setting approval berdasarkan jenis surat dan status aktif
    SettingApprovalSurat findByJenisSuratAndStatus(String jenisSurat, id.ac.tazkia.smilemahasiswa.entity.StatusRecord status);

    // Cari semua setting approval berdasarkan jenis surat
    Page<SettingApprovalSurat> findByJenisSurat(String jenisSurat, Pageable pageable);

    // Cari semua setting approval dengan pagination
    Page<SettingApprovalSurat> findByStatusOrderByTanggalUpdateDesc(id.ac.tazkia.smilemahasiswa.entity.StatusRecord status, Pageable pageable);
}
