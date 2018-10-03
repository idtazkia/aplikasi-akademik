package id.ac.tazkia.akademik.aplikasiakademik.entity;

import lombok.Data;
import org.hibernate.annotations.GenericGenerator;
import org.springframework.format.annotation.DateTimeFormat;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.time.LocalDate;

@Entity
@Data
public class Krs {

    @Id
    @GeneratedValue(generator = "uuid")
    @GenericGenerator(name = "uuid", strategy = "uuid2")
    private String id;

    @NotNull
    @JoinColumn(name = "id_tahun_akademik")
    private String idTahunAkademik;

    @NotNull
    @JoinColumn(name = "id_tahun_akademik_prodi")
    private String idTahunAkademikProdi;

    @NotNull
    @JoinColumn(name = "id_prodi")
    private String idProdi;

    @NotNull
    @JoinColumn(name = "id_mahasiswa")
    private String idMahasiswa;

    private String nim;

    private String tanggalTransaksi;

    private String ip;

    @NotNull
    @Enumerated(EnumType.STRING)
    private StatusRecord status = StatusRecord.AKTIF;
}
