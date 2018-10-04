package id.ac.tazkia.akademik.aplikasiakademik.entity;

import lombok.Data;
import org.hibernate.annotations.GenericGenerator;
import org.springframework.format.annotation.DateTimeFormat;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.time.LocalDate;

@Entity
@Data
public class Wali {
    @Id
    @GeneratedValue(generator = "uuid" )
    @GenericGenerator(name = "uuid", strategy = "uuid2")
    private String id;

    @NotNull
    private String namaWali;

    @NotNull
    private String kebutuhanKhusus;

    @NotNull
    private String tempatLahir;

    @Column(columnDefinition = "DATE")
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDate tanggalLahir;

    @NotNull
    private String idJenjangPendidikan;

    @NotNull
    private String idPekerjaan;

    @NotNull
    private String idPenghasilan;


    @ManyToOne
    @JoinColumn(name = "id_agama")
    private  Agama agama;

    @NotNull @Enumerated(EnumType.STRING)
    private StatusRecord status = StatusRecord.AKTIF;

    @NotNull
    @ManyToOne @JoinColumn(name = "id_mahasiswa")
    private Mahasiswa mahasiswa;
}
