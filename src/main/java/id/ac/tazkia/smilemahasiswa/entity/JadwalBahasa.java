package id.ac.tazkia.smilemahasiswa.entity;

import java.util.HashSet;
import java.util.Set;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.OrderBy;
import javax.validation.constraints.NotNull;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.Where;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class JadwalBahasa {
    @Id
    @GeneratedValue(generator = "uuid" )
    @GenericGenerator(name = "uuid", strategy = "uuid2")
    private String id;

    @ManyToOne
    @JoinColumn(name = "id_tahun_akademik")
    private TahunAkademik tahunAkademik;

    private String namaJadwal;

    private String namaKelas;

    private String jenis;

    @ManyToMany
    @JoinTable(name = "jadwal_dosen_bahasa",
            joinColumns=@JoinColumn(name = "id_jadwal_bahasa"),
            inverseJoinColumns = @JoinColumn(name = "id_dosen"))
    @OrderBy
    private Set<Dosen> dosens = new HashSet<>();

    @ManyToMany
    @JoinTable(name = "mahasiswa_jadwal_bahasa",
            joinColumns=@JoinColumn(name = "id_jadwal_bahasa"),
            inverseJoinColumns = @JoinColumn(name = "id_mahasiswa"))
    @OrderBy
    private Set<Mahasiswa> mahasiswas = new HashSet<>();

    @NotNull
    @Enumerated(EnumType.STRING)
    private StatusRecord status = StatusRecord.AKTIF;
}
