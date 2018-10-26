package id.ac.tazkia.akademik.aplikasiakademik.entity;

import lombok.Data;
import org.hibernate.annotations.GenericGenerator;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.sql.Time;
import java.time.LocalDateTime;
import java.util.Date;

@Entity
@Data
public class PresensiDosen {

    @Id
    @GeneratedValue(generator = "uuid" )
    @GenericGenerator(name = "uuid", strategy = "uuid2")
    private String id;

    @ManyToOne
    @JoinColumn(name = "id_tahun_akademik")
    private TahunAkademik idTahunAkademik;

    @ManyToOne @JoinColumn(name = "id_sesi_kuliah")
    private SesiKuliah sesiKuliah;

    @ManyToOne
    @JoinColumn(name = "id_jadwal")
    private Jadwal idJadwal;

    private LocalDateTime waktuMasuk;
    private LocalDateTime waktuSelesai;

    @Enumerated(EnumType.STRING) @NotNull
    private StatusPresensi statusPresensi;

    @NotNull
    @Enumerated(EnumType.STRING)
    private StatusRecord status = StatusRecord.AKTIF;

}
