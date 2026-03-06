package id.ac.tazkia.smilemahasiswa.dto;

import java.time.LocalDate;

import id.ac.tazkia.smilemahasiswa.entity.StatusRecord;
import lombok.Data;

/**
 * DTO untuk Smart Suggestions - Update Status Berjalan
 */
@Data
public class SmartSuggestionDto {

    // Data mahasiswa
    private String idMahasiswa;
    private String nim;
    private String nama;
    private String namaProdi;
    private String angkatan;
    private Double masaStudiTahun;

    // Data akademik (disederhanakan - tanpa kalkulasi presensi detail)
    private Boolean adaKrs;
    private Integer jumlahKrsDetail;
    private Integer jumlahCutiSebelumnya;

    // Riwayat status
    private String idRiwayatStatus;
    private StatusRecord statusBaruSaatIni;

    // Data untuk rekomendasi LULUS dan PUTUS_STUDI
    private LocalDate tanggalLulus;  // Jika ada dan masuk range tahun akademik -> LULUS
    private String kodeJenjang;     // D3, S1, S2, S3 - untuk menentukan batas studi

    // Status rekomendasi (dihitung oleh service)
    private StatusRecord statusRekomendasi;
    private String keteranganRekomendasi;

    /**
     * Factory method untuk membuat DTO dari hasil query
     * Urutan kolom dari query (versi sederhana tanpa presensi):
     * [0] m.id
     * [1] m.nim
     * [2] m.nama
     * [3] p.nama_prodi
     * [4] m.angkatan
     * [5] masa_studi_tahun
     * [6] ada_krs (1=ada, 0=tidak ada)
     * [7] jumlah_krs_detail
     * [8] jumlah_cuti_sebelumnya
     * [9] id_riwayat_status (jika sudah ada, atau null)
     * [10] status_berjalan_saat_ini (jika sudah ada, atau null)
     * [11] m.tanggal_lulus
     * [12] j.kode_jenjang
     */
    public static SmartSuggestionDto fromQueryResult(Object[] row) {
        SmartSuggestionDto dto = new SmartSuggestionDto();

        dto.setIdMahasiswa((String) row[0]);
        dto.setNim((String) row[1]);
        dto.setNama((String) row[2]);
        dto.setNamaProdi((String) row[3]);
        dto.setAngkatan((String) row[4]);

        // Masa studi
        if (row[5] != null) {
            dto.setMasaStudiTahun(((Number) row[5]).doubleValue());
        }

        // Ada KRS
        dto.setAdaKrs(row[6] != null && ((Number) row[6]).intValue() == 1);

        // Jumlah KRS Detail
        if (row[7] != null) {
            dto.setJumlahKrsDetail(((Number) row[7]).intValue());
        } else {
            dto.setJumlahKrsDetail(0);
        }

        // Jumlah cuti sebelumnya
        if (row[8] != null) {
            dto.setJumlahCutiSebelumnya(((Number) row[8]).intValue());
        } else {
            dto.setJumlahCutiSebelumnya(0);
        }

        // ID riwayat status
        dto.setIdRiwayatStatus((String) row[9]);

        // Status berjalan saat ini
        if (row[10] != null) {
            try {
                dto.setStatusBaruSaatIni(StatusRecord.valueOf((String) row[10]));
            } catch (IllegalArgumentException e) {
                dto.setStatusBaruSaatIni(null);
            }
        }

        // Tanggal lulus
        if (row[11] != null) {
            if (row[11] instanceof LocalDate) {
                dto.setTanggalLulus((LocalDate) row[11]);
            } else if (row[11] instanceof java.sql.Date) {
                dto.setTanggalLulus(((java.sql.Date) row[11]).toLocalDate());
            }
        }

        // Kode jenjang
        dto.setKodeJenjang((String) row[12]);

        return dto;
    }
}
