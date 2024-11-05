package id.ac.tazkia.smilemahasiswa.dto.tlc.attendance;

import lombok.Data;

@Data
public class PresensiDosenBahasaDto {
    private String id;
    private String jadwal;
    private String sesi;
    private String dosen;
    private String pertemuan;
    private String berita;

}
