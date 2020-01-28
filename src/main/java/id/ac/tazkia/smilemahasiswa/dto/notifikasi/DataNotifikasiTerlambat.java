package id.ac.tazkia.smilemahasiswa.dto.notifikasi;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalTime;

@Data
@Builder
public class DataNotifikasiTerlambat {
    private String nama;
    private String matakuliah;
    private String hariMasuk;
    private LocalDate tanggalMasuk;
    private LocalTime jamMasuk;
    private String hari;
    private LocalTime jam;
}
