package id.ac.tazkia.smilemahasiswa.entity;

import lombok.Data;
import org.hibernate.annotations.GenericGenerator;

import javax.persistence.*;
import java.time.LocalDate;

@Entity
@Data
public class RequestKonversiTenggat {

    @Id
    @GeneratedValue(generator = "uuid")
    @GenericGenerator(name = "uuid", strategy = "uuid2")
    private String id;

    private LocalDate tanggalAwal;

    private LocalDate tanggalAkhir;

    @Enumerated(EnumType.STRING)
    private StatusRecord status = StatusRecord.AKTIF;


}
