package id.ac.tazkia.akademik.aplikasiakademik.entity;


import lombok.Data;
import org.hibernate.annotations.GenericGenerator;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.sql.Time;

@Entity
@Data
public class Jadwal {

    @Id
    @GeneratedValue(generator = "uuid" )
    @GenericGenerator(name = "uuid", strategy = "uuid2")
    private String id;

    @ManyToOne
    @JoinColumn(name = "id_matakuliah_kurikulum")
    private MatakuliahKurikulum idMatakuliahkurikulum;

    @ManyToOne
    @JoinColumn(name = "id_hari")
    private Hari idHari;

    private Time jamMulai;

    private Time jamSelesai;

    @ManyToOne
    @JoinColumn(name = "id_tahun_akademik")
    private TahunAkademik idTahunAkademik;

    @ManyToOne
    @JoinColumn(name = "id_tahun_akademik_prodi")
    private TahunAkademikProdi idTahunAkademikProdi;

    @ManyToOne
    @JoinColumn(name = "id_prodi")
    private Prodi idProdi;

//    @ManyToOne
//    @JoinColumn(name = "id_dosen_pengampu")
    private String idDosenPengampu;

    private BigDecimal bobotUts;

    private BigDecimal bobotUas;

    private BigDecimal bobotTugas;

    private BigDecimal bobotPresensi;

    @ManyToOne
    @JoinColumn(name = "id_ruangan")
    private Ruangan idRuangan;

//    @ManyToOne
//    @JoinColumn(name = "id_kelas")
    private String idKelas;

    private Integer jumlahSesi;

    private String  finalStatus;

    @NotNull
    @Enumerated(EnumType.STRING)
    private StatusRecord status = StatusRecord.AKTIF;


}
