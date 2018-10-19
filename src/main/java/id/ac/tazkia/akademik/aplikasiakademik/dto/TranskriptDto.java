package id.ac.tazkia.akademik.aplikasiakademik.dto;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class TranskriptDto {
    private String kode;
    private String idKrs;
    private String tahunAkademik;
    private String mapel;
    private Integer sks;
    private BigDecimal presensi;
    private BigDecimal tugas;
    private BigDecimal uts;
    private BigDecimal uas;
    private String bobot;
    private BigDecimal total;
}
