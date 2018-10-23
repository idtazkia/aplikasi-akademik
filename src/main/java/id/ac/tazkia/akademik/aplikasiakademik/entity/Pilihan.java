package id.ac.tazkia.akademik.aplikasiakademik.entity;

import lombok.Data;

import javax.persistence.*;
import javax.validation.constraints.NotNull;

@Data
@Entity
@Table(name="tb_pilihan")
public class Pilihan {

    @Id
    private String idPilihan;

    private String namaPilihan;

    @NotNull
    @Enumerated(EnumType.STRING)
    private StatusRecord status = StatusRecord.AKTIF;
}
