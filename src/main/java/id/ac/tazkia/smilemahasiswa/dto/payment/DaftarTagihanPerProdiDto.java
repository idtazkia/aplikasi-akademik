package id.ac.tazkia.smilemahasiswa.dto.payment;

import java.math.BigDecimal;
import java.util.Date;

public interface DaftarTagihanPerProdiDto {

    String getId();
    String getProdi();
    BigDecimal getTagihan();
    BigDecimal getDibayar();
    BigDecimal getSisa();
    BigDecimal getPercentage();

}
