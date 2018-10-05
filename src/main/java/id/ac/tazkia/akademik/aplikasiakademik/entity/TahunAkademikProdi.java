package id.ac.tazkia.akademik.aplikasiakademik.entity;


import lombok.Data;
import org.hibernate.annotations.GenericGenerator;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.util.Date;

@Entity
@Data
public class TahunAkademikProdi {

    @Id
    @GeneratedValue(generator = "uuid" )
    @GenericGenerator(name = "uuid", strategy = "uuid2")
    private String id;

    @ManyToOne
    @JoinColumn(name = "id_tahun_akademik")
    private TahunAkademik idTahunAkademik;

    private Date mulaiKrs;

    private Date selesaiKRS;

    private Date mulaiKuliah;

    private Date selesaiKuliah;

    private Date mulaiUts;

    private Date selesaiUts;

    private Date mulaiUas;

    private Date selesaiUas;

    private Date mulaiNilai;

    private Date selesaiNilai;

    @ManyToOne
    @JoinColumn(name = "id_prodi")
    private Prodi idProdi;

    @ManyToOne
    @JoinColumn(name = "id_kurikulum")
    private Kurikulum idKurikulum;

    @NotNull
    @Enumerated(EnumType.STRING)
    private StatusRecord status = StatusRecord.AKTIF;

}
