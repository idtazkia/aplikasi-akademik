package id.ac.tazkia.smilemahasiswa.dto.elearning;


import id.ac.tazkia.smilemahasiswa.entity.Jadwal;
import id.ac.tazkia.smilemahasiswa.entity.TahunAkademik;
import lombok.Data;

import java.math.BigInteger;

@Data
public class MdlAttendanceLogDosenDto {

    private String idSession;
    private String idTahunAkademik;
    private String idJadwal;
    private String waktuMasuk;
    private String waktuSelesai;
    private String statusPresensi;
    private String status;
    private String idDosen;
    private String beritaAcara;
    private String statusimport;
    private String idLog;

}
