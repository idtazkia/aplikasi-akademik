package id.ac.tazkia.akademik.aplikasiakademik.entity;

import lombok.Data;
import org.hibernate.annotations.GenericGenerator;

import javax.persistence.*;
import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import java.sql.Time;
import java.time.LocalDateTime;
import java.util.Date;

@Entity
@Data
public class PresensiMahasiswa {

    @Id
    @GeneratedValue(generator = "uuid" )
    @GenericGenerator(name = "uuid", strategy = "uuid2")
    private String id;

    @ManyToOne
    @JoinColumn(name = "id_krs_detail")
    private KrsDetail krsDetail;

    @ManyToOne @JoinColumn(name = "id_presensi_dosen")
    private PresensiDosen presensiDosen;

    @ManyToOne @JoinColumn(name = "id_sesi_kuliah")
    private SesiKuliah sesiKuliah;

    @ManyToOne
    @JoinColumn(name = "id_mahasiswa")
    private Mahasiswa mahasiswa;

    private LocalDateTime waktuMasuk;
    private LocalDateTime waktuKeluar;

    @NotNull
    @Enumerated(EnumType.STRING)
    private StatusPresensi statusPresensi;

    private String catatan;

    @Min(1) @Max(5)
    private Integer rating = 3;

    @NotNull
    @Enumerated(EnumType.STRING)
    private StatusRecord status = StatusRecord.AKTIF;

}
