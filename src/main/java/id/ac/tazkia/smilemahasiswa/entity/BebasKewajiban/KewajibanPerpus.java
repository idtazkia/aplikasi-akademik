package id.ac.tazkia.smilemahasiswa.entity.BebasKewajiban;

import id.ac.tazkia.smilemahasiswa.entity.StatusRecord;
import lombok.Data;
import org.hibernate.annotations.GenericGenerator;

import javax.persistence.*;

@Entity
@Data
public class KewajibanPerpus {

    @Id
    @GeneratedValue(generator = "uuid")
    @GenericGenerator(name = "uuid", strategy = "uuid2")
    private String id;

    @Column(columnDefinition = "LONGTEXT")
    private String keterangan;

    @Enumerated(EnumType.STRING)
    private StatusRecord status;

}
