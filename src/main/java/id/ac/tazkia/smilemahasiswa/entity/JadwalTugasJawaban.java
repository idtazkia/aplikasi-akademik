package id.ac.tazkia.smilemahasiswa.entity;

import lombok.Data;
import org.hibernate.annotations.GenericGenerator;

import javax.persistence.*;
import javax.validation.constraints.Max;
import javax.validation.constraints.Size;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Data
public class JadwalTugasJawaban {

    @Id
    @GeneratedValue(generator = "uuid" )
    @GenericGenerator(name = "uuid", strategy = "uuid2")
    private String id;

    @ManyToOne
    @JoinColumn(name = "id_mahasiswa")
    private Mahasiswa mahasiswa;

    @ManyToOne
    @JoinColumn(name = "id_jadwal_tugas")
    private JadwalTugas jadwalTugas;

    @Lob
    @Column(columnDefinition = "LONGTEXT")
    private String jawabanTeks;

    private String namaFile;

    private String file;

    @Max(100)
    private BigDecimal nilai;

    @Enumerated(EnumType.STRING)
    private StatusRecord status = StatusRecord.AKTIF;

    @Column(columnDefinition = "DATETIME")
    private LocalDateTime waktuPengumpulan = LocalDateTime.now();

}
