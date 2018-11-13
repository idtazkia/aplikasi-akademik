package id.ac.tazkia.akademik.aplikasiakademik.entity;

import lombok.Data;
import org.hibernate.annotations.GenericGenerator;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Data
public class BipotBayarMahasiswa {
    @Id
    @GeneratedValue(generator = "uuid")
    @GenericGenerator(name = "uuid", strategy = "uuid2")
    private String id;

    @ManyToOne
    @NotNull
    @JoinColumn(name = "id_tahun_akademik")
    private TahunAkademik tahunAkademik;

    @ManyToOne
    @NotNull
    @JoinColumn(name = "id_rekening")
    private RekeningBank rekeningBank;

    private String idPmb;

    @ManyToOne
    @NotNull
    @JoinColumn(name = "id_bipot_tagihan_mahasiswa")
    private BipotTagihanMahasiswa bipotTagihanMahasiswa;

    @NotNull
    private String autoDebet;

    @NotNull
    private BigDecimal perkalian;

    private String idPmbMahasiswa;

    private String bank;

    private String buktiSetoran;

    private LocalDateTime waktuBayar;

    private BigDecimal jumlah;

    private String idCicilan;

    private String keterangan;

    @Enumerated(EnumType.STRING)
    @NotNull
    private StatusRecord status;

}
