package id.ac.tazkia.smilemahasiswa.entity;

import lombok.Data;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "s_permission") @Data
public class Permission {
    @Id
    private String id;
    @Column(name = "permission_label")
    private String label;
    @Column(name = "permission_value")
    private String value;
}
