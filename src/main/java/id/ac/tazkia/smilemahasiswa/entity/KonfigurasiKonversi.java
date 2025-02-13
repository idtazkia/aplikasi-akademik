package id.ac.tazkia.smilemahasiswa.entity;

import lombok.Data;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.Where;

import javax.persistence.*;
import javax.validation.constraints.NotNull;

@Data
@Entity
@Where(clause = "status = 'AKTIF'")
public class KonfigurasiKonversi {
    @Id
    @GeneratedValue(generator = "uuid" )
    @GenericGenerator(name = "uuid", strategy = "uuid2")
    private String id;

    @NotNull
    private String nama;

    @Enumerated(EnumType.STRING)
    private jenis jenis;

    @Enumerated(EnumType.STRING)
    private sistem sistem;

    private Integer maksimalUpload;

    @NotNull
    @Enumerated(EnumType.STRING)
    private StatusRecord status = StatusRecord.AKTIF;

    public enum jenis{
        AKADEMIK, NON_AKADEMIK
    }

    public enum sistem{
        POINT, LEVEL, ASSESMENT
    }
}
