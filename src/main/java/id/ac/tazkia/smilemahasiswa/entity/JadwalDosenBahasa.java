package id.ac.tazkia.smilemahasiswa.entity;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import org.hibernate.annotations.GenericGenerator;
import lombok.Data;

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
