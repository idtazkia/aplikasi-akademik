package id.ac.tazkia.smilemahasiswa.dto.schedule;

import id.ac.tazkia.smilemahasiswa.entity.*;
import lombok.Data;

@Data
public class RequestJadwal {
    private String id;
    private Hari hari;
    private Kelas kelas;
    private Ruangan ruangan;
    private Sesi sesii;
    private Dosen dosen;
    private String tipe;
    private Integer kapasitas;
    private Akses akses;
}
