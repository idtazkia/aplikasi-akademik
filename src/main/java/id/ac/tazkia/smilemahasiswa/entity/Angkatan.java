package id.ac.tazkia.smilemahasiswa.entity;

import org.hibernate.annotations.Immutable;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Immutable
@Table(name = "angkatan_view")
public class Angkatan {

    @Id
    private String angkatan; // Ini adalah field utama dari view

    // Constructors
    public Angkatan() {}

    public Angkatan(String angkatan) {
        this.angkatan = angkatan;
    }

    // Getters and Setters
    public String getAngkatan() {
        return angkatan;
    }

    public void setAngkatan(String angkatan) {
        this.angkatan = angkatan;
    }

}
