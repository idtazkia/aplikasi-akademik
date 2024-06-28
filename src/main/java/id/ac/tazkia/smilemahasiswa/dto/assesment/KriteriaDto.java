package id.ac.tazkia.smilemahasiswa.dto.assesment;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class KriteriaDto {
    private String jadwal;
    private String sds;
    private BigDecimal BobotPresensi = BigDecimal.ZERO;
    private BigDecimal BobotTugas = BigDecimal.ZERO;
    private BigDecimal bobotUas = BigDecimal.ZERO;
    private BigDecimal bobotUts = BigDecimal.ZERO;
}
