package id.ac.tazkia.smilemahasiswa.entity;

import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.validation.constraints.NotNull;

import org.hibernate.annotations.GenericGenerator;

import lombok.Data;

@Data
@Entity
public class EdomQuestionOld {

    @Id
    @GeneratedValue(generator = "uuid")
@GenericGenerator(name = "uuid", strategy = "uuid2")
    private String id;

    @NotNull
    private Integer nomor;

    @NotNull
    private String pertanyaan;

    @NotNull
    private String bahasa;

    @Enumerated(EnumType.STRING)
    private StatusRecord status = StatusRecord.AKTIF;

    @ManyToOne
    @JoinColumn(name = "id_tahun_akademik")
    private TahunAkademik tahunAkademik;

}
