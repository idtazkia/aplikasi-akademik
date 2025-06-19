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

    @ManyToOne
    @JoinColumn(name = "evaluation_target_id", nullable = false)
    private EdomEvaluationTarget evaluationTarget;

    @ManyToOne
    @JoinColumn(name = "id_mahasiswa", nullable = false)
    private Mahasiswa mahasiswa;

    @ManyToOne
    @JoinColumn(name = "question_id", nullable = false)
    private EdomQuestion question;

    @Column(name = "response_value", columnDefinition = "TEXT")
    private String responseValue; // Stores the answer (e.g., '5' for rating, or text for open-ended)
}
