package id.ac.tazkia.smilemahasiswa.dto.konversi;

import lombok.Data;

@Data
public class RequestKonversiNilai {
    private String mahasiswa;
    private String matakuliah;
    private String kode;
    private String matakuliahLama;
    private String grade;
}
