package id.ac.tazkia.smilemahasiswa.entity.edom;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EntityListeners;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import org.hibernate.annotations.GenericGenerator;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import id.ac.tazkia.smilemahasiswa.entity.Auditable;
import id.ac.tazkia.smilemahasiswa.entity.Jadwal;
import id.ac.tazkia.smilemahasiswa.entity.KrsDetail;
import id.ac.tazkia.smilemahasiswa.entity.Mahasiswa; // Assuming this path
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@Entity
@EqualsAndHashCode(callSuper = false)
@EntityListeners(AuditingEntityListener.class)
@Table(name = "edom_general_response")
public class EdomGeneralResponse extends Auditable {

    @Id
    @GeneratedValue(generator = "uuid")
    @GenericGenerator(name = "uuid", strategy = "uuid2")
    private String id;

    private String target;

    @ManyToOne
    @JoinColumn(name = "id_evaluation_target", nullable = false)
    private EdomEvaluationTarget evaluationTarget;

    @ManyToOne
    @JoinColumn(name = "id_question", nullable = false)
    private EdomQuestion question;

    @ManyToOne
    @JoinColumn(name = "id_krs_detail")
    private KrsDetail krsDetail;

    @ManyToOne
    @JoinColumn(name = "id_jadwal", nullable = false)
    private Jadwal jadwal;

    @Column(name = "response_value", columnDefinition = "TEXT")
    private String responseValue;
}
