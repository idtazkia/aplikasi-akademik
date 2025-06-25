package id.ac.tazkia.smilemahasiswa.entity.edom;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import org.hibernate.annotations.GenericGenerator;

import id.ac.tazkia.smilemahasiswa.entity.TahunAkademik; // Assuming this path
import lombok.Data;

@Data
@Entity
@Table(name = "edom_question_config")
public class EdomQuestionConfig {

    @Id
    @GeneratedValue(generator = "uuid")
    @GenericGenerator(name = "uuid", strategy = "uuid2")
    private String id;

    @ManyToOne
    @JoinColumn(name = "id_tahun_akademik")
    private TahunAkademik tahunAkademik;

    private String targetType;

    @ManyToOne
    @JoinColumn(name = "id_question")
    private EdomQuestion question;
}
