package id.ac.tazkia.smilemahasiswa.dto.report;

import java.time.LocalTime;

public interface RekapJadwalDosen2Dto {

    String getIdJadwal();
    String getIdDosen();
    String getNamaDosen();
    String getNamaMatakuliah();
    Integer getSks();
    String getNamaProdi();
    String getNamaKelas();
    String getNamaHari();
    String getSesi();
    LocalTime getJamMulai();
    LocalTime getJamSelesai();
    String getNamaRuangan();
    String getNamaGedung();
    Integer getJumlahKehadiran();

}
