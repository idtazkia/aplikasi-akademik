package id.ac.tazkia.akademik.aplikasiakademik.entity;

import org.hibernate.annotations.GenericGenerator;

import javax.persistence.*;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import java.io.Serializable;

@Entity
@Table(
        name="running_number",
        uniqueConstraints =
        @UniqueConstraint(columnNames = {"nama", "nomer_terakhir"})
)

public class RunningNumber implements Serializable {
    @Id
    @GeneratedValue(generator = "uuid" )
    @GenericGenerator(name = "uuid", strategy = "uuid2")
    private String id;


    @Column(name = "nama",nullable = false) @NotNull
    private String nama;

    @NotNull
    @Min(1)
    @Column(name="nomer_terakhir", nullable = false)
    private Long nomerTerakhir = 0L;

    public String getId() { return id; }

    public void setId(String id) { this.id = id; }

    public String getNama() { return nama; }

    public void setNama(String nama) { this.nama = nama; }

    public Long getNomerTerakhir() {return nomerTerakhir; }

    public void setNomerTerakhir(Long nomerTerakhir) {this.nomerTerakhir = nomerTerakhir; }
}
