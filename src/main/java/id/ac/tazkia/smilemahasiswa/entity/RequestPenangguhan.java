package id.ac.tazkia.smilemahasiswa.entity;

import lombok.Data;
import org.apache.tomcat.jni.Local;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.ManyToAny;
import org.hibernate.annotations.NotFound;
import org.springframework.format.annotation.DateTimeFormat;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Data
public class RequestPenangguhan {

    @Id @GeneratedValue(generator = "uuid")
    @GenericGenerator(name = "uuid", strategy = "uuid2")
    private String id;

    @ManyToOne
    @JoinColumn(name = "id_tagihan")
    private Tagihan tagihan;

    @NotNull
    @Column(columnDefinition = "DATE")
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDate tanggalPenangguhan;

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
