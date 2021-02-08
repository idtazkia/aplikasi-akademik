package id.ac.tazkia.smilemahasiswa.dto.elearning;

import lombok.Data;

import java.math.BigDecimal;
import java.math.BigInteger;

@Data
public class MdlGradeGradesDto {

    private String id;
    private String idJadwal;
    private String mahasiswa;
    private BigInteger idBobotTugas;
    private BigDecimal finalGrade;
    private BigDecimal nilai;
    private String status;
    private BigDecimal nilaiAkhir;
    private BigDecimal bobot;

}
