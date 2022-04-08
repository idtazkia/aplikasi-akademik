package id.ac.tazkia.smilemahasiswa.dto.assesment;

import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalTime;

public interface SoalDto {
    String getId();
    String getIdDos();
    String getRuangan();
    String getHari();
    @DateTimeFormat(pattern = "HH:mm:ss")
    LocalTime getMulai();
    @DateTimeFormat(pattern = "HH:mm:ss")
    LocalTime getSelesai();
    String getMatkul();
    String getKelas();
    String getNama();
    String getStatus();
    String getSoal();
    String getAkses();
}
