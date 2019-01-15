package id.ac.tazkia.akademik.aplikasiakademik.entity;

import lombok.Data;
import org.hibernate.annotations.GenericGenerator;
import org.springframework.format.annotation.DateTimeFormat;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.time.LocalDate;

@Entity
@Data
public class Ayah {
    @Id
    @GeneratedValue(generator = "uuid" )
    @GenericGenerator(name = "uuid", strategy = "uuid2")
    private String id;

    @NotNull
    private String namaAyah;

    @NotNull
    private String kebutuhanKhusus;

    @NotNull
    private String tempatLahir;

    @Column(columnDefinition = "DATE")
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDate tanggalLahir;

    @ManyToOne
    @JoinColumn(name = "id_jenjang_pendidikan")
    private Pendidikan idJenjangPendidikan;

    @ManyToOne
    @JoinColumn(name = "id_pekerjaan")
    private Pekerjaan idPekerjaan;

    @ManyToOne
    @JoinColumn(name = "penghasilan")
    private Penghasilan penghasilan;


    @ManyToOne
    @JoinColumn(name = "id_agama")
    private  Agama agama;

    private String statusHidup;

    @NotNull @Enumerated(EnumType.STRING)
    private StatusRecord status = StatusRecord.AKTIF;


}
