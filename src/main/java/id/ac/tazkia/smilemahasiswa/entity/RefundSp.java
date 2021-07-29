package id.ac.tazkia.smilemahasiswa.entity;

import lombok.Data;
import org.hibernate.annotations.GenericGenerator;

import javax.persistence.*;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Entity
public class RefundSp {
    @Id
    @GeneratedValue(generator = "uuid")
    @GenericGenerator(name = "uuid", strategy = "uuid2")
    private String id;

    @ManyToOne
    @JoinColumn(name = "id_mahasiswa")
    private Mahasiswa mahasiswa;

    @ManyToOne
    @JoinColumn(name = "id_pembayaran")
    private Pembayaran pembayaran;

    @ManyToOne
    @JoinColumn(name = "id_pra_krs_sp")
    private PraKrsSp praKrsSp;

    @ManyToOne
    @JoinColumn(name = "id_tagihan")
    private Tagihan tagihan;

    @NotNull
    private String nomorRekening;

    private String namaBank;

    @Min(0)
    private BigDecimal jumlah;

    private String nomorTelepon;

    @Enumerated(EnumType.STRING)
    private StatusRecord status = StatusRecord.AKTIF;

    @Enumerated(EnumType.STRING)
    private StatusRecord statusPengembalian = StatusRecord.DONE;

    @ManyToOne
    @JoinColumn(name = "user_update")
    private Karyawan userUpdate;

    @JoinColumn(name = "time_update")
    private LocalDateTime timeUpdate = LocalDateTime.now();

}
