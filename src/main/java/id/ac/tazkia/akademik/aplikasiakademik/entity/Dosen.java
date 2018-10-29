package id.ac.tazkia.akademik.aplikasiakademik.entity;

import lombok.Data;
import org.hibernate.annotations.GenericGenerator;

import javax.persistence.*;
import javax.validation.constraints.NotNull;

@Entity
@Data
public class Dosen {
    @Id
    @GeneratedValue(generator = "uuid" )
    @GenericGenerator(name = "uuid", strategy = "uuid2")
    private String id;

    @ManyToOne
    @NotNull
    @JoinColumn(name = "id_prodi_utama")
    private Prodi prodi;

    @Enumerated(EnumType.STRING)
    private StatusRecord status = StatusRecord.Y;

    @ManyToOne
    @NotNull
    @JoinColumn(name = "id_karyawan")
    private Karyawan karyawan;

    private String absen;
}
