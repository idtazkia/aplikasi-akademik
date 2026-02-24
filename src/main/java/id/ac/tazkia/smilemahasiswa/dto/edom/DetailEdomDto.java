package id.ac.tazkia.smilemahasiswa.dto.edom;

import java.math.BigDecimal;

public interface DetailEdomDto {
    String getIdJadwal();

    String getIdDosen();

    String getIdQuestion();

    String getQuestionText();

    BigDecimal getNilai();
}
