package id.ac.tazkia.smilemahasiswa.dto.payment;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Date;

public interface PembayaranDto {

    String getIdPembayaran();
    String getIdTagihan();
    String getNim();
    String getNama();
    String getJenisTagihan();
    String getBank();
    String getJumlah();
    LocalDateTime getTanggalTransaksi();
    String getReferensi();

}
