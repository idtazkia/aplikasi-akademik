package id.ac.tazkia.smilemahasiswa.dto.api;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class RateMahasiswaDto {

    private String bulan;
    private BigDecimal rate;

}
