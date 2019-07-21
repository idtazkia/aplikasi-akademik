package id.ac.tazkia.akademik.aplikasiakademik.entity;

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
public class KrsDetail {

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

}
