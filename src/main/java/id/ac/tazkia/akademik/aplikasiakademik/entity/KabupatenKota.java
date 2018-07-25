package id.ac.tazkia.akademik.aplikasiakademik.entity;


import lombok.Data;
import org.hibernate.annotations.GenericGenerator;

import javax.persistence.*;
import javax.validation.constraints.NotNull;


@Entity
@Data
public class KabupatenKota {

    @Id
    @GeneratedValue(generator = "uuid")
    @GenericGenerator(name = "uuid", strategy = "uuid2")
    private String idKobab;

    @NotNull
    @ManyToOne
    @JoinColumn(name = "id_provinsi")
    private Provinsi provinsi;

    @NotNull
    @javax.validation.constraints.NotEmpty
    private String nama;

}
