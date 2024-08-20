package id.ac.tazkia.smilemahasiswa.dto.periodetest;

import java.time.LocalDateTime;

public interface GetData {
    String getId();
    LocalDateTime getTanggal();
    LocalDateTime getTerakhir();
    String getStatuspt();
    Integer getKuota();
    String getJenis();
    String getRuanganid();
    String getNama();
    Integer getTotal();
}
