package id.ac.tazkia.akademik.aplikasiakademik.dto;

import id.ac.tazkia.akademik.aplikasiakademik.entity.*;
import lombok.Data;
import org.hibernate.annotations.GenericGenerator;
import org.springframework.format.annotation.DateTimeFormat;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
public class MahasiswaDto {
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

    @Column(columnDefinition = "DATE")
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDate tglLahir;

    @NotNull
    private String agama;

    @NotNull
    private  String statusSipil;

    @NotNull
    private  String alamat;

    private  KabupatenKota kokabMahasiswa;

    @NotNull
    private  String kodeposmhas;

    private  Provinsi provinsiMahasiswa;

    @NotNull
    private  String negaraMahasiswa;

    @NotNull
    private  String teleponMahasiswa;

    @NotNull
    private  String ponselMahasiswa;

    @NotNull
    private  String emailMahasiswa;

    @NotNull
    private  String pernahKuliah;

    @NotNull
    private  String tempatTinggalMahasiswa;

    @NotNull
    private  String foto;

    @Column(columnDefinition = "DATE")
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDateTime tglInsert;

    @Column(columnDefinition = "DATE")
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDateTime tglEdit;

    private User userInsert;

    private User userEdit;

    private String naMahasiswa = "1";

    @NotNull
    private String statusMahasiswa ="1";

    private User idUser;

    private UserPassword userPassword;

    private MhswAkademik mhswAkademik;

    private Fakultas idFakultas;

    private Jenjang idJenjang;

    private Prodi idProdi;

    private Konsentrasi idKonsentrasi;

    @NotNull
    private String nirm;

    @NotNull
    private String nisn;

    @Column(columnDefinition = "DATE")
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDateTime tglMasuk;

    @NotNull
    private String idStatusKeaktifan;

    @NotNull
    private String tahunAngkatan;

    @Column(columnDefinition = "DATE")
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDateTime tglAktif;

    @NotNull
    private String pendSebelum;

    @NotNull
    private String password;

    private MhswKost mhswKost;

    @NotNull
    private String alamatKost;

    private Provinsi idPropinsi;

    private KabupatenKota idKokab;

    @NotNull
    private String kodeposKost;

    @NotNull
    private String negaraKost;

    private MhswOrtu idMhswOrtu;

    @NotNull
    private String namaAyah;

    @NotNull
    private Agama agamaAyah;

    @NotNull
    private String pendAyah;

    @NotNull
    private String pekerjaanAyah;

    @NotNull
    private String stHidupAyah;

    @NotNull
    private String namaIbu;

    @NotNull
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

    private KabupatenKota kokabOrtu;

    @NotNull
    private String kodepos;

    private Provinsi provinsiOrtu;

    @NotNull
    private String negaraOrtu;

    @NotNull
    private String teleponOrtu;

    @NotNull
    private String ponselOrtu;

    @NotNull
    private String emailOrtu;

    private MhswSekolahAsal mhswSekolahAsal;

    private String kodeposSekolah;

    private Provinsi provinsiSekolah;

    @NotNull
    private String negaraSekolah;

    private Sekolah sekolah;

    private String alamatSekolah;

    private KabupatenKota kokabSekolah;

    @NotNull
    private String jurusanSekolah;

    @NotNull
    private String nilai;

    @NotNull
    private String tahunLulus;

    @NotNull
    private String ijazah;


}
