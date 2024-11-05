package id.ac.tazkia.smilemahasiswa.entity;

import lombok.Data;
import org.hibernate.annotations.GenericGenerator;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.util.HashSet;
import java.util.Set;

@Entity
@Data
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
