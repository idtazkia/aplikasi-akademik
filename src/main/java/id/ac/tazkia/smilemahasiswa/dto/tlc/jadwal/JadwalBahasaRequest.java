package id.ac.tazkia.smilemahasiswa.dto.tlc.jadwal;

import id.ac.tazkia.smilemahasiswa.entity.Dosen;
import id.ac.tazkia.smilemahasiswa.entity.Hari;
import id.ac.tazkia.smilemahasiswa.entity.Ruangan;
import id.ac.tazkia.smilemahasiswa.entity.TahunAkademik;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalTime;
import java.util.Set;

@Data
public class JadwalBahasaRequest {
    private String id;
    private String jadwal;
    private TahunAkademik tahunAkademik;
    private String kelas;
    private String jenis;
    private Set<Dosen> dosen;
    private Hari hariSesi1;
    private Ruangan ruanganSesi1;
    @DateTimeFormat(pattern = "HH:mm")
    private LocalTime jamMulaiSesi1;
    @DateTimeFormat(pattern = "HH:mm")
    private LocalTime jamSelesaiSesi1;
    private String sesi1;
    private Hari hariSesi2;
    private Ruangan ruanganSesi2;
    @DateTimeFormat(pattern = "HH:mm")
    private LocalTime jamMulaiSesi2;
    @DateTimeFormat(pattern = "HH:mm")
    private LocalTime jamSelesaiSesi2;
    private String sesi2;
    private Hari hariSesi3;
    private Ruangan ruanganSesi3;
    @DateTimeFormat(pattern = "HH:mm")
    private LocalTime jamMulaiSesi3;
    @DateTimeFormat(pattern = "HH:mm")
    private LocalTime jamSelesaiSesi3;
    private String sesi3;
    private Hari hariSesi4;
    private Ruangan ruanganSesi4;
    @DateTimeFormat(pattern = "HH:mm")
    private LocalTime jamMulaiSesi4;
    @DateTimeFormat(pattern = "HH:mm")
    private LocalTime jamSelesaiSesi4;
    private String sesi4;
}
