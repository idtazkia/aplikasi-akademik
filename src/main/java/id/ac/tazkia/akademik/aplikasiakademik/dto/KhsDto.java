package id.ac.tazkia.akademik.aplikasiakademik.dto;

import id.ac.tazkia.akademik.aplikasiakademik.entity.KrsDetail;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.Min;
import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class KhsDto {
    private String id;
    private String kode;
    private String mapel;
    private Integer sks;
    private BigDecimal presensi;
    private BigDecimal tugas;
    private BigDecimal uts;
    private BigDecimal uas;
    private BigDecimal bobot;
    private BigDecimal total;
    private BigDecimal totalBobot;
}
