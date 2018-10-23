package id.ac.tazkia.akademik.aplikasiakademik.dto;


import id.ac.tazkia.akademik.aplikasiakademik.entity.Krs;
import id.ac.tazkia.akademik.aplikasiakademik.entity.Matakuliah;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data @AllArgsConstructor @NoArgsConstructor
public class RekapMissAttendance {

    private String krsId;
    private String matakuliah;
    private Long absen;

    public BigDecimal getPersentase() {
        return new BigDecimal(absen).divide(new BigDecimal(3),2, BigDecimal.ROUND_HALF_EVEN).multiply(new BigDecimal(100));
    }
}
