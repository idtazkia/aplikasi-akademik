package id.ac.tazkia.smilemahasiswa.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import id.ac.tazkia.smilemahasiswa.entity.JenisKelamin;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;

import javax.persistence.Column;
import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class ImportMahasiswaDto implements Serializable {
    @NotNull
    private String nama;
    @NotNull
    private String angkatan;
    @NotNull
    private String prodi;
    @NotNull
    private String nim;
    @NotNull
    private String jenjang;
    @NotNull
    private String program;
    @NotNull
    private JenisKelamin jenisKelamin;
    @NotNull
    private String idAgama;
    @NotNull
    private String tempatLahir;
    @NotNull
    @Column(columnDefinition = "DATE")
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDate tanggalLahir;
    @NotNull
    private String kabupaten;
    @NotNull
    private String provinsi;
    @NotNull
    private String negara;
    @NotNull
    private String kewarganegaraan;
    private String nik;
    private String nisn;
    @NotNull
    private String alamat;
    @NotNull
    private String telepon;
    @NotNull
    private String email;
    private String statusAktif;
    private String user;
    @NotNull
    private String ayah;
    @NotNull
    private String ibu;
    private String id;

}
