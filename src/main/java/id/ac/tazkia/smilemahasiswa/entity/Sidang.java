package id.ac.tazkia.smilemahasiswa.entity;

import lombok.Data;
import org.hibernate.annotations.GenericGenerator;
import org.springframework.format.annotation.DateTimeFormat;

import javax.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;

@Data
@Entity
public class Sidang {

    @Id
    @GeneratedValue(generator = "uuid")
    @GenericGenerator(name = "uuid" ,strategy = "uuid2")
    private String id;

    @Lob
    private String judulTugasAkhir;

    @Lob
    private String judulInggris;

    @ManyToOne
    @JoinColumn(name = "id_seminar")
    private Seminar seminar;

    @ManyToOne
    @JoinColumn(name = "id_tahun_akademik")
    private TahunAkademik tahunAkademik;

    private String fileSidang;

    private String filePendaftaran;

    private String fileIjazah;
    private String fileKtp;
    private String fileBimbingan;
    private String fileTurnitin;
    private String filePersetujuan;

    @Column(columnDefinition = "DATE")
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDate tanggalInput;

    @ManyToOne
    @JoinColumn(name = "ketua_penguji")
    private Dosen ketuaPenguji;

    @ManyToOne
    @JoinColumn(name = "dosen_penguji")
    private Dosen dosenPenguji;

    @ManyToOne
    @JoinColumn(name = "pembimbing")
    private Dosen pembimbing;

    @Column(columnDefinition = "DATE")
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDate tanggalUjian;

    @Column(columnDefinition = "TIME")
    @DateTimeFormat(pattern = "HH:mm")
    private LocalTime jamMulai;

    @Column(columnDefinition = "TIME")
    @DateTimeFormat(pattern = "HH:mm")
    private LocalTime jamSelesai;

    @ManyToOne
    @JoinColumn(name = "ruangan")
    private Ruangan ruangan;

    @Column(name = "nilai_a")
    private BigDecimal nilaiA = BigDecimal.ZERO;

    @Column(name = "nilai_b")
    private BigDecimal nilaiB = BigDecimal.ZERO;

    @Column(name = "nilai_c")
    private BigDecimal nilaiC = BigDecimal.ZERO;

    @Column(name = "nilai_d")
    private BigDecimal nilaiD = BigDecimal.ZERO;

    private BigDecimal nilai = BigDecimal.ZERO;

    @Lob
    @Column(columnDefinition = "LONGTEXT")
    private String beritaAcara;

    private BigDecimal ka = BigDecimal.ZERO;
    private BigDecimal kb = BigDecimal.ZERO;
    private BigDecimal kc = BigDecimal.ZERO;
    private BigDecimal kd = BigDecimal.ZERO;

    @Lob
    @Column(columnDefinition = "LONGTEXT")
    private String komentarKetua;

    private BigDecimal ua = BigDecimal.ZERO;
    private BigDecimal ub = BigDecimal.ZERO;
    private BigDecimal uc = BigDecimal.ZERO;
    private BigDecimal ud = BigDecimal.ZERO;

    @Lob
    @Column(columnDefinition = "LONGTEXT")
    private String komentarPenguji;


    private BigDecimal pa = BigDecimal.ZERO;
    private BigDecimal pb = BigDecimal.ZERO;
    private BigDecimal pc = BigDecimal.ZERO;
    private BigDecimal pd = BigDecimal.ZERO;

    @Lob
    @Column(columnDefinition = "LONGTEXT")
    private String komentarPembimbing;

    @Lob
    @Column(columnDefinition = "LONGTEXT")
    private String komentarAkademik;

    @Lob
    @Column(columnDefinition = "LONGTEXT")
    private String komentarProdi;

    @Enumerated(EnumType.STRING)
    private StatusApprove akademik;

    @Enumerated(EnumType.STRING)
    private StatusApprove statusSidang;

    @Enumerated(EnumType.STRING)
    private StatusRecord publish;



}
