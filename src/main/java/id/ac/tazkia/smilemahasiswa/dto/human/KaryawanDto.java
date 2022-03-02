package id.ac.tazkia.smilemahasiswa.dto.human;


import lombok.Data;

import javax.persistence.*;
import java.time.LocalDate;

@Data
public class KaryawanDto {

    private String id;
    private String nik;
    private String namaKaryawan;
    private String gelar;
    private String jenisKelamin;
    private String idUser;
    private String Status;
    private String nidn;
    private String email;
    private LocalDate tanggalLahir;
    private String rfid;
    private Integer idAbsen;
    private String foto;



}
