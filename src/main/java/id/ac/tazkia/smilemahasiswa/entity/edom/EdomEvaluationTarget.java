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
import id.ac.tazkia.smilemahasiswa.entity.TahunAkademik; // Assuming this path
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@Entity
@EqualsAndHashCode(callSuper = false)
@EntityListeners(AuditingEntityListener.class)
@Table(name = "edom_evaluation_target")
public class EdomEvaluationTarget extends Auditable {

    @Id
    @GeneratedValue(generator = "uuid")
    @GenericGenerator(name = "uuid", strategy = "uuid2")
    private String id;

    @ManyToOne
    @JoinColumn(name = "id_tahun_akademik", nullable = false)
    private TahunAkademik tahunAkademik;

    @ManyToOne
    @JoinColumn(name = "target_type_id", nullable = false)
    private EdomTargetType targetType;

    @Column(name = "target_id", nullable = false, length = 255)
    private String targetId; // Changed to VARCHAR(255) to store names for facilities

    @Column(nullable = false, length = 50)
    private String status = "OPEN"; // e.g., 'OPEN', 'CLOSED', 'COMPLETED'
}
