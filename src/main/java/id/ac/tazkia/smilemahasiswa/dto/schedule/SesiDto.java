package id.ac.tazkia.smilemahasiswa.dto.schedule;

import java.time.LocalTime;

public interface SesiDto {
    String getSesi();
    LocalTime getJamMulai();
    LocalTime getJamSelesai();
}
