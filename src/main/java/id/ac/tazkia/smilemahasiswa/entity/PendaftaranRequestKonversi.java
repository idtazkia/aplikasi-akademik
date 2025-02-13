package id.ac.tazkia.smilemahasiswa.entity;

import com.fasterxml.jackson.annotation.JsonBackReference;
import id.ac.tazkia.smilemahasiswa.constant.Approval;
import lombok.Data;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.Where;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity
@Data
@Where(clause = "status not in ('HAPUS')")
public class PendaftaranRequestKonversi extends Auditable {
    @Id
    @GeneratedValue(generator = "uuid" )
    @GenericGenerator(name = "uuid", strategy = "uuid2")
    private String id;

    @ManyToOne
    @JoinColumn(name = "id_mahasiswa")
    @JsonBackReference
    private Mahasiswa mahasiswa;

    private Integer totalSks = 0;

    @ManyToOne
    @JoinColumn(name = "id_konfigurasi_konversi")
    private KonfigurasiKonversi konfigurasiKonversi;

    @Enumerated(EnumType.STRING)
    private Approval status = Approval.UNFINISHED;

    @OneToMany(mappedBy = "pendaftaranRequestKonversi")
    @JsonBackReference
    private List<FilePendaftaranKonversi> files = new ArrayList<>();

    @OneToMany(mappedBy = "pendaftaranRequestKonversi")
    @JsonBackReference
    private List<DetailPendaftaranKonversi> detailPendaftaranKonversiList = new ArrayList<>();

}
