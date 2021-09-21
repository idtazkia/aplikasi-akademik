package id.ac.tazkia.smilemahasiswa.dto.graduation;

import java.math.BigDecimal;

public interface RekapTugasAkhir {
    String getNim();
    String getNama();
    String getTanggalSempro();
    String getKetuaSempro();
    String getPembimbingSempro();
    String getPengujiSempro();
    String getJamMulai();
    String getMulaiSidang();
    String getSelesaiSIdang();
    BigDecimal getNilai();
    String getStatusSidang();
    String getKetuaSidang();
    String getPembimbingSidang();
    String getPengujiSidang();
    BigDecimal getNilaiSidang();
    String getTanggalSidang();
}
