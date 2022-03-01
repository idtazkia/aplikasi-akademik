package id.ac.tazkia.smilemahasiswa.dto.courses;

import id.ac.tazkia.smilemahasiswa.entity.Prodi;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalTime;

public interface DetailJadwalIntDto {

    String getId();
    String getNamaProdi();
    String getNamaKelas();
    String getKodeMatakuliah();
    String getNamaMatakuliah();
    String getNamaMatakuliahEnglish();
    String getIdDosen();
    String getDosen();
    LocalTime getJamMulai();
    LocalTime getJamSelesai();
    String getIdNumberElearning();
    String getIdTahunAkademik();
    String getStatus();

}
