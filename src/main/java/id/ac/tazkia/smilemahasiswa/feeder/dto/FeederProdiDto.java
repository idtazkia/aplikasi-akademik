package id.ac.tazkia.smilemahasiswa.feeder.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO untuk data Prodi dari Feeder API
 * Mapping dari response GetProdi
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FeederProdiDto {

    // Primary key di Feeder
    private String id_prodi;

    // Kode program studi
    private String kode_program_studi;

    // Nama program studi
    private String nama_program_studi;

    // Status (A = Aktif, N = Non-aktif, dll)
    private String status;

    // Jenjang pendidikan
    private String id_jenjang_pendidikan;
    private String nama_jenjang_pendidikan;

    // Tanggal berdiri
    private String tanggal_berdiri;

    // SK Penyelenggaraan
    private String sk_penyelenggaraan;
    private String tanggal_sk_penyelenggaraan;

    // Akreditasi
    private String id_status_milik;
    private String nama_status_milik;

    // Lainnya
    private String id_perguruan_tinggi;
    private String nama_perguruan_tinggi;
}
