package id.ac.tazkia.akademik.aplikasiakademik.entity;

import lombok.Data;
import org.hibernate.annotations.GenericGenerator;
import org.springframework.format.annotation.DateTimeFormat;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;

@Table(name = "tb_mhsw_ortu")
@Entity
@Data
public class MhswOrtu {

    @Id
    @GeneratedValue(generator = "uuid" )
    @GenericGenerator(name = "uuid", strategy = "uuid2")
    private String idMhswOrtu;

    @ManyToOne
    @NotNull
    @JoinColumn(name = "id_mhsw")
    private Mahasiswa idMhsw;

    @NotNull
    private String namaAyah;

    @ManyToOne
    @JoinColumn(name = "agama_ayah")
    private Agama agamaAyah;

    @NotNull
    private String pendAyah;

    @NotNull
    private String pekerjaanAyah;

    @NotNull
    private String stHidupAyah;

    @NotNull
    private String namaIbu;

    @ManyToOne
    @JoinColumn(name = "agama_ibu")
    private Agama agamaIbu;

    @NotNull
    private String pendIbu;

    @NotNull
    private String pekerjaanIbu;

    @NotNull
    private String stHidupIbu;

    @NotNull
    private String hasilOrtuPerBulan;

    @NotNull
    private String alamatOrtu;

    @ManyToOne
    @JoinColumn(name = "kokab")
    private KabupatenKota kokab;

    @NotNull
    private String kodepos;

    @ManyToOne
    @JoinColumn(name = "provinsi")
    private Provinsi provinsi;

    @NotNull
    private String negara;

    @NotNull
    private String telepon;

    @NotNull
    private String ponsel;

    @NotNull
    private String email;

    @NotNull
    private String status="1";

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
