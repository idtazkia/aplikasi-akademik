package id.ac.tazkia.smilemahasiswa.entity;

import lombok.Data;
import org.hibernate.annotations.GenericGenerator;

import javax.persistence.*;
import javax.validation.constraints.Min;
import java.math.BigDecimal;

@Entity
@Data
public class BiayaSksSp {

    @Id
    @GeneratedValue(generator = "uuid")
    @GenericGenerator(name = "uuid", strategy = "uuid2")
    private String id;

    @Min(1000)
    private BigDecimal biaya;

    @Enumerated(EnumType.STRING)
    private StatusRecord status = StatusRecord.AKTIF;

}
