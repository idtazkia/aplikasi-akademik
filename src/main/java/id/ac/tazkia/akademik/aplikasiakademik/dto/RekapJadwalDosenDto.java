package id.ac.tazkia.akademik.aplikasiakademik.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalTime;

@Data @NoArgsConstructor @AllArgsConstructor
public class RekapJadwalDosenDto {
    private String idDosen;
    private String namaDosen;
    private String namaMatakuliah;
    private Integer sks;
    private String namaProdi;
    private String namaKelas;
    private String namaHari;
    private LocalTime jamMulai;
    private LocalTime jamSelesai;
    private String namaRuangan;
    private String namaGedung;
}
