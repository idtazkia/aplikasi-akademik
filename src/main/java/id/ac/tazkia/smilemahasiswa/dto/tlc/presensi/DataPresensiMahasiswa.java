package id.ac.tazkia.smilemahasiswa.dto.tlc.presensi;

import id.ac.tazkia.smilemahasiswa.entity.Mahasiswa;
import lombok.Builder;
import lombok.Data;

import java.util.List;
import java.util.Set;

@Data
public class DataPresensiMahasiswa {
    private Integer total;
    private Integer totalNotFiltered;
    List<GetDataPresensiMahasiswa> rows;
}
