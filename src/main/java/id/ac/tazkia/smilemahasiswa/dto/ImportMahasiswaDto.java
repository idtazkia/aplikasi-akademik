package id.ac.tazkia.smilemahasiswa.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import id.ac.tazkia.smilemahasiswa.entity.JenisKelamin;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;

import javax.persistence.Column;
import java.io.Serializable;
import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class ImportMahasiswaDto implements Serializable {
    private String nama;
    private String angkatan;
    private String prodi;
    private String nim;
    private String jenjang;
    private String program;
    private JenisKelamin jenisKelamin;
    private String idAgama;
    private String tempatLahir;
    @Column(columnDefinition = "DATE")
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDate tanggalLahir;
    private String kabupaten;
    private String provinsi;
    private String negara;
    private String kewarganegaraan;
    private String nik;
    private String nisn;
    private String alamat;
    private String telepon;
    private String email;
    private String statusAktif;
    private String user;
    private String ayah;
    private String ibu;

}
