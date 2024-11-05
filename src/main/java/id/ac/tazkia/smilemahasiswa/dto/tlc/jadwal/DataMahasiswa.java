package id.ac.tazkia.smilemahasiswa.dto.tlc.jadwal;

import id.ac.tazkia.smilemahasiswa.entity.Mahasiswa;
import lombok.Data;

import java.util.Set;

@Data
public class DataMahasiswa {
    private Integer total;
    private Integer totalNotFiltered;
    Set<Mahasiswa> rows;
}
