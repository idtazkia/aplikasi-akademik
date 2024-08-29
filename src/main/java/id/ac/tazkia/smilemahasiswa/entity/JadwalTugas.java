package id.ac.tazkia.smilemahasiswa.entity;

import lombok.Data;
import org.hibernate.annotations.GenericGenerator;
import org.springframework.format.annotation.DateTimeFormat;

import javax.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Data
public class JadwalTugas {

    @Id
    @GeneratedValue(generator = "uuid" )
    @GenericGenerator(name = "uuid", strategy = "uuid2")
    private String id;

    @ManyToOne
    @JoinColumn(name = "id_sesi_kuliah")
    private SesiKuliah sesiKuliah;

    private Integer pertemuanKe;

    private BigDecimal bobot;

    private String namaTugas;

    @Lob
    @Column(columnDefinition = "LONGTEXT")
    private String deskripsi;

    @Lob
    @Column(columnDefinition = "LONGTEXT")
    private String petunjukAktivitas;

    private String file;

    private String namaFile;

    @Column(columnDefinition = "DATE")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime tenggatWaktu;

    @Enumerated(EnumType.STRING)
    private JenisPengiriman jenisPengiriman;

    private Integer maksFileUpload;

    @Lob
    @Column(columnDefinition = "LONGTEXT")
    private String pertanyaan;

    @Enumerated(EnumType.STRING)
    private StatusRecord status = StatusRecord.AKTIF;

    public enum JenisPengiriman{
        ONLINE_TEKS, UPLOAD_FILE, ONLINE_TEKS_UPLOAD_FILE
    }

}
