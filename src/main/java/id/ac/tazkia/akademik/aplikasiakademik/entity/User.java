package id.ac.tazkia.akademik.aplikasiakademik.entity;

import lombok.Data;
import org.hibernate.annotations.GenericGenerator;

import javax.persistence.*;
import javax.validation.constraints.NotNull;

@Entity
@Table(name = "tb_user")
@Data
public class User {

    @Id
    @GeneratedValue(generator = "uuid" )
    @GenericGenerator(name = "uuid", strategy = "uuid2")
    private String id;
    private String username;
    private String email;
    private Boolean active;

        @NotNull
        @ManyToOne @JoinColumn(name = "id_role")
        private Role role;
}
