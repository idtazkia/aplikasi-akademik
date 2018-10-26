package id.ac.tazkia.akademik.aplikasiakademik.entity;

import lombok.Data;
import org.hibernate.annotations.GenericGenerator;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.math.BigDecimal;

@Entity
@Data
public class BipotNama {
    @Id
    @GeneratedValue(generator = "uuid")
    @GenericGenerator(name = "uuid", strategy = "uuid2")
    private String id;

    @ManyToOne
    @NotNull
    @JoinColumn(name = "id_rekening")
    private RekeningBank rekeningBank;

    @NotNull
    private Integer nomorUrut;

    @NotNull
    private String namaBipot;

    @NotNull
    private String singkatan;

    @NotNull
    private BigDecimal perkalian;

    @NotNull
    private BigDecimal diskon;

    private String denda;

    private String potonganBeasiswa;

    private String catatan;

    @Enumerated(EnumType.STRING)
    @NotNull
    private StatusRecord statusRecord = StatusRecord.AKTIF;


}
