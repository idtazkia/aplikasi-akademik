package id.ac.tazkia.smilemahasiswa.dto.payment;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Date;

@Data
@Builder
public class TagihanRequest {
    private String jenisTagihan;
    private String kodeBiaya;
    private String debitur;
    private BigDecimal nilaiTagihan;
    private String tahunAkademik;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    private Date tanggalJatuhTempo;
    private String keterangan;
    private Type jenisRequest = Type.CREATE;
    private String nomorTagihanLama;

    public enum Type{
        CREATE, REPLACE
    }
}
