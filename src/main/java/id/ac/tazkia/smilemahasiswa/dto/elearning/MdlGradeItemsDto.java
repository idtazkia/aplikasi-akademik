package id.ac.tazkia.smilemahasiswa.dto.elearning;

import lombok.Data;

import java.math.BigDecimal;
import java.math.BigInteger;

@Data
public class MdlGradeItemsDto {

    private String id;
    private String idJadwal;
    private String namaTugas;
    private BigDecimal bobot;
    private String statusA;
    private BigInteger pertemuan;
    private String timeCreated;
    private String timeModified;


}
