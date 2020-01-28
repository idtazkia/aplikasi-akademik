package id.ac.tazkia.smilemahasiswa.entity;


import lombok.Data;
import org.hibernate.annotations.GenericGenerator;
import org.springframework.format.annotation.DateTimeFormat;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;

@Entity
@Data
public class SesiKuliah {
    @Id
    @GeneratedValue(generator = "uuid" )
    @GenericGenerator(name = "uuid", strategy = "uuid2")
    private String id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_jadwal")
    private Jadwal jadwal;

    @Lob
    @Column(columnDefinition = "LONGTEXT")
    private String beritaAcara;

    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @NotNull private LocalDateTime waktuMulai = LocalDateTime.now();

    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @NotNull private LocalDateTime waktuSelesai = LocalDateTime.now();

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_presensi_dosen")
    @NotNull
    private PresensiDosen presensiDosen;
}
