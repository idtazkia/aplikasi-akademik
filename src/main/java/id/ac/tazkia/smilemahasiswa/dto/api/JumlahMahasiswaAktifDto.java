package id.ac.tazkia.smilemahasiswa.dto.api;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class JumlahMahasiswaAktifDto {

    private String kodeProdi;

    private Integer pria;

    private Integer wanita;

    private Integer total;

}
