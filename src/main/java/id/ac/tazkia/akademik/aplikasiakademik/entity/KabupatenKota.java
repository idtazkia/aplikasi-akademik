package id.ac.tazkia.akademik.aplikasiakademik.entity;


import lombok.Data;
import org.hibernate.annotations.GenericGenerator;

import javax.persistence.*;
import javax.validation.constraints.NotNull;


@Entity
@Data
@Table(name = "tb_kokab")
public class KabupatenKota {

    @Id
    @GeneratedValue(generator = "uuid")
    @GenericGenerator(name = "uuid", strategy = "uuid2")
    private String idKokab;

    @NotNull
    @ManyToOne
    @JoinColumn(name = "id_provinsi")
    private Provinsi idProvinsi;

    @NotNull
    private String nama;

}
