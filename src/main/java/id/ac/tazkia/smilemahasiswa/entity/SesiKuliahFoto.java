package id.ac.tazkia.smilemahasiswa.entity;

import lombok.Data;
import org.hibernate.annotations.GenericGenerator;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Data
public class SesiKuliahFoto {

    @Id
    @GeneratedValue(generator = "uuid")
    @GenericGenerator(name = "uuid", strategy = "uuid2")
    @Column(length = 45)
    private String id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_jadwal")
    private Jadwal jadwal;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_sesi_kuliah")
    private SesiKuliah sesiKuliah;

    @Lob
    @Column(columnDefinition = "LONGTEXT")
    private String namaFile;

    @Lob
    @Column(columnDefinition = "LONGTEXT")
    private String file;

    @Column(length = 10)
    @Enumerated(EnumType.STRING)
    private StatusRecord status = StatusRecord.AKTIF;

    private LocalDateTime tanggalUpload;

    private LocalDateTime tanggalDelete;

    @Column(length = 45)
    private String userUpload;

    @Column(length = 45)
    private String userDelete;

}
