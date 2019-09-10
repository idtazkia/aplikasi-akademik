package id.ac.tazkia.akademik.aplikasiakademik.dto;

import lombok.Data;

@Data
public class RekapSksDosenDto {
    private String idDosen;
    private String namaDosen;
    private Integer totalSks;

    public void tambahSks(Integer sks) {
        totalSks = totalSks + sks;
    }
}
