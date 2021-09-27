package id.ac.tazkia.smilemahasiswa.dto.tahunakademik;

import lombok.Data;

import java.util.Date;

@Data
public class TahunAkademikDto {

    private String idTahunAkademik;
    private String kodeTahunAkademik;
    private String namaTahunAkademik;
    private Date tanggalMulai;
    private Date tanggalSelesai;
    private Date tanggalMulaiKrs;
    private Date tanggalSelesaiKrs;
    private Date tanggalMulaiKuliah;
    private Date tanggalSelesaiKuliah;
    private Date tanggalMulaiUts;
    private Date tanggalSelesaiUts;
    private Date tanggalMulaiUas;
    private Date tanggalSelesaiUas;
    private Date tanggalMulaiNilai;
    private Date tanggalSelesaiNilai;
    private String tahun;
    private String status;
    private String jenis;


}
