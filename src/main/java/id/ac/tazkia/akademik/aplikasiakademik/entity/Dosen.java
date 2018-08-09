package id.ac.tazkia.akademik.aplikasiakademik.entity;

import lombok.Data;
import org.hibernate.annotations.GenericGenerator;
import org.springframework.format.annotation.DateTimeFormat;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Data
@Table(name = "tb_dosen")
public class Dosen {
    @Id
    @GeneratedValue(generator = "uuid" )
    @GenericGenerator(name = "uuid", strategy = "uuid2")
    private String idDosen;

    @NotNull
    private String nip;

    private String nidn;

    @NotNull
    private String nama;

    @NotNull
    private String tempatLahir;

    @NotNull @Column(columnDefinition = "DATE")
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDate tglLahir;

    @NotNull
    private String jk;

    @NotNull
    private String asalNegara;

    @ManyToOne
    @JoinColumn(name = "agama")
    private Agama agama;

    @NotNull
    private String statusSipil;

    @NotNull
    private String alamat;

    @ManyToOne
    @NotNull
    @JoinColumn(name = "id_propinsi")
    private Provinsi provinsi;

    @ManyToOne
    @JoinColumn(name = "id_kokab")
    private KabupatenKota kokab;

    @NotNull
    private String kodepos;

    @NotNull
    private String negara;

    @ManyToOne
    @JoinColumn(name = "tempatTinggal")
    private TempatTinggal tempatTinggal;

    @NotNull
    private String telepon;

    @NotNull
    private String ponsel;

    @NotNull
    private String email;

    @ManyToOne
    @JoinColumn(name = "prodi_homebase")
    private Prodi prodiHomebase;


    @NotNull
    private String aktaMengajar;

    @NotNull
    private String na = "1";

    private String status = "1";

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
