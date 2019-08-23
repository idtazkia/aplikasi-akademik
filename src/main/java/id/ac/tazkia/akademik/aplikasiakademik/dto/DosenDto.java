package id.ac.tazkia.akademik.aplikasiakademik.dto;

import id.ac.tazkia.akademik.aplikasiakademik.entity.User;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import javax.persistence.Column;
import java.time.LocalDate;

@Data
public class DosenDto {
    private String id;
    private String nik;
    private String nidn;
    private String nama;
    private String gelar;
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    @Column(columnDefinition = "DATE")
    private LocalDate tanggalLahir;
    private String jenisKelamin;
    private String email;
    private String prodi;
    private User idUser;
}
