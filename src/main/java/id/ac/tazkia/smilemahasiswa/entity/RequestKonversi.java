package id.ac.tazkia.smilemahasiswa.entity;

import lombok.Data;
import org.hibernate.annotations.GenericGenerator;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Data
public class RequestKonversi {

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

    private String createdBy;

    private LocalDateTime createdDate;

    @Enumerated(EnumType.STRING)
    private StatusRecord status = StatusRecord.AKTIF;

    private String deletedBy;

    private LocalDateTime deletedDate;

    @Enumerated(EnumType.STRING)
    private StatusRecord approval = StatusRecord.WAITING;

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

    @Enumerated(EnumType.STRING)
    private StatusRecord statusTenggat = StatusRecord.N;

    @ManyToOne
    @JoinColumn(name = "id_request_konversi_tenggat")
    private RequestKonversiTenggat requestKonversiTenggat;

    private String rejectReason;


}
