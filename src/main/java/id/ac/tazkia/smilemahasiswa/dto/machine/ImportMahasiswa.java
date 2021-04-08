package id.ac.tazkia.smilemahasiswa.dto.machine;

import id.ac.tazkia.smilemahasiswa.entity.Ayah;
import id.ac.tazkia.smilemahasiswa.entity.Ibu;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import javax.persistence.Column;
import java.time.LocalDate;

@Data
public class ImportMahasiswa  {
    private String id;
    private String nim;
    private String nama;
    private String angkatan;
    private String idProdi;
    private String jenisKelamin;
    private String agama;
    private String tempatLahir;
    @Column(columnDefinition = "DATE")
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDate tanggalLahir;
    private String kokab;
    private String provinsi;
    private String negara;
    private String kewarganegaraan;
    private String nik;
    private String nisn;
    private String alamatRumah;
    private String kodePos;
    private String noOrangtua;
    private String noMahasiswa;
    private String email;
    private String namaAyah;
    private String namaIbu;
    private String program;
    private String jenjang;
    private String ayah;
    private String ibu;
}
