package id.ac.tazkia.akademik.aplikasiakademik.entity;

import lombok.Data;
import org.hibernate.annotations.GenericGenerator;

import javax.persistence.*;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import java.math.BigDecimal;

@Entity
@Data
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
    private String nilaiPresensi;

    @NotNull @Min(0)
    private BigDecimal nilaiUts;

    @NotNull @Min(0)
    private BigDecimal nilaiUas;

    @NotNull @Min(0)
    private BigDecimal nilaiTugas;

    @NotNull
    private String finalisasi;

    @NotNull @Enumerated(EnumType.STRING)
    private StatusRecord status = StatusRecord.AKTIF;
}
