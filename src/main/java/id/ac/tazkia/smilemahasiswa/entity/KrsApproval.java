package id.ac.tazkia.smilemahasiswa.entity;

import lombok.Data;
import org.hibernate.annotations.GenericGenerator;
import org.springframework.jmx.export.annotation.ManagedAttribute;

import javax.persistence.*;

@Data
@Entity
public class KrsApproval {

    @Id
    @GeneratedValue(generator = "uuid")
    @GenericGenerator(name = "uuid", strategy = "uuid2")
    private String id;

    @ManyToOne
    @JoinColumn(name = "id_krs")
    private Krs krs;

    @ManyToOne
    @JoinColumn(name = "id_mahasiswa")
    private Mahasiswa mahasiswa;

    @Column(columnDefinition = "LONGTEXT")
    private String komentar;

    @Enumerated(EnumType.STRING)
    private StatusApprove status;

}
