package id.ac.tazkia.smilemahasiswa.dto.transkript;

import java.math.BigDecimal;

public interface DataTranskript {
    String getSemester();
    String getMatkur();
    String getKode();
    String getMatkul();
    String getCourse();
    Integer getSks();
    BigDecimal getNilaiAkhir();
    BigDecimal getBobot();
    String getGrade();
    BigDecimal getMutu();

}
