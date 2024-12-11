package id.ac.tazkia.smilemahasiswa.dto.graduation;

import id.ac.tazkia.smilemahasiswa.entity.Dosen;
import id.ac.tazkia.smilemahasiswa.entity.Ruangan;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import javax.persistence.Column;
import java.time.LocalDate;
import java.time.LocalTime;

@Data
public class PenjadwalanSeminarDto {
    private String id;
    private Dosen ketuaPenguji;
    private Dosen dosenPenguji;
    private Ruangan ruangan;
    @Column(columnDefinition = "DATE")
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDate tanggalUjian;
    private String jamMulai;
    private String jamSelesai;
}
