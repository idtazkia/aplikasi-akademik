package id.ac.tazkia.smilemahasiswa.dto.api.akunting;

import lombok.Data;

import java.math.BigDecimal;

public interface DataTagihanPerbulanTahunDto {

    String getNim();
    String getNama();
    BigDecimal getTagihanJanuari();
    BigDecimal getTagihanFebruari();
    BigDecimal getTagihanMaret();
    BigDecimal getTagihanApril();
    BigDecimal getTagihanMei();
    BigDecimal getTagihanJuni();
    BigDecimal getTagihanJuli();
    BigDecimal getTagihanAgustus();
    BigDecimal getTagihanSeptember();
    BigDecimal getTagihanOktober();
    BigDecimal getTagihanNovember();
    BigDecimal getTagihanDesember();
    BigDecimal getBayarJanuari();
    BigDecimal getBayarFebruari();
    BigDecimal getBayarMaret();
    BigDecimal getBayarApril();
    BigDecimal getBayarMei();
    BigDecimal getBayarJuni();
    BigDecimal getBayarJuli();
    BigDecimal getBayarAgustus();
    BigDecimal getBayarSeptember();
    BigDecimal getBayarOktober();
    BigDecimal getBayarNovember();
    BigDecimal getBayarDesember();
    BigDecimal getTotalTagihan();
    BigDecimal getTotalBayar();
    BigDecimal getSisaTagihan();

}
