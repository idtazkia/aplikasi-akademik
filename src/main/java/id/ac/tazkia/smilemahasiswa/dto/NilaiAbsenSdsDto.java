package id.ac.tazkia.smilemahasiswa.dto;

import java.math.BigDecimal;

public interface NilaiAbsenSdsDto {

    String getIdMahasiswa();
    BigDecimal getPresensiDosen();
    BigDecimal getPresensiMahasiswa();
    BigDecimal getNilai();

}
