package id.ac.tazkia.akademik.aplikasiakademik.entity;

import lombok.Data;

import javax.persistence.*;
import javax.validation.constraints.NotNull;

@Data
@Entity
@Table(name="tb_jenisMk")
public class JenisMk {

    @Id
    private String idJenismk;

    private String jenisMk;

    @NotNull
    @Enumerated(EnumType.STRING)
    private StatusRecord status = StatusRecord.AKTIF;
}
