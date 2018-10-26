package id.ac.tazkia.akademik.aplikasiakademik.entity;

import lombok.Data;
import org.hibernate.annotations.GenericGenerator;

import javax.persistence.*;
import javax.validation.constraints.NotNull;

@Data
@Entity
public class RekeningBank {
    @Id
    @GeneratedValue(generator = "uuid")
    @GenericGenerator(name = "uuid", strategy = "uuid2")
    private String id;

    @NotNull
    private String nomorRekening;

    @NotNull
    private String namaBank;

    @NotNull
    private String keterangan;

    @Enumerated(EnumType.STRING)
    private StatusRecord statusRecord = StatusRecord.AKTIF;
}
