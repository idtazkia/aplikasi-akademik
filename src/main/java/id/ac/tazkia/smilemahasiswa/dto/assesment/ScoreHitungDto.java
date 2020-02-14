package id.ac.tazkia.smilemahasiswa.dto.assesment;

import java.math.BigDecimal;

public interface ScoreHitungDto {
    String getKrs();
    String getMahasiswa();
    String getNama();
    String getNim();
    String getAbsmahasiswa();
    String getAbsdosen();
    BigDecimal getAbsen();
    BigDecimal getNilaiabsen();
    BigDecimal getNilaiSds();
    BigDecimal getNilaiTugas();
    BigDecimal getNilaiUts();
    BigDecimal getNilaiUas();
    BigDecimal getNilaiAkhir();
    String getGrade();
    BigDecimal getBobot();
}
