package id.ac.tazkia.akademik.aplikasiakademik.entity;

import lombok.Data;
import org.hibernate.annotations.GenericGenerator;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.sql.Time;
import java.util.Date;

@Entity
@Data
public class PresensiMahasiswa {

    @Id
    @GeneratedValue(generator = "uuid" )
    @GenericGenerator(name = "uuid", strategy = "uuid2")
    private String id;

    @ManyToOne
    @JoinColumn(name = "id_krs_detail")
    private KrsDetail krsDetail;

    @ManyToOne
    @JoinColumn(name = "id_presensi_dosen")
    private PresensiDosen presensiDosen;

    @ManyToOne
    @JoinColumn(name = "id_mahasiswa")
    private Mahasiswa mahasiswa;

    private Date tanggalMasuk;

    private Time jamMasuk;

    private Time jamKeluar;

    private String statusPresensi;

    private String catatan;

    @NotNull
    @Enumerated(EnumType.STRING)
    private StatusRecord status = StatusRecord.AKTIF;

}
