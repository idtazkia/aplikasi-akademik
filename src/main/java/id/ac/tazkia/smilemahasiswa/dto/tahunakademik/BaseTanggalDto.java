package id.ac.tazkia.smilemahasiswa.dto.tahunakademik;

import java.time.LocalDate;

import javax.validation.constraints.NotNull;

import org.springframework.format.annotation.DateTimeFormat;

import lombok.Data;

/**
 * Base class untuk DTO yang memiliki field tanggal-tanggal tahun akademik
 * Digunakan untuk menghindari duplikasi field tanggal di berbagai DTO
 */
@Data
public abstract class BaseTanggalDto {

    @NotNull(message = "Tanggal mulai KRS tidak boleh kosong")
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDate tanggalMulaiKrs;

    @NotNull(message = "Tanggal selesai KRS tidak boleh kosong")
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDate tanggalSelesaiKrs;

    @NotNull(message = "Tanggal mulai kuliah tidak boleh kosong")
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDate tanggalMulaiKuliah;

    @NotNull(message = "Tanggal selesai kuliah tidak boleh kosong")
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDate tanggalSelesaiKuliah;

    @NotNull(message = "Tanggal mulai UTS tidak boleh kosong")
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDate tanggalMulaiUts;

    @NotNull(message = "Tanggal selesai UTS tidak boleh kosong")
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDate tanggalSelesaiUts;

    @NotNull(message = "Tanggal mulai UAS tidak boleh kosong")
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDate tanggalMulaiUas;

    @NotNull(message = "Tanggal selesai UAS tidak boleh kosong")
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDate tanggalSelesaiUas;

    @NotNull(message = "Tanggal mulai nilai tidak boleh kosong")
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDate tanggalMulaiNilai;

    @NotNull(message = "Tanggal selesai nilai tidak boleh kosong")
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDate tanggalSelesaiNilai;
}
