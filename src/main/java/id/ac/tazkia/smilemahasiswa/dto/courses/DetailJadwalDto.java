package id.ac.tazkia.smilemahasiswa.dto.courses;

import id.ac.tazkia.smilemahasiswa.entity.*;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;


import java.time.LocalTime;

@Data
public class DetailJadwalDto {

    private String id;
    private String namaProdi;
    private String namaKelas;
    private String kodeMatakuliah;
    private String namaMatakuliah;
    private String namaMatakuliahEnglish;
    private String idDosen;
    private String dosen;
    @DateTimeFormat(pattern = "HH:mm:ss")
    private LocalTime jamMulai;
    @DateTimeFormat(pattern = "HH:mm:ss")
    private LocalTime jamSelesai;
    private String idNumberElearning;
    private String idTahunAkademik;
    private String status;

}
