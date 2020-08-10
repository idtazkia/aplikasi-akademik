package id.ac.tazkia.smilemahasiswa.dto.payment;

import java.math.BigDecimal;

public interface BiayaMahasiswaDto {

    String getIdTagihan();
    String getNamaTagihan();
    String getNamaTahun();
    BigDecimal getnilai_tagihan();
    BigDecimal getDibayar();
    BigDecimal getSisa();

}
