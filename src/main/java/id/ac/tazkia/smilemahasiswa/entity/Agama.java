package id.ac.tazkia.smilemahasiswa.entity;

import lombok.Data;
import org.hibernate.annotations.GenericGenerator;

import javax.persistence.*;
import javax.validation.constraints.NotNull;

@Entity
@Data
public class Agama {

    @Id
    @GeneratedValue(generator = "uuid")
    @GenericGenerator(name = "uuid", strategy = "uuid2")
    private String idAgama;

    @NotNull
    private String agama;

    @Enumerated(value = EnumType.STRING)
    @NotNull
    private StatusRecord status = StatusRecord.AKTIF ;

}
