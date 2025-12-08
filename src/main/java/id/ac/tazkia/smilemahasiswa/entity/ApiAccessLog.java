package id.ac.tazkia.smilemahasiswa.entity;

import lombok.Data;
import org.hibernate.annotations.GenericGenerator;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "api_access_log")
@Data
public class ApiAccessLog {

    @Id
    @GeneratedValue(generator = "uuid")
    @GenericGenerator(name = "uuid", strategy = "uuid2")
    private String id;

    @ManyToOne
    @JoinColumn(name = "client_id")
    private ApiClient client;

    @Column(name = "username")
    private String username;

    @Column(name = "ip_address", length = 45)
    private String ipAddress;

    @Column(name = "endpoint")
    private String endpoint;

    @Column(name = "timestamp")
    private LocalDateTime timestamp;

    @Column(name = "success")
    private Boolean success;

    @Column(name = "error_message")
    private String errorMessage;
}
