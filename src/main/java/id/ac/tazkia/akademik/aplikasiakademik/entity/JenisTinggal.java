package id.ac.tazkia.akademik.aplikasiakademik.entity;

import lombok.Data;
import org.hibernate.annotations.GenericGenerator;

import javax.persistence.*;
import javax.validation.constraints.NotNull;

@Data
@Entity
public class JenisTinggal {

    @Id
    @GeneratedValue(generator = "uuid")
    @GenericGenerator(name = "uuid", strategy = "uuid2")
    private String id;

    @NotNull
    private String jenisTinggal;

    @NotNull
    private String keterangan;

    @Enumerated(value = EnumType.STRING)
    @NotNull
    private StatusRecord status = StatusRecord.AKTIF ;
}
