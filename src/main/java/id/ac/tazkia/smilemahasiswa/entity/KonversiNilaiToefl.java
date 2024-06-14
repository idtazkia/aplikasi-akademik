package id.ac.tazkia.smilemahasiswa.entity;

import javax.persistence.Entity;
import javax.persistence.Id;

import lombok.Data;

@Data
@Entity
public class KonversiNilaiToefl {
    @Id
    private Integer jawaban;

    private Integer listening;

    private Integer structure;

    private Integer reading;

}
