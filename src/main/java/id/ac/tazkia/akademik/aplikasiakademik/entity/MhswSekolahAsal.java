package id.ac.tazkia.akademik.aplikasiakademik.entity;

import lombok.Data;
import org.hibernate.annotations.GenericGenerator;
import org.springframework.format.annotation.DateTimeFormat;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;

@Table(name = "tb_mhsw_sekolah_asal")
@Entity
@Data
public class MhswSekolahAsal {

    @Id
    @GeneratedValue(generator = "uuid" )
    @GenericGenerator(name = "uuid", strategy = "uuid2")
    private String idMhswSekolahAsal;

    @ManyToOne
    @NotNull
    @JoinColumn(name = "id_mhsw")
    private Mahasiswa idMhsw;

    @NotNull
    private String kodepos;

    @ManyToOne
    @NotNull
    @JoinColumn(name = "id_provinsi")
    private Provinsi idProvinsi;

    @NotNull
    private String negara;

    @NotNull
    private String namaSekolah;

    @NotNull
    private String idSekolah;

    @NotNull
    private String alamat;

    @ManyToOne
    @NotNull
    @JoinColumn(name = "id_kokab")
    private KabupatenKota idKokab;

    @NotNull
    private String jurusanSekolah;

    @NotNull
    private String nilai;

    @NotNull
    private String tahunLulus;

    @NotNull
    private String ijazah;

    @NotNull
    private String status;

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
