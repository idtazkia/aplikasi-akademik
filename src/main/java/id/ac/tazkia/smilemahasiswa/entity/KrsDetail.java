package id.ac.tazkia.smilemahasiswa.entity;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;
import lombok.Data;
import org.hibernate.annotations.GenericGenerator;

import javax.persistence.*;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import java.math.BigDecimal;

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
    private Krs krs;

    @ManyToOne
    @JoinColumn(name = "id_mahasiswa")
    private Mahasiswa mahasiswa;

    @ManyToOne
    @JoinColumn(name = "id_jadwal")
    private Jadwal jadwal;

    @ManyToOne
    @JoinColumn(name = "id_matakuliah_kurikulum")
    private MatakuliahKurikulum matakuliahKurikulum;

    @NotNull
    private BigDecimal nilaiPresensi;

    @NotNull
    private BigDecimal nilaiUts;

    @NotNull
    private BigDecimal nilaiUas;

    @NotNull
    private BigDecimal nilaiTugas;

    @NotNull
    private String finalisasi;

    @NotNull @Enumerated(EnumType.STRING)
    private StatusRecord status = StatusRecord.AKTIF;

    private BigDecimal nilaiAkhir;
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
    private Integer jumlahIzin;

    private String kodeUts;
    private String kodeUas;

    private Integer e1;
    private Integer e2;
    private Integer e3;
    private Integer e4;
    private Integer e5;

    @Enumerated(EnumType.STRING)
    private StatusRecord statusEdom;
}
