package id.ac.tazkia.smilemahasiswa.dto.machine;

import id.ac.tazkia.smilemahasiswa.entity.StatusPresensi;
import lombok.Data;

import java.time.LocalTime;

@Data
public class ApiPresensiMahasiswa {
    private String presensiMahasiswa;
    private String krsDetail;
    private String SesiKuliah;
    private String Mahasiswa;
    private String namaMahasiswa;
    private LocalTime WaktuMasuk;
    private LocalTime WaktuKeluar;
    private StatusPresensi statusPresensi;
    private String catatan;
    private Integer rating;
    private String rfid;
    private Boolean sukses = true;
    private String pesanError;
    private Integer jumlah;
    private String nim;
}
