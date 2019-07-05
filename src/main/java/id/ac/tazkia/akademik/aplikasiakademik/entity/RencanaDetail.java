package id.ac.tazkia.akademik.aplikasiakademik.entity;

import lombok.Data;
import org.hibernate.annotations.GenericGenerator;

import javax.persistence.*;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import java.math.BigDecimal;

@Data @Entity
@Table(name = "rencana_pembayaran_detail")
public class RencanaDetail {
    @Id
    @GeneratedValue(generator = "uuid")
    @GenericGenerator(name = "uuid", strategy = "uuid2")
    private String id;

    @ManyToOne
    @JoinColumn(name = "id_rencana_pembayaran")
    private RencanaPembayaran rencanaPembayaran;

    private String pembayaranKe;

    @ManyToOne
    @JoinColumn(name = "id_tagihan")
    private TagihanMahasiswa tagihanMahasiswa;

    @Min(0)
    private BigDecimal amount;

    @NotNull
    @Enumerated(EnumType.STRING)
    private StatusRecord statusBayar;

    @Enumerated(EnumType.STRING)
    private StatusRecord lunas;
    private String keterangan;
}
