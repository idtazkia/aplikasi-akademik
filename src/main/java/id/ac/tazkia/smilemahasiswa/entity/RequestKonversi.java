package id.ac.tazkia.smilemahasiswa.entity;

import id.ac.tazkia.smilemahasiswa.constant.Approval;
import lombok.Data;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.Where;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Data
@SQLDelete(sql = "UPDATE request__konversi SET status = 'HAPUS' WHERE id=?")
@Where(clause = "status = 'AKTIF'")
public class RequestKonversi extends Auditable {

    @Id
    @GeneratedValue(generator = "uuid")
    @GenericGenerator(name = "uuid", strategy = "uuid2")
    private String id;

    @ManyToOne
    @JoinColumn(name = "id_krs_detail")
    private KrsDetail krsDetail;

    @Lob
    @Column(columnDefinition = "LONGTEXT")
    private String deskripsi;

    private String filename;

    private String namaFile;

    @Enumerated(EnumType.STRING)
    private StatusRecord status = StatusRecord.AKTIF;

    @Enumerated(EnumType.STRING)
    private Approval approval = Approval.WAITING;

    private String approvedBy;

    private LocalDateTime approvedDate;

    private String rejectedBy;

    private LocalDateTime rejectedDate;

    @Enumerated(EnumType.STRING)
    private StatusRecord finalization = StatusRecord.N;

    private String finalizationBy;

    private LocalDateTime finalizationDate;

    private String gradeLama;

    private String gradeBaru;

    @Enumerated(EnumType.STRING)
    private StatusRecord jenisNilai;

    private String rejectReason;


}
