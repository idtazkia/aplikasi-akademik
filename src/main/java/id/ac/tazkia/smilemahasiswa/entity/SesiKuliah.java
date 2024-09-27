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
    @GeneratedValue(generator = "uuid")
    @GenericGenerator(name = "uuid", strategy = "uuid2")
    private String id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_jadwal")
    private Jadwal jadwal;

    @Lob
    @Column(columnDefinition = "LONGTEXT")
    private String beritaAcara;

    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @NotNull
    private LocalDateTime waktuMulai = LocalDateTime.now();

    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @NotNull
    private LocalDateTime waktuSelesai = LocalDateTime.now();

    private String pertemuan;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_presensi_dosen")
    @NotNull
    private PresensiDosen presensiDosen;

    private Integer pertemuanKe;

    @Lob
    @Column(columnDefinition = "LONGTEXT")
    private String rps;

    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime tanggalInput;
    
    @JoinColumn(name = "id_detail_pertemuan")
    @ManyToOne
    private DetailPertemuan detailPertemuan;

    @Column(columnDefinition = "LONGTEXT")
    private String linkVideo;

    @Column(columnDefinition = "LONGTEXT")
    private String linkOnline;

    @Enumerated(EnumType.STRING)
    private StatusRecord ceramah;

    @Enumerated(EnumType.STRING)
    private StatusRecord diskusi;

    @Enumerated(EnumType.STRING)
    private StatusRecord presentasi;

    @Enumerated(EnumType.STRING)
    private StatusRecord tanyajawab;

    @Enumerated(EnumType.STRING)
    private StatusRecord kuis;

    @Enumerated(EnumType.STRING)
    private StatusRecord praktikum;

    @Column(name = "set_edit_sesi")
    private Boolean isEdit = Boolean.FALSE;

}
