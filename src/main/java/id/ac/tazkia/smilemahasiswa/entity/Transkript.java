package id.ac.tazkia.smilemahasiswa.entity;

import lombok.Data;
import org.hibernate.annotations.GenericGenerator;

import javax.persistence.*;
import java.math.BigDecimal;

@Entity
@Data
public class Transkript {

    @Id
    @GeneratedValue(generator = "uuid")
    @GenericGenerator(name = "uuid", strategy = "uuid2")
    private String id;

    private String semester;

    @JoinColumn(name = "id_mahasiswa")
    @ManyToOne
    private Mahasiswa mahasiswa;

    @JoinColumn(name = "id_matakuliah")
    @ManyToOne
    private Matakuliah matakuliah;

    private int sks;

    private BigDecimal bobot;

    private BigDecimal nilai = BigDecimal.ZERO;

    private String grade;

}
