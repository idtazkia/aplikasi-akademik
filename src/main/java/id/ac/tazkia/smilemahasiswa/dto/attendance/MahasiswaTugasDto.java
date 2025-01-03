package id.ac.tazkia.smilemahasiswa.dto.attendance;

import java.time.LocalDate;
import java.time.LocalDateTime;

public interface MahasiswaTugasDto {
    String getId();
    String getNim();
    String getNama();
    Integer getMangkir();
    String getNilai();
    String getWaktu();
    String getFile();
    String getStatus();
}
