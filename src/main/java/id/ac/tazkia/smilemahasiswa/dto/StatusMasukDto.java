package id.ac.tazkia.smilemahasiswa.dto;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

import id.ac.tazkia.smilemahasiswa.entity.JenisMasuk;

import lombok.Data;

/**
 * DTO untuk input Status Masuk Mahasiswa
 */
@Data
public class StatusMasukDto {

    /**
     * NIM mahasiswa (opsional, untuk pencarian mahasiswa)
     */
    private String nim;

    /**
     * ID Mahasiswa (opsional, jika update langsung dengan ID)
     */
    private String idMahasiswa;

    /**
     * Jenis masuk mahasiswa
     */
    @NotNull(message = "Jenis masuk wajib diisi")
    private JenisMasuk jenisMasuk;

    /**
     * Keterangan tambahan (opsional)
     */
    private String keterangan;
}
