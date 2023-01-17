package id.ac.tazkia.smilemahasiswa.dto.ploting;

import org.springframework.format.annotation.DateTimeFormat;

public interface ValidasiSesi {
    String getId();
    String getIdSesi();
    String getNamaSesi();
    @DateTimeFormat(pattern = "HH:mm:ss")
    String getMulai();
    @DateTimeFormat(pattern = "HH:mm:ss")
    String getSelesai();
    String getBentrok();
    String getDosen();
    String getMatkul();
    String getSesi();
}
