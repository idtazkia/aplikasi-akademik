package id.ac.tazkia.smilemahasiswa.dto.payment;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class PembayaranTagihan {

    private String jenisTagihan;
    private String nomorTagihan;
    private String keteranganTagihan;
    private String tanggalPembuatan;
    private String tanggalJatuhTempo;
    private String tanggalPenangguhan;
    private String statusTagihan;
    private BigDecimal nilaiTagihan;
    private BigDecimal nilaiPembayaran;
    private BigDecimal nilaiAkumulasiPembayaran;
    private String bank;
    private String waktuPembayaran;
    private String referensiPembayaran;

}
