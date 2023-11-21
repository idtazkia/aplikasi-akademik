package id.ac.tazkia.smilemahasiswa.entity.BebasKewajiban;

import id.ac.tazkia.smilemahasiswa.entity.StatusApprove;
import id.ac.tazkia.smilemahasiswa.entity.StatusRecord;
import lombok.Data;
import org.hibernate.annotations.GenericGenerator;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Data
public class HistoriBebasKewajiban {

    @Id
    @GeneratedValue(generator = "uuid")
    @GenericGenerator(name = "uuid", strategy = "uuid2")
    private String id;

    @ManyToOne
    @JoinColumn(name = "id_pendaftaran")
    private PendaftaranBebasKewajiban bebasKewajiban;

    @Column(columnDefinition = "LONGTEXT")
    private String komentar;

    private LocalDateTime waktuInput = LocalDateTime.now();

    @Enumerated(EnumType.STRING)
    private ByBagian byBagian;

    @Enumerated(EnumType.STRING)
    private StatusApprove statusApprove;

    @Enumerated(EnumType.STRING)
    private StatusRecord status = StatusRecord.AKTIF;

}
