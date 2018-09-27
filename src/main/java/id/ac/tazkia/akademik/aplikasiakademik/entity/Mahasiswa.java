package id.ac.tazkia.akademik.aplikasiakademik.entity;

import lombok.Data;
import org.hibernate.annotations.GenericGenerator;
import org.springframework.format.annotation.DateTimeFormat;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.time.LocalDate;

@Entity
@Data
public class Mahasiswa {

    @Id
    @GeneratedValue(generator = "uuid" )
    @GenericGenerator(name = "uuid", strategy = "uuid2")
    private String id;

    @NotNull
    private String angkatan;

    @ManyToOne
    @NotNull
    @JoinColumn(name = "id_prodi")
    private  Prodi idProdi;

    @ManyToOne
    @NotNull
        @JoinColumn(name = "id_konsentrasi")
    private  Konsentrasi idKonsentrasi;

    @NotNull
    private String nim;

    @NotNull
    private String nama;

    @NotNull
    private String statusMatrikulasi;

    @ManyToOne
    @NotNull
    @JoinColumn(name = "id_program")
    private  Program idProgram;

    private  String jenisKelamin;

    private String idAgama;

    private  String tempatLahir;

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
    private String jenisTinggal;
    private String alatTransportasi;
    private String teleponRumah;
    private String teleponSeluler;
    private String emailPribadi;
    private String statusAktif;

    @NotNull @Enumerated(EnumType.STRING)
    private StatusRecord status = StatusRecord.AKTIF;

    @ManyToOne
    @NotNull
    @JoinColumn(name = "id_user")
    private  User idUser;
}
