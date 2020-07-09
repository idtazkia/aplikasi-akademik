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
@Table(name = "pembayaran")
public class Pembayaran {
    @Id
    @GeneratedValue(generator = "uuid" )
    @GenericGenerator(name = "uuid", strategy = "uuid2")
    private String id;

    @ManyToOne
    @JoinColumn(name = "id_tagihan")
    private Tagihan tagihan;

    @NotNull
    private LocalDate waktuBayar = LocalDate.now();

    @ManyToOne
    @JoinColumn(name = "id_bank")
    private Bank bank;

    @NotNull
    @NotEmpty
    private String nomorRekening;

    @Min(0) @NotNull
    private BigDecimal amount;

    private String referensi;

    @Enumerated(EnumType.STRING)
    private StatusRecord status = StatusRecord.AKTIF;

}
