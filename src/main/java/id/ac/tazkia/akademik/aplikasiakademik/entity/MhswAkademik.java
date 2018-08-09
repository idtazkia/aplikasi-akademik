package id.ac.tazkia.akademik.aplikasiakademik.entity;


import lombok.Data;
import org.hibernate.annotations.GenericGenerator;
import org.springframework.format.annotation.DateTimeFormat;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;

@Table(name = "tb_mhsw_akademik")
@Entity
@Data
public class MhswAkademik {

    @Id
    @GeneratedValue(generator = "uuid" )
    @GenericGenerator(name = "uuid", strategy = "uuid2")
    private String idMhswAkademik;

    @ManyToOne
    @NotNull
    @JoinColumn(name = "id_mhsw")
    private Mahasiswa idMhsw;

    @ManyToOne
    @NotNull
    @JoinColumn(name = "id_fakultas")
    private Fakultas idFakultas;

    @ManyToOne
    @NotNull
    @JoinColumn(name = "id_jenjang")
    private Jenjang idJenjang;

    @ManyToOne
    @NotNull
    @JoinColumn(name = "id_prodi")
    private Prodi idProdi;

    @ManyToOne
    @NotNull
    @JoinColumn(name = "id_konsentrasi")
    private Konsentrasi idKonsentrasi;

    @NotNull
    private String nim;

    @NotNull
    private String nirm;

    @NotNull
    private String nisn;

    @NotNull
    private String tahunAngkatan;

    @Column(columnDefinition = "DATE")
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDateTime tglMasuk;

    @NotNull
    private String idStatusKeaktifan;

    @Column(columnDefinition = "DATE")
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDateTime tglAktif;

    @NotNull
    private String pendSebelum;

    @NotNull
    private String status = "1";

    @ManyToOne
    @JoinColumn(name = "user_insert")
    private User userInsert;

    @ManyToOne
    @JoinColumn(name = "user_edit ")
    private User userEdit;

    @Column(columnDefinition = "DATE")
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDateTime tglInsert;

    @Column(columnDefinition = "DATE")
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDateTime tglEdit;

}
