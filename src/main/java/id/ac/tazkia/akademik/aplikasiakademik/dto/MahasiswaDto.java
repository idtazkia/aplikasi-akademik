package id.ac.tazkia.akademik.aplikasiakademik.dto;

import id.ac.tazkia.akademik.aplikasiakademik.entity.*;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import javax.persistence.Column;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.validation.constraints.NotNull;
import java.time.LocalDate;

@Data
public class MahasiswaDto {

    private String id;
    private String angkatan;
    private Prodi idProdi;
    private Konsentrasi idKonsentrasi;
    private String nim;
    private String nama;
    private String statusMatrikulasi;
    private Program idProgram;
    private String jenisKelamin;
    private Agama religion;
    private String tempat;
    @Column(columnDefinition = "DATE")
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDate tanggalLahir;
    private String idKelurahan;
    private String idKecamatan;
    private String idKotaKabupaten;
    private String idProvinsi;
    private String idNegara;
    private String kewarganegaraan;
    private String nik;
    private String nisn;
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
    private String emailTazkia;
    private String statusAktif;
    @NotNull
    @Enumerated(EnumType.STRING)
    private StatusRecord status = StatusRecord.AKTIF;
    private User idUser;
    private UserPassword userPassword;
    private String password;

    private String ayah;
    private String namaAyah;
    private String kebutuhanKhusus;
    private String tempatLahirAyah;
    @Column(columnDefinition = "DATE")
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDate tanggalLahirAyah;
    private String idJenjangPendidikan;
    private String idPekerjaan;
    private String penghasilan;
    private  Agama agama;
    private String hidup;

    private String ibu;
    private String namaIbuKandung;
    private String kebutuhanKhususIbu;
    private String tempatLahirIbu;
    @Column(columnDefinition = "DATE")
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDate tanggalLahirIbu;
    private String idJenjangPendidikanIbu;
    private String idPekerjaanIbu;
    private String penghasilanIbu;
    private  Agama agamaIbu;
    private String statusHidupIbu;

    private String wali;
    private String namaWali;
    private String kebutuhanKhususWali;
    private String tempatLahirWali;
    @Column(columnDefinition = "DATE")
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDate tanggalLahirWali;
    private String idJenjangPendidikanWali;
    private String idPekerjaanWali;
    private String idPenghasilanWali;
    private  Agama agamaWali;
}
