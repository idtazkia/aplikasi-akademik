package id.ac.tazkia.smilemahasiswa.entity.BebasKewajiban;

import id.ac.tazkia.smilemahasiswa.entity.StatusRecord;
import lombok.Data;
import org.hibernate.annotations.GenericGenerator;

import javax.persistence.*;

@Data
@Entity
public class KewajibanPerpusMahasiswa {

    @Id
    @GeneratedValue(generator = "uuid")
    @GenericGenerator(name = "uuid", strategy = "uuid2")
    private String id;

    @ManyToOne
    @JoinColumn(name = "id_pendaftaran")
    private PendaftaranBebasKewajiban bebasKewajiban;

    @ManyToOne
    @JoinColumn(name = "id_kewajiban_perpus")
    private KewajibanPerpus kewajibanPerpus;

    @Enumerated(EnumType.STRING)
    private StatusRecord status = StatusRecord.NONAKTIF;

}
