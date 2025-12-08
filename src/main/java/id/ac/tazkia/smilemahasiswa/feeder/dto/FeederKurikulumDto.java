package id.ac.tazkia.smilemahasiswa.feeder.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO untuk data Kurikulum dari Feeder API
 * Mapping dari response GetListKurikulum
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FeederKurikulumDto {

    // Primary key di Feeder
    private String id_kurikulum;

    // Nama kurikulum
    private String nama_kurikulum;

    // Program Studi
    private String id_prodi;
    private String nama_program_studi;

    // Jenjang
    private String id_jenjang_pendidikan;
    private String nama_jenjang_pendidikan;

    // Semester mulai berlaku
    private String id_semester;
    private String nama_semester;

    // Jumlah SKS
    private String jumlah_sks_lulus;
    private String jumlah_sks_wajib;
    private String jumlah_sks_pilihan;

    // Jumlah semester
    private String jumlah_semester;

    // Capaian Pembelajaran Lulusan
    private String cpl_sikap;
    private String cpl_pengetahuan;
    private String cpl_keterampilan_umum;
    private String cpl_keterampilan_khusus;

    // Keterangan
    private String keterangan;
}
