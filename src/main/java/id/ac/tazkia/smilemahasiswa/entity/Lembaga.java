package id.ac.tazkia.smilemahasiswa.entity;

import lombok.Data;
import org.hibernate.annotations.GenericGenerator;

import javax.persistence.*;
import javax.validation.constraints.NotNull;

@Entity
@Data
public class Lembaga {

    @Id
    @GeneratedValue(generator = "uuid" )
    @GenericGenerator(name = "uuid", strategy = "uuid2")
    private String id;

    @NotNull
    private String kodeLembaga;

    @NotNull
    private String namaLembaga;

    @Enumerated(EnumType.STRING)
    private StatusRecord status = StatusRecord.AKTIF;



}
