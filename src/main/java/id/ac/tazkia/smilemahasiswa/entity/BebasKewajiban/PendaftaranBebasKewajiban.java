package id.ac.tazkia.smilemahasiswa.entity.BebasKewajiban;

import id.ac.tazkia.smilemahasiswa.entity.Mahasiswa;
import id.ac.tazkia.smilemahasiswa.entity.StatusApprove;
import id.ac.tazkia.smilemahasiswa.entity.StatusRecord;
import lombok.Data;
import org.hibernate.annotations.GenericGenerator;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Data
public class PendaftaranBebasKewajiban {

    @Id
    @GeneratedValue(generator = "uuid")
    @GenericGenerator(name = "uuid", strategy = "uuid2")
    private String id;

    @ManyToOne
    @JoinColumn(name = "id_mahasiswa")
    private Mahasiswa mahasiswa;

    @Enumerated(EnumType.STRING)
    private StatusApprove statusApprove = StatusApprove.WAITING;

    @Enumerated(EnumType.STRING)
    private StatusRecord status = StatusRecord.AKTIF;

    @Enumerated(EnumType.STRING)
    private StatusApprove statusApprovePerpus = StatusApprove.WAITING;

    @Enumerated(EnumType.STRING)
    private StatusApprove statusApproveKabagPerpus = StatusApprove.WAITING;

    @Enumerated(EnumType.STRING)
    private StatusApprove statusApproveKeuangan = StatusApprove.WAITING;

    @Enumerated(EnumType.STRING)
    private StatusApprove statusApproveTlc = StatusApprove.WAITING;

    @Enumerated(EnumType.STRING)
    private StatusApprove statusApproveAkademik = StatusApprove.WAITING;

    private LocalDateTime tanggalRequest = LocalDateTime.now();

}
