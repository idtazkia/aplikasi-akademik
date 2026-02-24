package id.ac.tazkia.smilemahasiswa.dto.edom;

import java.math.BigDecimal;

public interface RekapEdomDto {
    String getIdDosen();

    String getNamaDosen();

    String getIdJadwal();

    String getNamaMatakuliah();

    String getNamaKelas();

    Long getTotalMahasiswa();

    Long getTotalMengisi();

    BigDecimal getRataRata();
}
