package id.ac.tazkia.smilemahasiswa.dto.graduation;

import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import javax.persistence.Column;
import java.time.LocalDate;

@Data
public class WisudaDto {
    private String mahasiswa;
    private String nik;
    private String nama;
    @Column(columnDefinition = "DATE")
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDate tanggal;
    private String kelamin;
    private String nomor;
    private String email;
    private String ayah;
    private String ibu;
    private String beasiswa;
    private String idBeasiswa;
    private String toga;
    private String judulIndo;
    private String judulInggris;
    private String sidang;
    private String id;
}
