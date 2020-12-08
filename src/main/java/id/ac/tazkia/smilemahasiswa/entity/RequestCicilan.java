package id.ac.tazkia.smilemahasiswa.entity;

import lombok.Data;
import org.hibernate.annotations.GenericGenerator;

import javax.persistence.*;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.time.LocalDate;

@Entity
@Data
public class RequestCicilan {

    @Id @GeneratedValue(generator = "uuid")
    @GenericGenerator(name = "uuid", strategy = "uuid2")
    private String id;

    @ManyToOne
    @JoinColumn(name = "id_tagihan")
    private Tagihan tagihan;

    @NotNull
    private Integer banyakCicilan;

    @Enumerated(EnumType.STRING)
    private StatusApprove statusApprove = StatusApprove.WAITING;

    private LocalDate tanggalApprove;

    @ManyToOne
    @JoinColumn(name = "user_approve")
    private Karyawan userApprove;

    @Enumerated(EnumType.STRING)
    private StatusRecord status = StatusRecord.AKTIF;

    private String keterangan;
    private String keteranganReject;

}
