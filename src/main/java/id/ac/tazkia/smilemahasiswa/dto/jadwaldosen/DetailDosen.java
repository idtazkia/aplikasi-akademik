package id.ac.tazkia.smilemahasiswa.dto.jadwaldosen;


import com.fasterxml.jackson.annotation.JsonBackReference;

public interface DetailDosen {

    String getEmail();
    String getMulai();
    String getSelesai();
    String getMatakuliah();
    String getKelas();
    String getJenis();

}
