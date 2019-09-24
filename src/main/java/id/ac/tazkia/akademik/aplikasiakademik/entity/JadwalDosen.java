package id.ac.tazkia.akademik.aplikasiakademik.entity;

import lombok.Data;
import org.hibernate.annotations.GenericGenerator;

import javax.persistence.*;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;

@Entity
@Data
public class JadwalDosen {
    @Id
    @GeneratedValue(generator = "uuid" )
    @GenericGenerator(name = "uuid", strategy = "uuid2")
    private String id;

    @NotNull
    @ManyToOne
    @JoinColumn(name = "id_jadwal")
    private Jadwal jadwal;

    @NotNull
    @ManyToOne
    @JoinColumn(name = "id_dosen")
    private Dosen dosen;

    @NotNull
    @Enumerated(EnumType.STRING)
    private StatusJadwalDosen statusJadwalDosen;

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

}
