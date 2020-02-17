package id.ac.tazkia.smilemahasiswa.dto.report;

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
