package id.ac.tazkia.smilemahasiswa.entity;

import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import javax.persistence.EntityListeners;
import javax.persistence.MappedSuperclass;
import java.time.LocalDateTime;

@MappedSuperclass
@EntityListeners(AuditingEntityListener.class)
public abstract class Auditable {
    @CreatedDate
    protected LocalDateTime createdTime;

    @CreatedBy
    protected String createdBy;

    @LastModifiedDate
    protected LocalDateTime lastModifiedTime;

    @LastModifiedBy
    protected String lastModifiedBy;
}
