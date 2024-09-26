package id.ac.tazkia.smilemahasiswa.dto.attendance;

import java.time.LocalDate;
import java.time.LocalDateTime;

public interface PresensiDosenDto {
    String getIdPresensi();

    String getSesi();

    LocalDateTime getMasuk();

    String getTanggal();

    LocalDate getTanggalDate();

    LocalDateTime getSelesai();

    String getDosen();

    String getBerita();

    String getPertemuan();

    String getPertemuanke();

    String getRps();

    String getLink();

    LocalDateTime getSesiMulai();

    LocalDateTime getSesiSelesai();




}
