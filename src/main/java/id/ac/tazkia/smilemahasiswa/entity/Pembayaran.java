package id.ac.tazkia.smilemahasiswa.entity;

import lombok.Data;
import org.hibernate.annotations.GenericGenerator;
import org.springframework.format.annotation.DateTimeFormat;

import javax.persistence.*;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Data
@Table(name = "pembayaran_mahasiswa")
public class Pembayaran {
    @Id
    @GeneratedValue(generator = "uuid" )
    @GenericGenerator(name = "uuid", strategy = "uuid2")
    private String id;

    @ManyToOne
    @JoinColumn(name = "id_tagihan")
    private TagihanMahasiswa tagihanMahasiswa;

    @Column(columnDefinition = "DATE")
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDate waktuBayar;

    @NotNull
    @NotEmpty
    private String nomorRekening;

    @Min(0)
    private BigDecimal amount;

    private String referensi;

    @ManyToOne
    @JoinColumn(name = "id_rencana_pembayaran")
    private RencanaPembayaran rencanaPembayaran;
}
