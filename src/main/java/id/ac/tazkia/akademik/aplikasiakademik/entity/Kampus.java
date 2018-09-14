package id.ac.tazkia.akademik.aplikasiakademik.entity;

import lombok.Data;
import org.hibernate.annotations.GenericGenerator;
import org.springframework.format.annotation.DateTimeFormat;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "tb_kampus")
public class Kampus {

    @Id
    @GeneratedValue(generator = "uuid" )
    @GenericGenerator(name = "uuid", strategy = "uuid2")
    private String idKampus;

    @Column(nullable = false)
    private String namaKampus;

    @Column(nullable = false)
    private String alamat;


    @ManyToOne
    @NotNull
    @JoinColumn(name = "propinsi")
    private Provinsi propinsi;

    @ManyToOne
    @NotNull
    @JoinColumn(name = "kokab")
    private KabupatenKota kabupatenKota;

    @Column(nullable = false)
    private String keterangan;

    @NotNull @Enumerated(EnumType.STRING)
    private StatusRecord status = StatusRecord.AKTIF;

    @NotNull @Column(columnDefinition = "DATE")
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
