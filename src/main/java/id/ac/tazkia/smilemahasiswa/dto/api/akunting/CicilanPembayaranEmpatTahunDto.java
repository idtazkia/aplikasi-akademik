package id.ac.tazkia.smilemahasiswa.dto.api.akunting;

import id.ac.tazkia.smilemahasiswa.entity.AkuntansiExport;
import id.ac.tazkia.smilemahasiswa.entity.Pembayaran;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public interface CicilanPembayaranEmpatTahunDto {

    String getId();
    String getKodeJurnal();
    AkuntansiExport getZahirExport();
    String getNim();
    String getNama();
    LocalDateTime getWaktuBayar();
    Pembayaran.TipePengiriman getTipePengiriman();
    String getKeterangan();
    BigDecimal getNilaiCicilan();

}
