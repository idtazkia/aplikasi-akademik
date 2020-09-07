package id.ac.tazkia.smilemahasiswa.entity;

import lombok.Data;
import org.hibernate.annotations.GenericGenerator;

import javax.persistence.*;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;

@Entity
@Data
public class RequestCicilan {

    @Id @GeneratedValue(generator = "uuid")
    @GenericGenerator(name = "uuid", strategy = "uuid2")
    private String id;

    @ManyToOne
    @JoinColumn(name = "id_tagihan")
    private Tagihan tagihan;

    @NotNull @NotEmpty
    private Integer banyakCicilan;

    @Enumerated(EnumType.STRING)
    private StatusApprove statusApprove = StatusApprove.WAITING;

    @Enumerated(EnumType.STRING)
    private StatusRecord status = StatusRecord.AKTIF;

    private String keterangan;
    private String keteranganReject;

}
