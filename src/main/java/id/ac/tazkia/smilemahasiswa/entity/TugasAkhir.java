    package id.ac.tazkia.smilemahasiswa.entity;

import id.ac.tazkia.smilemahasiswa.constant.StatusTugasAkhir;
import lombok.Data;
import org.hibernate.annotations.GenericGenerator;
import org.springframework.format.annotation.DateTimeFormat;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.time.LocalDate;

@Entity
@Data
public class TugasAkhir {
    @Id
    @GeneratedValue(generator = "uuid" )
    @GenericGenerator(name = "uuid", strategy = "uuid2")
    private String id;

    @ManyToOne
    @JoinColumn(name = "id_mahasiswa")
    private Mahasiswa mahasiswa;

    public enum MenuTugasAkhir {
        NOTE,
        SEMINAR,
        SIDANG,
        WISUDA
    }

    @Enumerated(EnumType.STRING)
    private MenuTugasAkhir menu;

    @Enumerated(EnumType.STRING)
    private StatusTugasAkhir statusTugasAkhir;

    @Enumerated(EnumType.STRING)
    private StatusRecord status = StatusRecord.AKTIF;


}
