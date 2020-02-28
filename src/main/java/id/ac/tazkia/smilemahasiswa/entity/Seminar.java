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

    @Column(columnDefinition = "DATE")
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDate tanggalUjian;

    @Column(columnDefinition = "TIME")
    @DateTimeFormat(pattern = "HH:mm:ss")
    private LocalTime jam;

    @ManyToOne
    @JoinColumn(name = "ruangan")
    private Ruangan ruangan;

    private BigDecimal nilai;

    @Lob
    @Column(columnDefinition = "LONGTEXT")
    private String beritaAcara;

    private BigDecimal ka;
    private BigDecimal kb;
    private BigDecimal kc;
    private BigDecimal kd;
    private BigDecimal ke;
    private BigDecimal kf;

    @Lob
    @Column(columnDefinition = "LONGTEXT")
    private String komentarKetua;

    private BigDecimal ua;
    private BigDecimal ub;
    private BigDecimal uc;
    private BigDecimal ud;
    private BigDecimal ue;
    private BigDecimal uf;

    @Lob
    @Column(columnDefinition = "LONGTEXT")
    private String komentarPenguji;

    private BigDecimal pa;
    private BigDecimal pb;
    private BigDecimal pc;
    private BigDecimal pd;
    private BigDecimal pe;
    private BigDecimal pf;

    @Enumerated(EnumType.STRING)
    private StatusRecord statusSempro;

    private String publish;

    @Lob
    @Column(columnDefinition = "LONGTEXT")
    private String komentarDosen1;

    @Lob
    @Column(columnDefinition = "LONGTEXT")
    private String komentarDosen2;

    @Lob
    @Column(columnDefinition = "LONGTEXT")
    private String komentarDosen3;

}
