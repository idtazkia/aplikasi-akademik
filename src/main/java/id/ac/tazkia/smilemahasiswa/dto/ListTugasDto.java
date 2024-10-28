package id.ac.tazkia.smilemahasiswa.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public interface ListTugasDto {

    String getId();
    String getIdSesi();
    String getNamaTugas();
    String getDeskripsi();
    String getPetunjukAktivitas();
    String getFile();
    String getNamaFile();
    LocalDateTime getWaktuMulai();
    LocalDateTime getTenggatWaktu();
    BigDecimal getNilai();
    LocalDateTime getWaktuPengumpulan();
    String getMasukan();

}
