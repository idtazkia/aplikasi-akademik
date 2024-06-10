package id.ac.tazkia.smilemahasiswa.dto.attendance;

import java.time.LocalDate;
import java.time.LocalDateTime;

public interface PresensiTugasDto {

    String getStatus();
    LocalDateTime getMasuk();
    LocalDateTime getKeluar();
    String getBeritaAcara();
    String getIdSesiKuliah();
    String getPertemuan();
    String getIdSesi();
    String getIdTugas();
    String getNama();
    String getDeskripsi();
    String getAktivitas();
    String getFile();
    LocalDate getTenggatWaktu();
    String getJenis();
    Integer getMaks();
    String getPertanyaan();

}
