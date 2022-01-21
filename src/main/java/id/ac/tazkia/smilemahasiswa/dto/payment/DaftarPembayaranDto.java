package id.ac.tazkia.smilemahasiswa.dto.payment;

import javax.xml.crypto.Data;
import java.math.BigDecimal;
import java.util.Date;

public interface DaftarPembayaranDto {

    String getIdPembayaran();
    String getIdTagihan();
    String getIdMahasiswa();
    String getIdTahunAkademik();
    Date getTanggal();
    String getTagihan();
    String getNomorBukti();
    BigDecimal getJumlah();
    String getKeterangan();

}
