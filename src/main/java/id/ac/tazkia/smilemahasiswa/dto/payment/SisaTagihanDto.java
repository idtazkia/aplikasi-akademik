package id.ac.tazkia.smilemahasiswa.dto.payment;

import java.math.BigDecimal;

public interface SisaTagihanDto {

    String getIdMahasiswa();
    String getIdTahunAkademik();
    String getNamaTahun();
    BigDecimal getTagihan();
    BigDecimal getPotongan();
    BigDecimal getDibayar();
    BigDecimal getPenarikan();
    BigDecimal getSisa();

}
