package id.ac.tazkia.smilemahasiswa.dto.schedule;

import id.ac.tazkia.smilemahasiswa.entity.Dosen;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalTime;
import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class JadwalRequest {
    private String id;
    private String sesi;
    private String hari;
    private String ruangan;
    private String kelas;
    @DateTimeFormat(pattern = "HH:mm")
    private LocalTime mulai;
    @DateTimeFormat(pattern = "HH:mm")
    private LocalTime selesai;
    private String dosen;
    private List<String> team;
    private String akses;
    private Integer kapasitas;
    private List<Dosen> teamTeaching;

}
