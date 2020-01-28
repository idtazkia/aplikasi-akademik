package id.ac.tazkia.akademik.aplikasiakademik.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TestDto {
    private String id;
    private String nim;
    private String nama;
    private Integer presensi;
    private BigDecimal nilaiUts;
    private BigDecimal nilaiUas;
    private Integer absensi;
    private BigDecimal nilaiAkhir;
    private String grade;
}
