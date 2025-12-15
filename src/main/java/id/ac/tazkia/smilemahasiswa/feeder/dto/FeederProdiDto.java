package id.ac.tazkia.smilemahasiswa.feeder.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

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

    /** Primary key di Feeder */
    @JsonProperty("id_prodi")
    private String idProdi;

    /** Kode program studi */
    @JsonProperty("kode_program_studi")
    private String kodeProgramStudi;

    /** Nama program studi */
    @JsonProperty("nama_program_studi")
    private String namaProgramStudi;

    /** Status (A = Aktif, N = Non-aktif, dll) */
    @JsonProperty("status")
    private String status;

    // Jenjang pendidikan
    @JsonProperty("id_jenjang_pendidikan")
    private String idJenjangPendidikan;

    @JsonProperty("nama_jenjang_pendidikan")
    private String namaJenjangPendidikan;

    /** Tanggal berdiri */
    @JsonProperty("tanggal_berdiri")
    private String tanggalBerdiri;

    // SK Penyelenggaraan
    @JsonProperty("sk_penyelenggaraan")
    private String skPenyelenggaraan;

    @JsonProperty("tanggal_sk_penyelenggaraan")
    private String tanggalSkPenyelenggaraan;

    // Akreditasi/Status Milik
    @JsonProperty("id_status_milik")
    private String idStatusMilik;

    @JsonProperty("nama_status_milik")
    private String namaStatusMilik;

    // Perguruan Tinggi
    @JsonProperty("id_perguruan_tinggi")
    private String idPerguruanTinggi;

    @JsonProperty("nama_perguruan_tinggi")
    private String namaPerguruanTinggi;
}
