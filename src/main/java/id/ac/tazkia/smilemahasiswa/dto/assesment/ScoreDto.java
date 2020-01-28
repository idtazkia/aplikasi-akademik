package id.ac.tazkia.smilemahasiswa.dto.assesment;

import java.math.BigDecimal;

public interface ScoreDto {
    String getKrs();
    String getMahasiswa();
    String getNama();
    String getNim();
    String getAbsmahasiswa();
    String getAbsdosen();
    BigDecimal getAbsen();
    BigDecimal getNilaiabsen();
    BigDecimal getNilaisds();
    BigDecimal getUts();
    BigDecimal getUas();
    BigDecimal getAkhir();
    String getGrade();
}
