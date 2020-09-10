package id.ac.tazkia.smilemahasiswa.entity;

import lombok.Data;
import org.apache.tomcat.jni.Local;
import org.hibernate.annotations.GenericGenerator;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;

@Entity
@Data
public class MatakuliahSetara {

    @Id
    @GeneratedValue(generator = "uuid" )
    @GenericGenerator(name = "uuid", strategy = "uuid2")
    private String id;

    @ManyToOne
    @JoinColumn(name = "id_matakuliah")
    private Matakuliah matakuliah;

    @ManyToOne
    @JoinColumn(name = "id_matakuliah_setara")
    private Matakuliah matakuliahSetara;

    @NotNull
    @Enumerated(EnumType.STRING)
    private StatusRecord status = StatusRecord.AKTIF;

    private String userInsert;

    private LocalDateTime tanggalInsert;

    private String userEdit;

    private LocalDateTime tanggalEdit;

    private String userDelete;

    private LocalDateTime tanggalDelete;

}
