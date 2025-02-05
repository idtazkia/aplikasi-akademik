package id.ac.tazkia.smilemahasiswa.dto.api.akunting;

import id.ac.tazkia.smilemahasiswa.entity.AkuntansiExport;

import java.math.BigDecimal;
import java.time.LocalDate;

public interface CicilanTagihanEmpatTahunDto {

    String getIdTagihan();
    String getIdCicilan();
    String getKodeJurnal();
    AkuntansiExport getZahirExport();
    String getNim();
    String getNama();
    LocalDate getTanggalJatuhTempo();
    String getKeterangan();
    BigDecimal getNilaiCicilan();

}
