package id.ac.tazkia.smilemahasiswa.dto.user;

import id.ac.tazkia.smilemahasiswa.entity.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;

import javax.persistence.Column;
import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProfileDto {
    private String id;
    private String nim;
    private String nama;
    private Agama agama;
    private String lahir;
    @Column(columnDefinition = "DATE")
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDate tanggalLahir;
    private JenisKelamin kelamin;
    private String nik;
    private String nisn;
    private String wargaNegara;
    private Transportasi transportasi;
    private String telepon;
    private String hp;
    private String email;
    private String emailTazkia;
    private String toga;
    private String negara;
    private String alamat;
    private String nikAyah;
    private String ayah;
    private Pendidikan pendidikanAyah;
    private String lahirAyah;
    @Column(columnDefinition = "DATE")
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDate tanggalLahirAyah;
    private Pekerjaan pekerjaanAyah;
    private Penghasilan penghasilanAyah;
    private Agama agamaAyah;
    private String statusAyah;
    private String kebutuhanAyah;

    private String nikIbu;
    private String ibu;
    private Pendidikan pendidikanIbu;
    private String lahirIbu;
    @Column(columnDefinition = "DATE")
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDate tanggalLahirIbu;
    private Pekerjaan pekerjaanIbu;
    private Penghasilan penghasilanIbu;
    private Agama agamaIbu;
    private String statusIbu;
    private String kebutuhanIbu;
    private String prodi;
    private String angkatan;
}
