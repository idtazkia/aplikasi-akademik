package id.ac.tazkia.smilemahasiswa.entity;

import id.ac.tazkia.smilemahasiswa.constant.JenisTest;
import id.ac.tazkia.smilemahasiswa.constant.StatusUjian;
import lombok.Data;
import org.hibernate.annotations.GenericGenerator;
import org.springframework.format.annotation.DateTimeFormat;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Data
public class PrediksiTest {
    @Id
    @GeneratedValue(generator = "uuid")
    @GenericGenerator(name = "uuid", strategy = "uuid2")
    private String id;

    @ManyToOne
    @JoinColumn(name = "id_mahasiswa")
    private Mahasiswa mahasiswa;

    @ManyToOne
    @JoinColumn(name = "id_periode")
    private PeriodeTest periodeTest;

    private Integer ujianKe;
    private String fileBukti;
    private String fileTest;

    @NotNull
    @Enumerated(EnumType.STRING)
    private JenisTest jenisTest;

    private BigDecimal nilaiListening;
    private BigDecimal nilaiReading;
    private BigDecimal nilaiStructure;
    private BigDecimal nilaiWriting;
    private BigDecimal nilaiSpeaking;
    private BigDecimal nilai;

    @Enumerated(EnumType.STRING)
    private StatusUjian statusUjian= StatusUjian.WAITING;

    @NotNull
    @Enumerated(EnumType.STRING)
    private StatusRecord status = StatusRecord.WAITING;

    @Column(columnDefinition = "LONGTEXT")
    private String keterangan;

    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime tanggalUpload = LocalDateTime.now();

    @ManyToOne
    @JoinColumn(name = "id_tagihan")
    private Tagihan tagihan;

}
