package id.ac.tazkia.smilemahasiswa.dto.human;

import java.time.LocalDate;

public interface KaryawanIntDto {

    String getId();
    String getNik();
    String getNamaKaryawan();
    String getGelar();
    String getJenisKelamin();
    String getIdUser();
    String getStatus();
    String getNidn();
    String getEmail();
    LocalDate getTanggalLahir();
    String getRfid();
    Integer getIdAbsen();
    String getFoto();
}
