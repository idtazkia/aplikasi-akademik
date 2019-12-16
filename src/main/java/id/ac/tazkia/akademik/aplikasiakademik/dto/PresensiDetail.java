package id.ac.tazkia.akademik.aplikasiakademik.dto;

import id.ac.tazkia.akademik.aplikasiakademik.entity.StatusPresensi;
import lombok.Data;

import javax.persistence.Column;
import javax.persistence.Lob;

@Data
public class PresensiDetail {
    private String jamMasuk;
    private StatusPresensi presensi;
    @Lob
    @Column(columnDefinition = "LONGTEXT")
    private String materi;
}
