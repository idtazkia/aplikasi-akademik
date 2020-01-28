package id.ac.tazkia.akademik.aplikasiakademik.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;

import java.math.BigDecimal;
import java.time.LocalTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ListPenilaian {
    private String id;
    private String tahun;
    private String matakuliah;
    private Integer sks;
    private String hari;
    @DateTimeFormat(pattern = "HH:mm:ss")
    private LocalTime jamMulai;

    @DateTimeFormat(pattern = "HH:mm:ss")
    private LocalTime jamSelesai;

    private String prodi;
    private String dosen;
    private String kelas;
    private Integer sds;
    private BigDecimal presensi;
    private BigDecimal uts;
    private BigDecimal uas;
    private BigDecimal tugas;
}
