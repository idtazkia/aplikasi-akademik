package id.ac.tazkia.smilemahasiswa.entity;

import lombok.Data;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.Where;
import org.springframework.format.annotation.DateTimeFormat;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;

@Entity
@Data
@SQLDelete(sql = "UPDATE presensi_mahasiswa_bahasa SET status = 'HAPUS' WHERE id=?")
@Where(clause = "status = 'AKTIF'")
public class PresensiMahasiswaBahasa {
    @Id
    @GeneratedValue(generator = "uuid" )
    @GenericGenerator(name = "uuid", strategy = "uuid2")
    private  String id;

    @ManyToOne
    @JoinColumn(name = "id_presensi_dosen_bahasa")
    private PresensiDosenBahasa presensiDosenBahasa;

    @ManyToOne
    @JoinColumn(name = "id_mahasiswa")
    private Mahasiswa mahasiswa;

    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime waktuMasuk;

    @NotNull
    @Enumerated(EnumType.STRING)
    private StatusPresensi statusPresensi;

    @NotNull
    @Enumerated(EnumType.STRING)
    private StatusRecord status = StatusRecord.AKTIF;


}
