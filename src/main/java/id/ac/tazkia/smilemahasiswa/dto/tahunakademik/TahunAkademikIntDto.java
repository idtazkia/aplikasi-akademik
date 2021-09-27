package id.ac.tazkia.smilemahasiswa.dto.tahunakademik;

import java.util.Date;

public interface TahunAkademikIntDto {

    String getIdTahunAkademik();
    String getKodeTahunAkademik();
    String getNamaTahunAkademik();
    Date getTanggalMulai();
    Date getTanggalSelesai();
    Date getTanggalMulaiKrs();
    Date getTanggalSelesaiKrs();
    Date getTanggalMulaiKuliah();
    Date getTanggalSelesaiKuliah();
    Date getTanggalMulaiUts();
    Date getTanggalSelesaiUts();
    Date getTanggalMulaiUas();
    Date getTanggalSelesaiUas();
    Date getTanggalMulaiNilai();
    Date getTanggalSelesaiNilai();
    String getTahun();
    String getStatus();
    String getJenis();

}
