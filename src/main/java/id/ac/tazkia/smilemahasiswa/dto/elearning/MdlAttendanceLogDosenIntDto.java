package id.ac.tazkia.smilemahasiswa.dto.elearning;

import java.math.BigInteger;

public interface MdlAttendanceLogDosenIntDto {

    BigInteger getId();
    String getIdTahunAkademik();
    String getNamaMatakuliah();
    String getIdJadwal();
    String getWaktuMasuk();
    String getWaktuSelesai();
    String getStatusPresensi();
    String getStatus();
    String getIdDosen();
    String getBeritaAcara();
    String getIdLog();
}
