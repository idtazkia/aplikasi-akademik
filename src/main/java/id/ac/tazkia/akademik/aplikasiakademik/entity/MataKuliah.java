package id.ac.tazkia.akademik.aplikasiakademik.entity;


import lombok.Data;
import org.hibernate.annotations.GenericGenerator;
import org.springframework.format.annotation.DateTimeFormat;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name="tb_matakuliah")
public class MataKuliah {

    @Id
    @GeneratedValue(generator = "uuid" )
    @GenericGenerator(name = "uuid", strategy = "uuid2")
    private String idMatakuliah;

    @NotNull
    private String kodeMatakuliah;

    @NotNull
    private String nama;

    @NotNull
    private String namaEng;

    @NotNull
    private String singkatan;

    @NotNull
    private String responsiLab;

    @ManyToOne
    @JoinColumn(name = "id_jenismk")
    private JenisMk jenisMk;

    @ManyToOne
    @JoinColumn(name = "id_pilihan")
    private Pilihan pilihan;

    @ManyToOne
    @JoinColumn(name = "id_kurikulum")
    private Kurikulum kurikulum;

    @NotNull
    private String wajib;

    @ManyToOne
    @JoinColumn(name = "id_konsentrasi")
    private Konsentrasi konsentrasi;

    private Integer semester;

    private Integer sesi;

    private Integer sks;

    private Integer sksTatapMuka;

    private Integer sksPraktikum;

    private Integer sksLapangan;

    private Integer sksMinimal;

    private Float ipkMinimal;

    private String tugasAkhir;

    private String penanggungJawab;

    private String keterangan;

    @NotNull @Enumerated(EnumType.STRING)
    private StatusRecord status = StatusRecord.AKTIF;

    @Column(columnDefinition = "DATE")
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDateTime tglInsert;

    @Column(columnDefinition = "DATE")
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDateTime tglEdit;

    @ManyToOne
    @JoinColumn(name = "user_insert")
    private User userInsert;

    @ManyToOne
    @JoinColumn(name = "user_edit ")
    private User userEdit;

}
