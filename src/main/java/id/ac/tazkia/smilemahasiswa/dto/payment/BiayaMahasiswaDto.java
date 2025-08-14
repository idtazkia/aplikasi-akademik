package id.ac.tazkia.smilemahasiswa.dto.payment;

import java.math.BigDecimal;
import java.util.Date;

public interface BiayaMahasiswaDto {

    String getId();
    String getNamaTagihan();
    String getNamaTahun();
    String getStatus();
    Date getTanggalPembuatan();
    BigDecimal getnilai_tagihan();
    BigDecimal getDibayar();
    BigDecimal getSisa();

}
