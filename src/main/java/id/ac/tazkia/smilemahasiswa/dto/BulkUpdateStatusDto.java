package id.ac.tazkia.smilemahasiswa.dto;

import java.time.LocalDate;
import java.util.List;

import javax.validation.constraints.NotNull;

import id.ac.tazkia.smilemahasiswa.entity.StatusRecord;

import lombok.Data;

/**
 * DTO untuk Bulk Update Status Mahasiswa
 *
 * Digunakan untuk mengupdate status banyak mahasiswa sekaligus
 */
@Data
public class BulkUpdateStatusDto {

    /**
     * Filter: ID Prodi (opsional)
     */
    private String idProdi;

    /**
     * Filter: Angkatan (opsional)
     */
    private String angkatan;

    /**
     * Filter: ID Tahun Akademik (opsional)
     */
    private String idTahunAkademik;

    /**
     * Filter: Status saat ini (opsional)
     * Hanya mahasiswa dengan status ini yang akan diupdate
     */
    private StatusRecord statusSaatIni;

    /**
     * Status baru yang akan diset
     */
    @NotNull(message = "Status baru wajib diisi")
    private StatusRecord statusBaru;

    /**
     * Keterangan untuk perubahan status
     */
    private String keterangan;

    /**
     * Tanggal mulai status (opsional)
     */
    private LocalDate tanggalMulai;

    /**
     * Tanggal keluar (opsional) - digunakan jika status baru adalah status keluar
     */
    private LocalDate tanggalKeluar;

    /**
     * List ID mahasiswa yang akan diupdate (opsional)
     * Jika diisi, hanya mahasiswa ini yang akan diupdate
     * Jika kosong, semua mahasiswa yang sesuai filter akan diupdate
     */
    private List<String> idMahasiswa;

    /**
     * Preview mode - hanya menampilkan yang akan diupdate tanpa eksekusi (default: false)
     */
    private Boolean previewMode = false;
}
