package id.ac.tazkia.smilemahasiswa.feeder.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

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

    /** Primary key di Feeder */
    @JsonProperty("id_kurikulum")
    private String idKurikulum;

    /** Nama kurikulum */
    @JsonProperty("nama_kurikulum")
    private String namaKurikulum;

    // Program Studi
    @JsonProperty("id_prodi")
    private String idProdi;

    @JsonProperty("nama_program_studi")
    private String namaProgramStudi;

    // Jenjang
    @JsonProperty("id_jenjang_pendidikan")
    private String idJenjangPendidikan;

    @JsonProperty("nama_jenjang_pendidikan")
    private String namaJenjangPendidikan;

    // Semester mulai berlaku
    @JsonProperty("id_semester")
    private String idSemester;

    @JsonProperty("nama_semester")
    private String namaSemester;

    // Jumlah SKS
    @JsonProperty("jumlah_sks_lulus")
    private String jumlahSksLulus;

    @JsonProperty("jumlah_sks_wajib")
    private String jumlahSksWajib;

    @JsonProperty("jumlah_sks_pilihan")
    private String jumlahSksPilihan;

    // Jumlah semester
    @JsonProperty("jumlah_semester")
    private String jumlahSemester;

    // Capaian Pembelajaran Lulusan
    @JsonProperty("cpl_sikap")
    private String cplSikap;

    @JsonProperty("cpl_pengetahuan")
    private String cplPengetahuan;

    @JsonProperty("cpl_keterampilan_umum")
    private String cplKeterampilanUmum;

    @JsonProperty("cpl_keterampilan_khusus")
    private String cplKeterampilanKhusus;

    /** Keterangan */
    @JsonProperty("keterangan")
    private String keterangan;
}
