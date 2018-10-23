package id.ac.tazkia.akademik.aplikasiakademik.dto;

import id.ac.tazkia.akademik.aplikasiakademik.entity.KrsDetail;
import lombok.Data;

import javax.validation.constraints.Min;
import java.math.BigDecimal;

@Data
public class KhsDto {
    private KrsDetail id;
    private String kode;
    private String mapel;
    private Integer sks;
    private BigDecimal presensi;
    private BigDecimal tugas;
    private BigDecimal uts;
    private BigDecimal uas;
    private String bobot;
    private BigDecimal total;
}
