package id.ac.tazkia.smilemahasiswa.entity;

import lombok.Data;
import org.hibernate.annotations.GenericGenerator;

import javax.persistence.*;
import javax.validation.constraints.Min;

@Entity
@Data
public class KodeSidang {

    @Id
    @GeneratedValue(generator = "uuid")
    @GenericGenerator(name = "uuid", strategy = "uuid2")
    private String id;

    @Min(1)
    private Integer kode;

    private String kodeString;

    private String tahun;

    @ManyToOne
    @JoinColumn(name = "id_prodi")
    private Prodi prodi;

    @Enumerated(EnumType.STRING)
    private StatusRecord status;

}
