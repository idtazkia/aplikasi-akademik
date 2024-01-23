package id.ac.tazkia.smilemahasiswa.entity;

import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;

import org.hibernate.annotations.GenericGenerator;

import lombok.Data;

@Entity
@Data
public class KriteriaJurnal {
    @Id
    @GeneratedValue(generator = "uuid")
    @GenericGenerator(name = "uuid" ,strategy = "uuid2")
    private String id;

    private String nama;

    @ManyToOne
    @JoinColumn(name = "id_jenjang")
    private Jenjang jenjang;

    @Enumerated(EnumType.STRING)
    private StatusRecord status;
}
