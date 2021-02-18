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
import java.time.LocalDateTime;

@Entity
@Data
public class RequestCicilan {

    @Id @GeneratedValue(generator = "uuid")
    @GenericGenerator(name = "uuid", strategy = "uuid2")
    private String id;

    @ManyToOne
    @JoinColumn(name = "id_tagihan")
    private Tagihan tagihan;

    @NotNull
    @Min(0)
    private BigDecimal nilaiCicilan;

    @Column(columnDefinition = "DATE")
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDate tanggalJatuhTempo;

    @Enumerated(EnumType.STRING)
    private StatusApprove statusApprove = StatusApprove.WAITING;

    private LocalDateTime waktuApprove;

    @ManyToOne
    @JoinColumn(name = "user_approve")
    private Karyawan userApprove;

    @Enumerated(EnumType.STRING)
    private StatusRecord status = StatusRecord.AKTIF;

    @Enumerated(EnumType.STRING)
    private StatusTagihan statusCicilan = StatusTagihan.CICILAN_1;

    private String keterangan;

}
