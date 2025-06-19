package id.ac.tazkia.smilemahasiswa.entity.edom;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EntityListeners;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;

import org.hibernate.annotations.GenericGenerator;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import id.ac.tazkia.smilemahasiswa.entity.Auditable;
import id.ac.tazkia.smilemahasiswa.entity.StatusRecord;
import lombok.Data;
import lombok.EqualsAndHashCode;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;

@Data
@Entity
@EqualsAndHashCode(callSuper = false)
@EntityListeners(AuditingEntityListener.class)
@Table(name = "edom_question")
public class EdomQuestion extends Auditable {

    @Id
    @GeneratedValue(generator = "uuid")
    @GenericGenerator(name = "uuid", strategy = "uuid2")
    private String id;

    @Column(name = "question_text", nullable = false, columnDefinition = "TEXT")
    private String questionText;

    @Column(name = "question_type", nullable = false, length = 50)
    private String questionType;

    @ManyToOne
    @JoinColumn(name = "id_category", nullable = false)
    @NotNull
    private EdomCategory edomCategory;

    @Enumerated(value = EnumType.STRING)
    @NotNull
    private StatusRecord status = StatusRecord.AKTIF;
}
