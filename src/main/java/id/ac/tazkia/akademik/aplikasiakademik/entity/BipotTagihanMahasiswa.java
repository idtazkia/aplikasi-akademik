package id.ac.tazkia.akademik.aplikasiakademik.entity;

import lombok.Data;
import org.hibernate.annotations.GenericGenerator;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.math.BigDecimal;

@Entity
@Data
public class BipotTagihanMahasiswa {
    @Id
    @GeneratedValue(generator = "uuid")
    @GenericGenerator(name = "uuid", strategy = "uuid2")
    private String id;

    @ManyToOne
    @NotNull
    @JoinColumn(name = "id_mahasiswa")
    private Mahasiswa mahasiswa;

    @ManyToOne
    @NotNull
    @JoinColumn(name = "id_tahun_akademik")
    private TahunAkademik tahunAkademik;

    @ManyToOne
    @NotNull
    @JoinColumn(name = "id_tahun_akademik_prodi")
    private TahunAkademikProdi tahunAkademikProdi;

    @ManyToOne
    @NotNull
    @JoinColumn(name = "id_detail_bipot")
    private BipotDetail bipotDetail;

    @NotNull
    private BigDecimal perkalian;

    @NotNull
    private BigDecimal jumlah;

    @NotNull
    private BigDecimal besar;

    @NotNull
    private BigDecimal dibayar;

    private String dispensasi;
    private String nomorDispensasi;
    private String catatan;

    @NotNull
    @Enumerated(EnumType.STRING)
    private StatusRecord status =  StatusRecord.AKTIF;
}
