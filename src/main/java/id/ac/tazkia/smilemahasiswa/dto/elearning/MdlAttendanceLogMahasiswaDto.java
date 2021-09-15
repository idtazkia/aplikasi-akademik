package id.ac.tazkia.smilemahasiswa.dto.elearning;

import lombok.Data;

import java.math.BigInteger;

@Data
public class MdlAttendanceLogMahasiswaDto {

    private String idSession;
    private String idTahunAkademik;
    private String idJadwal;
    private String waktuMasuk;
    private String waktuSelesai;
    private String statusPresensi;
    private String status;
    private String mahasiswa;
    private String nim;
}
