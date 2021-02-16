package id.ac.tazkia.smilemahasiswa.dto.payment;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class HapusTagihanRequest {
    private String jenisTagihan;
    private String kodeBiaya;
    private String debitur;
    private String nomorTagihan;
}
