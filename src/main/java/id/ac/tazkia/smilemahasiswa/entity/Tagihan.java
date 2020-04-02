package id.ac.tazkia.smilemahasiswa.entity;

import lombok.Data;
import org.hibernate.annotations.GenericGenerator;

import javax.persistence.*;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Data
@Table(name = "tagihan")
public class Tagihan {
    @Id
    @GeneratedValue(generator = "uuid")
    @GenericGenerator(name = "uuid", strategy = "uuid2")
    private String id;

    @NotNull
    private StatusRecord statusRecord = StatusRecord.AKTIF;

    @NotNull @NotEmpty
    private String nomor;

    @NotNull
    @ManyToOne @JoinColumn(name = "id_jenis_tagihan")
    private JenisTagihan jenisTagihan;

    @NotNull
    @ManyToOne @JoinColumn(name = "id_mahasiswa")
    private Mahasiswa mahasiswa;

    @NotNull @NotEmpty
    private String keterangan;

    @NotNull @Min(1000)
    private BigDecimal nilaiTagihan;

    @NotNull
    private LocalDate tanggalPembuatan;

    @NotNull
    private LocalDate tanggalJatuhTempo;

    @NotNull
    private Boolean lunas = false;
}
