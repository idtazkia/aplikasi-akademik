package id.ac.tazkia.smilemahasiswa.dto.krs;

import id.ac.tazkia.smilemahasiswa.entity.Jadwal;
import id.ac.tazkia.smilemahasiswa.entity.MatakuliahKurikulum;
import lombok.Data;

import java.util.List;

@Data
public class GeneratedKrsDto {

    private String id;
    private String nim;
    private String nama;
    private String kelas;

    private List<Jadwal> jadwals;

}
