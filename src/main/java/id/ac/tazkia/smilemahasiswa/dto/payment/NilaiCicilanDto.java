package id.ac.tazkia.smilemahasiswa.dto.payment;

import java.math.BigDecimal;
import java.util.Date;

public interface NilaiCicilanDto {

    String getIdTagihan();
    String getIdCicilan();
    BigDecimal getNilai();
    BigDecimal getNilaiCicilan();
    Date getJatuhTempo();

}
