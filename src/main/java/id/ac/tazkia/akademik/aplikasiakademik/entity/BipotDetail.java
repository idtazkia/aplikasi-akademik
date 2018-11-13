package id.ac.tazkia.akademik.aplikasiakademik.entity;

import lombok.Data;
import org.hibernate.annotations.GenericGenerator;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.math.BigDecimal;

@Data
@Entity
public class BipotDetail {
    @Id
    @GeneratedValue(generator = "uuid")
    @GenericGenerator(name = "uuid", strategy = "uuid2")
    private String id;

    @ManyToOne
    @NotNull
    @JoinColumn(name = "id_bipot")
    private Bipot bipot;

    @ManyToOne
    @NotNull
    @JoinColumn(name = "id_nama_bipot")
    private BipotNama bipotNama;

    @NotNull
    private String prioritas;

    @NotNull
    private BigDecimal jumlah;

    @NotNull
    private BigDecimal kaliBayar;

    @NotNull
    private String dihitungSks;

    @NotNull
    private String otomatis;

    @NotNull
    @Enumerated(EnumType.STRING)
    private StatusRecord statusMahasiswaAktif;

    @NotNull
    @Enumerated(EnumType.STRING)
    private StatusRecord statusAwalMahasiswa;

    @NotNull
    private String gunakangradeNilai;

    @NotNull
    private BigDecimal gradeNilai;

    @NotNull
    @Enumerated(EnumType.STRING)
    private StatusRecord status;




}
