package id.ac.tazkia.akademik.aplikasiakademik.dto;

import java.math.BigDecimal;

public interface Khs {
    String getId();
    String getMatakuliah();
    BigDecimal getPresensi();
    BigDecimal getTugas();
    BigDecimal getUts();
    BigDecimal getUas();
    BigDecimal getNilaiAkhir();
    BigDecimal getBobot();
    String getGrade();
}
