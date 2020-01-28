package id.ac.tazkia.smilemahasiswa.entity;

import lombok.Data;
import org.hibernate.annotations.GenericGenerator;

import javax.persistence.*;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;

@Entity
@Data
public class RekapKehadiranMahasiswa {
    @Id
    @GeneratedValue(generator = "uuid" )
    @GenericGenerator(name = "uuid", strategy = "uuid2")
    private String id;

    @NotNull
    @ManyToOne
    @JoinColumn(name = "id_mahasiswa")
    private Mahasiswa mahasiswa;

    @NotNull
    @ManyToOne
    @JoinColumn(name = "id_jadwal")
    private Jadwal jadwal;

    @NotNull @Min(0)
    private Integer hadir = 0;

    @NotNull @Min(0)
    private Integer mangkir = 0;

    @NotNull @Min(0)
    private Integer sakit = 0;

    @NotNull @Min(0)
    private Integer izin = 0;

    @NotNull @Min(0)
    private Integer terlambat = 0;

    @NotNull @Min(0)
    private Integer lainLain = 0;

    @NotNull
    private LocalDateTime terakhirUpdate;
}
