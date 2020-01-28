package id.ac.tazkia.smilemahasiswa.entity;

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
    private Integer jumlahKehadiran = 0;

    @NotNull @Min(0)
    private Integer jumlahTerlambat = 0;

    @NotNull @Min(0)
    private Integer jumlahMangkir = 0;

    @NotNull @Min(0)
    private Integer jumlahSakit = 0;

    @NotNull @Min(0)
    private Integer jumlahIzin = 0;

}
