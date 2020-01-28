package id.ac.tazkia.smilemahasiswa.dto.report;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

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
