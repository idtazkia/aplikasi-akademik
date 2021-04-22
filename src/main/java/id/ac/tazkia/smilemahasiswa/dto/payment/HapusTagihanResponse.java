package id.ac.tazkia.smilemahasiswa.dto.payment;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class HapusTagihanResponse {
    private String jenisTagihan;
    private String kodeBiaya;
    private String debitur;
    private String nomorTagihan;
    private BigDecimal nilai;
    private Boolean sukses;
    private String keterangan;
}
