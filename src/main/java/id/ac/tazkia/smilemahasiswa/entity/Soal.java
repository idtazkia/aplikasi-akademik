package id.ac.tazkia.smilemahasiswa.entity;

import lombok.Data;
import org.hibernate.annotations.GenericGenerator;
import org.springframework.format.annotation.DateTimeFormat;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.time.LocalDate;

@Data
@Entity
public class Soal {
    @Id
    @GeneratedValue(generator = "uuid" )
    @GenericGenerator(name = "uuid", strategy = "uuid2")
    private String id;

    @ManyToOne
    @NotNull
    @JoinColumn(name = "id_jadwal")
    private Jadwal jadwal;

    @NotNull
    private String fileUpload;

    @ManyToOne
    @NotNull
    @JoinColumn(name = "id_dosen")
    private Dosen dosen;

    private String keterangan;

    @Enumerated(EnumType.STRING)
    private StatusRecord status;

    @Column(columnDefinition = "DATE")
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDate tanggalUpload;

    private String fileApprove;

    private String keteranganApprove;

    @Enumerated(EnumType.STRING)
    private StatusApprove statusApprove;

    @Enumerated(EnumType.STRING)
    private StatusRecord statusSoal;
}
