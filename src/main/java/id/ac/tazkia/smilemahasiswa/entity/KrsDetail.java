package id.ac.tazkia.smilemahasiswa.entity;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;
import lombok.Data;
import org.hibernate.annotations.GenericGenerator;

import javax.persistence.*;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Entity
@Data
@JsonIdentityInfo(
        generator = ObjectIdGenerators.PropertyGenerator.class,
        property = "id")
public class KrsDetail extends Auditable {

    @Id
    @GeneratedValue(generator = "uuid" )
    @GenericGenerator(name = "uuid", strategy = "uuid2")
    private String id;

    @ManyToOne
    @JoinColumn(name = "id_krs")
    @JsonBackReference
    private Krs krs;

    @ManyToOne
    @JoinColumn(name = "id_mahasiswa")
    @JsonBackReference
    private Mahasiswa mahasiswa;

    @ManyToOne
    @JoinColumn(name = "id_jadwal")
    @JsonBackReference
    private Jadwal jadwal;

    @ManyToOne
    @JoinColumn(name = "id_matakuliah_kurikulum")
    @JsonBackReference
    private MatakuliahKurikulum matakuliahKurikulum;

    @NotNull
    private BigDecimal nilaiPresensi = BigDecimal.ZERO;

    @NotNull
    private BigDecimal nilaiUts = BigDecimal.ZERO;

    @NotNull
    private BigDecimal nilaiUas = BigDecimal.ZERO;

    @NotNull
    private BigDecimal nilaiTugas = BigDecimal.ZERO;

    @NotNull
    @JsonBackReference
    private String finalisasi;

    @NotNull @Enumerated(EnumType.STRING)
    @JsonBackReference
    private StatusRecord status = StatusRecord.AKTIF;

    private BigDecimal nilaiAkhir = BigDecimal.ZERO;
    private BigDecimal bobot;
    private String grade;

    @NotNull @Min(0)
    private Integer jumlahKehadiran;

    @NotNull @Min(0)
    private Integer jumlahTerlambat;

    @NotNull @Min(0)
    private Integer jumlahMangkir;

    @NotNull @Min(0)
    private Integer jumlahSakit;

    @NotNull @Min(0)
    @JsonBackReference
    private Integer jumlahIzin;

    @JsonBackReference
    private String kodeUts;
    @JsonBackReference
    private String kodeUas;

    @JsonBackReference
    private Integer e1;
    @JsonBackReference
    private Integer e2;
    @JsonBackReference
    private Integer e3;
    @JsonBackReference
    private Integer e4;
    @JsonBackReference
    private Integer e5;

    @Enumerated(EnumType.STRING)
    @JsonBackReference
    private StatusRecord statusEdom;

    @ManyToOne
    @JsonBackReference
    @JoinColumn(name = "id_tahun_akademik")
    private TahunAkademik tahunAkademik;

    @JsonBackReference
    private String keterangan;

    @Enumerated(EnumType.STRING)
    @JsonBackReference
    private StatusRecord statusKonversi;

    @JsonBackReference
    private BigDecimal nilaiUtsFinal = BigDecimal.ZERO;

    @JsonBackReference
    private BigDecimal nilaiUasFinal = BigDecimal.ZERO;

//    @OneToMany(mappedBy = "krsDetail")
//    private List<NilaiTugas> nilaiTugasList = new ArrayList<>();

    @Enumerated(EnumType.STRING)
    private StatusRecord requestKonversi;
}
