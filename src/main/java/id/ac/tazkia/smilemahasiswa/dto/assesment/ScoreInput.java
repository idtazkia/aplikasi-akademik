package id.ac.tazkia.smilemahasiswa.dto.assesment;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class ScoreInput {
    private String krsDetail;
    private String tugas;
    private String type;
    private BigDecimal nilai;
    private BigDecimal absensi;
}
