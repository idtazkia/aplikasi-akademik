package id.ac.tazkia.smilemahasiswa.dto.edom;

import java.math.BigDecimal;

public interface RekapEdomSarprasDto {
    String getIdCategory();

    String getCategoryName();

    String getIdQuestion();

    String getQuestionText();

    Long getTotalResponden();

    BigDecimal getRataRata();
}
