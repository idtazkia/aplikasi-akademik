package id.ac.tazkia.akademik.aplikasiakademik.entity;

import lombok.Data;
import org.hibernate.annotations.GenericGenerator;

import javax.persistence.*;
import javax.validation.constraints.NotNull;

@Entity
@Data
public class Bipot {
    @Id
    @GeneratedValue(generator = "uuid")
    @GenericGenerator(name = "uuid", strategy = "uuid2")
    private String id;

    @ManyToOne
    @NotNull
    @JoinColumn(name = "id_tahun_akademik")
    private TahunAkademik tahunAkademik;

    @ManyToOne
    @NotNull
    @JoinColumn(name = "id_prodi")
    private Prodi prodi;

    @NotNull
    private String statusSemesterPendek;

    @ManyToOne
    @NotNull
    @JoinColumn(name = "id_program")
    private Program program;

    private String catatan;

    @Enumerated(EnumType.STRING)
    private StatusRecord statusRecord = StatusRecord.AKTIF;
}
