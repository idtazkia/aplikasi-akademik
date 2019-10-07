package id.ac.tazkia.akademik.aplikasiakademik.dto;

import id.ac.tazkia.akademik.aplikasiakademik.entity.StatusRecord;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RekapSksDto {
    private String id;
    private String nim;
    private String nama;
    private Integer jumlah;
    private StatusRecord skripsi;

    public void tambahSks(Integer sks) {
        jumlah = jumlah+ sks;
    }
}
