package id.ac.tazkia.smilemahasiswa.entity;


import lombok.Data;
import org.hibernate.annotations.GenericGenerator;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Data
public class ProsesBackgroundAbsensiElearning {
    @Id
    @GeneratedValue(generator = "uuid" )
    @GenericGenerator(name = "uuid", strategy = "uuid2")
    private String id;

    private String namaProses;

    private LocalDateTime tanggalMulai;

    private LocalDateTime tanggalSelesai;

    @Enumerated(EnumType.STRING)
    private StatusRecord status = StatusRecord.AKTIF;

    private String keterangan;

    private String tahunAkademik;

    @ManyToOne
    @JoinColumn(name = "id_prodi")
    private Prodi prodi;

    private LocalDateTime tanggalInput;
}
