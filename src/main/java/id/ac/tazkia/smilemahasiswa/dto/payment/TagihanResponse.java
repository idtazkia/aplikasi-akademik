package id.ac.tazkia.smilemahasiswa.dto.payment;

import com.fasterxml.jackson.annotation.JsonInclude;
import id.ac.tazkia.smilemahasiswa.entity.Mahasiswa;
import id.ac.tazkia.smilemahasiswa.entity.NilaiJenisTagihan;
import id.ac.tazkia.smilemahasiswa.entity.TahunAkademik;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class TagihanResponse {
    private Boolean sukses;
    private String error;
    private String debitur;
    private Mahasiswa mahasiswa;
    private NilaiJenisTagihan jenisTagihan;
    private String kodeBiaya;
    private TahunAkademik tahunAkademik;
    private BigDecimal nilaiTagihan;
    private LocalDate tanggalTagihan;
    private LocalDate tanggalJatuhTempo;
    private String nomorTagihan;
    private String keterangan;
}
