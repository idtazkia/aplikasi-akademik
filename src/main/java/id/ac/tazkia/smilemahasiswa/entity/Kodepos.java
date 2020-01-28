package id.ac.tazkia.smilemahasiswa.entity;

import lombok.Data;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.validation.constraints.NotNull;

@Entity
@Data
public class Kodepos {

    @NotNull
    @Id
    private String kodepos;

    @NotNull
    private String kelurahan;

    @NotNull
    private String kecamatan;

    @NotNull
    private String jenis;

    @NotNull
    private String kabupaten;

    @NotNull
    private String propinsi;
}
