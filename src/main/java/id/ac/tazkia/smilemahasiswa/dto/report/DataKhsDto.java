package id.ac.tazkia.smilemahasiswa.dto.report;

import java.math.BigDecimal;

public interface DataKhsDto {
    String getId();
    String getKode();
    String getMatakuliah();
    BigDecimal getPresensi();
    BigDecimal getTugas();
    BigDecimal getUts();
    BigDecimal getUas();
    BigDecimal getNilaiAkhir();
    BigDecimal getBobot();
    String getGrade();
}
