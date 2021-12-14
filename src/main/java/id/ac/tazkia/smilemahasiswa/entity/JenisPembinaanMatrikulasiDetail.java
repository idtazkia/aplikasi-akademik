package id.ac.tazkia.smilemahasiswa.entity;

import lombok.Data;
import org.hibernate.annotations.GenericGenerator;

import javax.persistence.*;
import javax.validation.constraints.NotNull;

@Entity
@Data
public class JenisPembinaanMatrikulasiDetail {

    @Id
    @GeneratedValue(generator = "uuid" )
    @GenericGenerator(name = "uuid", strategy = "uuid2")
    private String id;

    @ManyToOne
    @JoinColumn(name = "id_jenis_pembinaan_matrikulasi")
    private JenisPembinaanMatrikulasi jenisPembinaanMatrikulasi;

    @Column(name = "nama_pembinaan_matrikulasi_detail")
    private String namaPembinaanMatrikulasiDetail;

    @Column(name = "nilai")
    private String nilai;

    @Column(name = "keterangan")
    private String keterangan;

    @ManyToOne
    @JoinColumn(name = "id_mahasiswa")
    private Mahasiswa mahasiswa;

    @ManyToOne
    @JoinColumn(name = "id_tahun_akademik")
    private TahunAkademik tahunAkademik;

    @NotNull @Enumerated(EnumType.STRING)
    private StatusRecord status = StatusRecord.AKTIF;

    @ManyToOne
    @JoinColumn(name = "id_s_user")
    private User user;


}
