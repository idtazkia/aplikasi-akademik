package id.ac.tazkia.smilemahasiswa.dto.report;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;


public interface ReportUjianDto {
    String getId();
    String getDosen();
    String getMatakuliah();
    String getKelas();
    String getDetailPertemuan();
}
