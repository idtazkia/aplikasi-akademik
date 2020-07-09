package id.ac.tazkia.smilemahasiswa.dto.payment;

import java.math.BigDecimal;

public interface DaftarBiayaDto {

    String getIdMahasiswa();
    String getIdTahunAkademik();
    String getNamaTagihan();
    BigDecimal getTagihan();
    BigDecimal getDibayar();

}
