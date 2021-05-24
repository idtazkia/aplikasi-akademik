package id.ac.tazkia.smilemahasiswa.dto.schedule;

import id.ac.tazkia.smilemahasiswa.entity.Akses;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ScheduleDto {
    private String id;
    private String idMatakuliah;
    private String namaMatkuliah;
    private String namaKelas;
    private String namaDosen;
    private int jumlahSks;
    @DateTimeFormat(pattern = "HH:mm:ss")
    private LocalTime jamMulai;

    @DateTimeFormat(pattern = "HH:mm:ss")
    private LocalTime jamSelesai;
    private Akses akses;
    private String ruangan;
    private String hari;
    private String course;



}