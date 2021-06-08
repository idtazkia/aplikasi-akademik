package id.ac.tazkia.smilemahasiswa.dto.payment;

import java.math.BigDecimal;

public interface DaftarTagihanPerAngkatanDto {

    String getAngkatan();
    BigDecimal getTagihan();
    BigDecimal getDibayar();
    BigDecimal getSisa();
    BigDecimal getPercentage();

}
