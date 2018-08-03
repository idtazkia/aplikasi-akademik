package id.ac.tazkia.akademik.aplikasiakademik.entity;

import lombok.Data;
import org.hibernate.annotations.GenericGenerator;
import org.springframework.format.annotation.DateTimeFormat;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Table(name = "tb_mhsw")
@Entity
@Data
public class Mahasiswa {

    @Id
    @GeneratedValue(generator = "uuid" )
    @GenericGenerator(name = "uuid", strategy = "uuid2")
    private String idMhsw;

    @NotNull
    private String nim;

    @NotNull
    private String nama;

    @NotNull
    private String jk;

    @NotNull
    private String asalNegara;

    @NotNull
    private String tempatLahir;

    @NotNull
    private LocalDate tglLahir;

    @NotNull
    private String agama;

    @NotNull
    private  String statusSipil;

    @NotNull
    private  String alamat;

    @NotNull
    private  String kokab;

    @NotNull
    private  String kodepos;

    @NotNull
    private  String provinsi;

    @NotNull
    private  String negara;

    @NotNull
    private  String telepon;

    @NotNull
    private  String ponsel;

    @NotNull
    private  String email;

    @NotNull
    private  String pernahKuliah;

    @NotNull
    private  String tempatTinggal;

    @NotNull
    private  String foto;

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

    @NotNull
    private String na = "1";

    @NotNull
    private String status ="1";

    @ManyToOne
    @NotNull
    @JoinColumn(name = "id_user")
    private User idUser;
}
