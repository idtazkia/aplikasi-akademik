package id.ac.tazkia.smilemahasiswa.dto.attendance;

import id.ac.tazkia.smilemahasiswa.entity.StatusPresensi;

public interface PresensiMahasiswaDto {
    String getIdSesi();
    String getIdPresensi();
    String getIdMahasiswa();
    String getKrs();
    String getNama();
    String getNim();
    StatusPresensi getPresensi();
    String getTime();
}
