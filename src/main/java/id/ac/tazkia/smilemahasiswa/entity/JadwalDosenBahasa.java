package id.ac.tazkia.smilemahasiswa.entity;

import lombok.Data;
import org.hibernate.annotations.GenericGenerator;
import org.springframework.format.annotation.DateTimeFormat;

import javax.persistence.*;
import java.time.LocalTime;

@Entity
@Data
public class JadwalDosenBahasa {
    @Id
    @GeneratedValue(generator = "uuid" )
    @GenericGenerator(name = "uuid", strategy = "uuid2")
    private String id;

    @ManyToOne
    @JoinColumn(name = "id_jadwal_bahasa")
    private JadwalBahasa jadwalBahasa;

    @ManyToOne
    @JoinColumn(name = "id_dosen")
    private Dosen dosen ;

}
