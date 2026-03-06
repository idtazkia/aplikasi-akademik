package id.ac.tazkia.smilemahasiswa.dto;

import java.time.LocalDate;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

import id.ac.tazkia.smilemahasiswa.entity.StatusKeluar;

import lombok.Data;

/**
 * DTO untuk input Status Keluar Mahasiswa
 */
@Data
public class StatusKeluarDto {

    /**
     * ID Mahasiswa
     */
    @NotBlank(message = "ID Mahasiswa wajib diisi")
    private String idMahasiswa;

    /**
     * Status keluar mahasiswa
     */
    @NotNull(message = "Status keluar wajib diisi")
    private StatusKeluar statusKeluar;

    /**
     * Tanggal keluar/lulus
     */
    @NotNull(message = "Tanggal keluar wajib diisi")
    private LocalDate tanggalKeluar;

    /**
     * ID Tahun Akademik saat keluar
     */
    private String idTahunAkademikKeluar;

    /**
     * Alasan keluar (opsional)
     */
    private String alasanKeluar;

    /**
     * Update juga riwayat status di periode ini? (default: true)
     */
    private Boolean updateRiwayatStatus = true;
}
