package id.ac.tazkia.smilemahasiswa.entity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.validation.constraints.NotNull;

import org.hibernate.annotations.GenericGenerator;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;

import lombok.Data;

@JsonIdentityInfo(generator = ObjectIdGenerators.PropertyGenerator.class, property = "id")
@Data
@Entity
public class Kurikulum {

    @Id
    @GeneratedValue(generator = "uuid")
    @GenericGenerator(name = "uuid", strategy = "uuid2")
    private String id;

    @ManyToOne
    @JoinColumn(name = "id_prodi")
    private Prodi prodi;

    /**
     * ID Kurikulum dari PDDIKTI Feeder (UUID format)
     * Contoh: "d7ee4c09-b7a1-424b-b76d-9a761cb7acbe"
     */
    @Column(name = "id_pddikti")
    private String idPddikti;

    private String tahunKurikulum;

    private String namaKurikulum;

    private String sesi;

    private Integer jumlahSesi;

    private String nomorSkRektor;

    @NotNull
    @Enumerated(EnumType.STRING)
    private StatusRecord status = StatusRecord.NONAKTIF;

}
