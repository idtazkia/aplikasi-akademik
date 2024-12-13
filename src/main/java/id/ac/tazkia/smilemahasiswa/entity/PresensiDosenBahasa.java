package id.ac.tazkia.smilemahasiswa.entity;

import id.ac.tazkia.smilemahasiswa.constant.JenisTest;
import lombok.Data;
import org.hibernate.annotations.GenericGenerator;
import org.springframework.format.annotation.DateTimeFormat;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;
import java.time.LocalTime;

@Entity
@Data
public class PresensiDosenBahasa {
    @Id
    @GeneratedValue(generator = "uuid" )
    @GenericGenerator(name = "uuid", strategy = "uuid2")
    private String id;

    @ManyToOne
    @JoinColumn(name = "id_jadwal_bahasa")
    private JadwalBahasa jadwalBahasa;

    @ManyToOne
    @JoinColumn(name = "id_detail")
    private DetailJadwalBahasa detailJadwalBahasa;

    @ManyToOne
    @JoinColumn(name = "id_dosen")
    private Dosen dosen;

    @NotNull
    private Integer pertemuanKe;

    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime waktuMengajar;

    @Column(columnDefinition = "LONGTEXT")
    private String linkPertemuan;

    @Column(columnDefinition = "LONGTEXT")
    private String beritaAcara;

    @NotNull
    private String pertemuan;

    @NotNull @Enumerated(EnumType.STRING)
    private JenisTest jenis;

    @NotNull @Enumerated(EnumType.STRING)
    private StatusRecord status = StatusRecord.AKTIF;

}
