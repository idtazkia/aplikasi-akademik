package id.ac.tazkia.smilemahasiswa.dto;

import id.ac.tazkia.smilemahasiswa.entity.StatusRecord;
import org.apache.kafka.common.protocol.types.Field;

import javax.persistence.criteria.CriteriaBuilder;
import java.math.BigDecimal;

public interface KrsNilaiTugasDto {

    String getId();
    String getKrsDetail();
    String getJadwalBobotTugas();
    BigDecimal getNilai();
    String getStatus();
    BigDecimal getNilaiAkhir();

}
