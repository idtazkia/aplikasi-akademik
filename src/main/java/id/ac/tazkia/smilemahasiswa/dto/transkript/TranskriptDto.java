package id.ac.tazkia.smilemahasiswa.dto.transkript;

import java.math.BigDecimal;

public interface TranskriptDto {
    String getTahun();
    String getKode();
    String getMatakuliah();
    String getCourses();
    Integer getSks();
    BigDecimal getBobot();
    String getGrade();
    BigDecimal getMutu();
}
