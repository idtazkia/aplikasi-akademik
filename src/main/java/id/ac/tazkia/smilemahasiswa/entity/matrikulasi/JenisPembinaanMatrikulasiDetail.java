package id.ac.tazkia.smilemahasiswa.entity.matrikulasi;

import id.ac.tazkia.smilemahasiswa.entity.Pendidikan;
import id.ac.tazkia.smilemahasiswa.entity.StatusRecord;
import lombok.Data;
import org.hibernate.annotations.GenericGenerator;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.math.BigDecimal;

@Entity
@Data
public class JenisPembinaanMatrikulasiDetail {

    @Id
    @GeneratedValue(generator = "uuid" )
    @GenericGenerator(name = "uuid", strategy = "uuid2")
    private String id;

    @ManyToOne
    @JoinColumn(name = "id_jenis_pembinaan_matrikulasi")
    private JenisPembinaanMatrikulasi jenisPembinaanMatrikulasi;

    @NotNull
    private String namaPembinaanMatrikulasiDetail;

    private BigDecimal bobot;

    @NotNull @Enumerated(EnumType.STRING)
    private StatusRecord status = StatusRecord.AKTIF;


}
