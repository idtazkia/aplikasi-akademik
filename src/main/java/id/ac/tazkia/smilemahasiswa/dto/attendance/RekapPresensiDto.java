package id.ac.tazkia.smilemahasiswa.dto.attendance;

public interface RekapPresensiDto {
    String getDosen();
    String getKelas();
    String getMatkul();
    String getMulai();
    String getRealisasi();
    String getRangeWaktu();
    String getPertemuan();
    String getPresensi();
    Integer getJumlahMahasiswa();
    Integer getHadir();
    Integer getMangkir();
    Integer getIzin();
    Integer getSakit();
}
