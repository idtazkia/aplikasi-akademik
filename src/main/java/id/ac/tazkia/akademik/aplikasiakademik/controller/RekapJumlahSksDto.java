package id.ac.tazkia.akademik.aplikasiakademik.controller;

import id.ac.tazkia.akademik.aplikasiakademik.entity.Dosen;
import lombok.Data;

@Data
public class RekapJumlahSksDto {

    private String namaDosen;
    private  String namaProdi;
    private Integer jumlahSKs;
}
