package id.ac.tazkia.smilemahasiswa.entity;

import lombok.Data;
import org.hibernate.annotations.GenericGenerator;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;

@Entity
@Table(name = "api_client")
@Data
public class ApiClient {

    @Id
    @GeneratedValue(generator = "uuid")
    @GenericGenerator(name = "uuid", strategy = "uuid2")
    private String id;

    @NotNull
    @Column(name = "client_name", nullable = false, length = 100)
    private String clientName;

    @NotNull
    @Column(name = "api_key_hash", nullable = false, length = 255)
    private String apiKeyHash;

    @ManyToOne
    @JoinColumn(name = "created_by")
    private User createdBy;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "last_used_at")
    private LocalDateTime lastUsedAt;

    @Column(name = "last_used_by")
    private String lastUsedBy;

    @Column(name = "is_active")
    private Boolean isActive = true;

    @Column(name = "allowed_ips", columnDefinition = "TEXT")
    private String allowedIps;

    @Column(name = "rate_limit_per_hour")
    private Integer rateLimitPerHour = 1000;

    @Column(name = "notes", columnDefinition = "TEXT")
    private String notes;
}
