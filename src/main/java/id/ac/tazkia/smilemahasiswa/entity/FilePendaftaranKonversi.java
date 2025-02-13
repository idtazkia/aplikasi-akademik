package id.ac.tazkia.smilemahasiswa.entity;

import lombok.Data;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.Where;

import javax.persistence.*;

@Entity
@Data
@Where(clause = "status = 'AKTIF'")
public class FilePendaftaranKonversi {
    @Id
    @GeneratedValue(generator = "uuid" )
    @GenericGenerator(name = "uuid", strategy = "uuid2")
    private String id;

    @ManyToOne
    @JoinColumn(name = "id_pendaftaran_konversi")
    private PendaftaranRequestKonversi pendaftaranRequestKonversi;

    @ManyToOne
    @JoinColumn(name = "id_detail_konfigurasi_konversi")
    private DetailKonfigurasiKonversi detailKonfigurasiKonversi;

    private String file;

    @Enumerated(EnumType.STRING)
    private StatusRecord status = StatusRecord.AKTIF;
}
