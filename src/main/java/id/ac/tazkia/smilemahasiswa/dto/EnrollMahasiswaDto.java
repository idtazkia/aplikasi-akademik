package id.ac.tazkia.smilemahasiswa.dto;


import lombok.Data;

@Data
public class EnrollMahasiswaDto {

    private String idNumberElearning;
    private String idKrs;
    private String idKrsDetail;
    private String idJadwal;
    private String idMahasiswa;
    private String email;
    private String nim;
    private String statusEnroll;

}
