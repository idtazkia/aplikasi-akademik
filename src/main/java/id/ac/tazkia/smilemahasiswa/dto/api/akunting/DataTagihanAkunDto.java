package id.ac.tazkia.smilemahasiswa.dto.api.akunting;

import java.math.BigDecimal;

public interface DataTagihanAkunDto {

    String getNim();
    String getNama();
    String getJenisTagihan();
    String getKodeTagihan();
    BigDecimal getNilaiTagihan();
    BigDecimal getDibayar();
    BigDecimal getSisa();

}
