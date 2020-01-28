package id.ac.tazkia.smilemahasiswa.entity;

import lombok.Data;
import org.hibernate.annotations.GenericGenerator;

import javax.persistence.*;
import javax.validation.constraints.Min;
import java.math.BigDecimal;

@Data
@Entity
public class Diskon {
    @Id
    @GeneratedValue(generator = "uuid")
    @GenericGenerator(name = "uuid", strategy = "uuid2")
    private String id;

    @ManyToOne
    @JoinColumn(name = "id_tagihan")
    private TagihanMahasiswa tagihanMahasiswa;

    @ManyToOne
    @JoinColumn(name = "id_jenis_diskon")
    private JenisDiskon jenisDiskon;

    @Min(0)
    private BigDecimal amount;
}
