package id.ac.tazkia.smilemahasiswa.dto.report;

import lombok.Data;

@Data
public class RekapDetailDosenDto {
    private String idDosen;
    private String namaDosen;
    private Integer sks1;
    private Integer status;
    private Integer jumlah;

    public void tambahSks(Integer sks) {
        sks1 = sks1 + sks;
    }
}
