package id.ac.tazkia.smilemahasiswa.dto.schedule;

import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalTime;

public interface ListJadwalUjian {
    String getId();
    String getDosen();
    String getMatkul();
    String getKelas();
    String getRuangan();
    String getTanggal();
    String getWaktu();
}
