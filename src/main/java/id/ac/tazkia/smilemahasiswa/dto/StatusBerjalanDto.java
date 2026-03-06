package id.ac.tazkia.smilemahasiswa.dto;

import java.time.LocalDate;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.format.annotation.DateTimeFormat.ISO;

import id.ac.tazkia.smilemahasiswa.entity.StatusRecord;

import lombok.Data;

/**
 * DTO untuk input/update Status Berjalan Mahasiswa per Semester
 */
@Data
public class StatusBerjalanDto {

    /**
     * ID Mahasiswa
     */
    @NotBlank(message = "ID Mahasiswa wajib diisi")
    private String idMahasiswa;

    /**
     * ID Tahun Akademik
     */
    @NotBlank(message = "Tahun Akademik wajib diisi")
    private String idTahunAkademik;

    /**
     * Status berjalan mahasiswa
     */
    @NotNull(message = "Status berjalan wajib diisi")
    private StatusRecord statusBerjalan;

    /**
     * Tanggal status mulai berlaku (opsional)
     */
    @DateTimeFormat(iso = ISO.DATE)
    private LocalDate tanggalMulai;

    /**
     * Tanggal status selesai (opsional, untuk status sementara)
     */
    @DateTimeFormat(iso = ISO.DATE)
    private LocalDate tanggalSelesai;

    /**
     * Tanggal keluar (opsional, untuk status keluar)
     */
    @DateTimeFormat(iso = ISO.DATE)
    private LocalDate tanggalKeluar;

    /**
     * Keterangan tambahan (opsional)
     */
    private String keterangan;
}
