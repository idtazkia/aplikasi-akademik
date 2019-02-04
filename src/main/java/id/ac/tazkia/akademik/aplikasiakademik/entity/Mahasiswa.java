package id.ac.tazkia.akademik.aplikasiakademik.entity;

import com.fasterxml.jackson.annotation.JsonBackReference;
import lombok.Data;
import org.hibernate.annotations.GenericGenerator;
import org.springframework.format.annotation.DateTimeFormat;

import javax.persistence.*;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Entity
@Data
public class Mahasiswa {

    @Id
    @GeneratedValue(generator = "uuid" )
    @GenericGenerator(name = "uuid", strategy = "uuid2")
    private String id;

    @NotNull
    private String angkatan;

    @NotBlank
    private String nim;

    @NotBlank
    private String nama;

    @Enumerated(EnumType.STRING)
    private  JenisKelamin jenisKelamin;

    @ManyToOne
    @NotNull
    @JoinColumn(name = "id_agama")
    private Agama idAgama;

    @ManyToOne
    @NotNull
    @JoinColumn(name = "id_prodi")
    private Prodi idProdi;

    @ManyToOne
    @NotNull
    @JoinColumn(name = "id_konsentrasi")
    private  Konsentrasi idKonsentrasi;

    private  String tempatLahir;

    @NotNull
    private String statusMatrikulasi;

    @ManyToOne
    @NotNull
    @JoinColumn(name = "id_program")
    private  Program idProgram;

    @Column(columnDefinition = "DATE")
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDate tanggalLahir;

    private String idKelurahan;

    private  String idKecamatan;

    private  String idKotaKabupaten;

    private  String idProvinsi;

    private  String idNegara;

    private  String kewarganegaraan;

    private  String nik;

    private  String nisn;

    private String namaJalan;
    private String rt;
    private String rw;
    private String namaDusun;


    private String kodepos;
    @ManyToOne
    @JoinColumn(name = "jenis_tinggal")
    private JenisTinggal jenisTinggal;

    @ManyToOne
    @JoinColumn(name = "alat_transportasi")
    private Transportasi alatTransportasi;

    private String teleponRumah;
    private String teleponSeluler;
    private String emailPribadi;
    private String emailTazkia;
    private String statusAktif;

    @NotNull @Enumerated(EnumType.STRING)
    private StatusRecord status = StatusRecord.AKTIF;

    @ManyToOne
    @JoinColumn(name = "id_user")
    private  User user;

    @ManyToOne
    @JsonBackReference
    @JoinColumn(name = "id_dosen_wali")
    private  Dosen dosen ;

    private String ukuranBaju;
    private String kps = "Tidak";
    private String nomorKps;

}
