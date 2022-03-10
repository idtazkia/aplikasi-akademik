package id.ac.tazkia.smilemahasiswa.entity;

import lombok.Data;
import org.hibernate.annotations.GenericGenerator;
import org.springframework.format.annotation.DateTimeFormat;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;

@Data
@Entity
public class Seminar {

    @Id
    @GeneratedValue(generator = "uuid")
    @GenericGenerator(name = "uuid" ,strategy = "uuid2")
    private String id;


    @ManyToOne
    @JoinColumn(name = "id_note")
    private Note note;

    @ManyToOne
    @JoinColumn(name = "id_tahun_akademik")
    private TahunAkademik tahunAkademik;

    private String filePengesahan;

    private String fileBimbingan;

    private String fileSkripsi;
    private String fileFormulir;
    private String fileTurnitin;
    private String fileCoverNote;

    @Column(columnDefinition = "DATE")
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDate tanggalInput;

    @Enumerated(EnumType.STRING)
    private StatusApprove status;

    @ManyToOne
    @JoinColumn(name = "user")
    private User userApprove;

    @ManyToOne
    @JoinColumn(name = "ketua_penguji")
    private Dosen ketuaPenguji;

    @ManyToOne
    @JoinColumn(name = "dosen_penguji")
    private Dosen dosenPenguji;

    @ManyToOne
    @JoinColumn(name = "penguji_eksternal")
    private Dosen penguji;

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

    private BigDecimal nilai = BigDecimal.ZERO;

    @Lob
    @Column(columnDefinition = "LONGTEXT")
    private String beritaAcara;

    private BigDecimal ka = BigDecimal.ZERO;
    private BigDecimal kb = BigDecimal.ZERO;
    private BigDecimal kc = BigDecimal.ZERO;
    private BigDecimal kd = BigDecimal.ZERO;
    private BigDecimal ke = BigDecimal.ZERO;
    private BigDecimal kf = BigDecimal.ZERO;

    @Lob
    @Column(columnDefinition = "LONGTEXT")
    private String komentarKetua;

    private BigDecimal ua = BigDecimal.ZERO;
    private BigDecimal ub = BigDecimal.ZERO;
    private BigDecimal uc = BigDecimal.ZERO;
    private BigDecimal ud = BigDecimal.ZERO;
    private BigDecimal ue = BigDecimal.ZERO;
    private BigDecimal uf = BigDecimal.ZERO;

    @Lob
    @Column(columnDefinition = "LONGTEXT")
    private String komentarPenguji;

    @Lob
    @Column(columnDefinition = "LONGTEXT")
    private String komentarPembimbing;

    @Lob
    @Column(columnDefinition = "LONGTEXT")
    private String komentarPembimbing2;

    @Lob
    @Column(columnDefinition = "LONGTEXT")
    private String komentarPembimbing3;

    private BigDecimal pa = BigDecimal.ZERO;
    private BigDecimal pb = BigDecimal.ZERO;
    private BigDecimal pc = BigDecimal.ZERO;
    private BigDecimal pd = BigDecimal.ZERO;
    private BigDecimal pe = BigDecimal.ZERO;
    private BigDecimal pf = BigDecimal.ZERO;

    private BigDecimal pa2 = BigDecimal.ZERO;
    private BigDecimal pb2 = BigDecimal.ZERO;
    private BigDecimal pc2 = BigDecimal.ZERO;
    private BigDecimal pd2 = BigDecimal.ZERO;
    private BigDecimal pe2 = BigDecimal.ZERO;
    private BigDecimal pf2 = BigDecimal.ZERO;


    private BigDecimal ea = BigDecimal.ZERO;
    private BigDecimal eb = BigDecimal.ZERO;
    private BigDecimal ec = BigDecimal.ZERO;
    private BigDecimal ed = BigDecimal.ZERO;
    private BigDecimal ee = BigDecimal.ZERO;
    private BigDecimal ef = BigDecimal.ZERO;

    @Enumerated(EnumType.STRING)
    private StatusApprove statusSempro;

    private String publish = "NONAKTIF";

    @Lob
    @Column(columnDefinition = "LONGTEXT")
    private String komentarDosen1;

    @Lob
    @Column(columnDefinition = "LONGTEXT")
    private String komentarDosen2;

    @Lob
    @Column(columnDefinition = "LONGTEXT")
    private String komentarDosen3;

    @Column(name = "nilai_a")
    private BigDecimal nilaiA = BigDecimal.ZERO;

    @Column(name = "nilai_b")
    private BigDecimal nilaiB = BigDecimal.ZERO;

    @Column(name = "nilai_c")
    private BigDecimal nilaiC = BigDecimal.ZERO;

    @Column(name = "nilai_d")
    private BigDecimal nilaiD = BigDecimal.ZERO;

    @Column(name = "nilai_e")
    private BigDecimal nilaiE = BigDecimal.ZERO;

    @Column(name = "nilai_f")
    private BigDecimal nilaiF = BigDecimal.ZERO;

}
