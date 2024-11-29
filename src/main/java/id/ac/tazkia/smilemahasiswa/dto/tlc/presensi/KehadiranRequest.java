package id.ac.tazkia.smilemahasiswa.dto.tlc.presensi;

import lombok.Data;

@Data
public class KehadiranRequest {
    private String mahasiswaId;
    private String kehadiran;
    private String presensiId;
}
