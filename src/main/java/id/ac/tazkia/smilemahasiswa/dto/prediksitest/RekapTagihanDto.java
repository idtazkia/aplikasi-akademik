package id.ac.tazkia.smilemahasiswa.dto.prediksitest;

import id.ac.tazkia.smilemahasiswa.constant.JenisTest;

import java.math.BigDecimal;

public interface RekapTagihanDto {

    String getIdPrediksi();
    String getNim();
    String getNama();
    String getAngkatan();
    Integer getUjian();
    String getJenis();
    BigDecimal getNilai();
    String getStatus();

}
