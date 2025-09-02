package id.ac.tazkia.smilemahasiswa.dto.tahunakademik;

import java.time.LocalDate;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

import org.springframework.format.annotation.DateTimeFormat;

import id.ac.tazkia.smilemahasiswa.entity.StatusRecord;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class CreateTahunAkademikDto extends BaseTanggalDto {

    // Field untuk edit mode
    private String id;

    @NotBlank(message = "Kode tahun akademik tidak boleh kosong")
    private String kodeTahunAkademik;

    @NotBlank(message = "Nama tahun akademik tidak boleh kosong")
    private String namaTahunAkademik;

    @NotNull(message = "Tanggal mulai tidak boleh kosong")
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDate tanggalMulai;

    @NotNull(message = "Tanggal selesai tidak boleh kosong")
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDate tanggalSelesai;

    @NotNull(message = "Status tidak boleh kosong")
    private StatusRecord status;

    // Auto-generate dari kode
    private String tahun;
    private StatusRecord jenis;

    /**
     * Method untuk auto-generate tahun dan jenis dari kodeTahunAkademik
     */
    public void deriveFromKode() {
        if (kodeTahunAkademik != null && kodeTahunAkademik.length() >= 5) {
            this.tahun = kodeTahunAkademik.substring(0, 4);
            String kode = kodeTahunAkademik.substring(4, 5);

            switch (kode) {
                case "1":
                    this.jenis = StatusRecord.GANJIL;
                    break;
                case "2":
                    this.jenis = StatusRecord.GENAP;
                    break;
                case "3":
                    this.jenis = StatusRecord.PENDEK;
                    break;
                default:
                    this.jenis = StatusRecord.GANJIL;
            }
        }
    }
}
