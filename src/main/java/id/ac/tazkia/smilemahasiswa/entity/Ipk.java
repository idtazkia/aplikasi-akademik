package id.ac.tazkia.smilemahasiswa.entity;

import lombok.Data;
import org.hibernate.annotations.GenericGenerator;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.math.BigDecimal;

@Data
@Entity
public class Ipk {

    @Id
    @GeneratedValue(generator = "uuid" )
    @GenericGenerator(name = "uuid", strategy = "uuid2")
    private String id;

    @ManyToOne
    @NotNull
    @JoinColumn(name = "id_mahasiswa")
    private Mahasiswa mahasiswa;

    private Integer sksTotal;
    private BigDecimal bobotTotal;
    private Integer nilaiTotalSks;
    private BigDecimal ipk;
}
