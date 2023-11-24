package id.ac.tazkia.smilemahasiswa.entity.BebasKewajiban;

import id.ac.tazkia.smilemahasiswa.entity.Mahasiswa;
import id.ac.tazkia.smilemahasiswa.entity.StatusRecord;
import lombok.Data;
import org.hibernate.annotations.GenericGenerator;

import javax.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Entity
public class KewajibanSertifikatTlc {

    @Id
    @GeneratedValue(generator = "uuid")
    @GenericGenerator(name = "uuid", strategy = "uuid2")
    private String id;

    @ManyToOne
    @JoinColumn(name = "id_pendaftaran")
    private PendaftaranBebasKewajiban bebasKewajiban;

    private LocalDate tanggalUjian;

    private String tempatUjian;

    private String file;

    @Enumerated(EnumType.STRING)
    private ByBagian keteranganUjian;

    @Enumerated(EnumType.STRING)
    private StatusRecord status = StatusRecord.AKTIF;

    private LocalDateTime tanggalInput = LocalDateTime.now();

}
