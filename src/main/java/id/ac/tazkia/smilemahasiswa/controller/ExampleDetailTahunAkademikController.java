package id.ac.tazkia.smilemahasiswa.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import id.ac.tazkia.smilemahasiswa.entity.DetailTahunAkademik;
import id.ac.tazkia.smilemahasiswa.entity.Mahasiswa;
import id.ac.tazkia.smilemahasiswa.entity.TahunAkademik;
import id.ac.tazkia.smilemahasiswa.service.DetailTahunAkademikService;

/**
 * Contoh penggunaan DetailTahunAkademikService dengan caching
 */
@Controller
@RequestMapping("/example")
public class ExampleDetailTahunAkademikController {

    @Autowired
    private DetailTahunAkademikService detailTahunAkademikService;

    /**
     * Contoh penggunaan cache dalam controller
     */
    @GetMapping("/mahasiswa-schedule")
    public String getMahasiswaSchedule(Model model) {
        // Misalkan kita punya data mahasiswa dan tahun akademik dari session/security
        // context
        Mahasiswa mahasiswa = getCurrentMahasiswa(); // implement sesuai kebutuhan
        TahunAkademik tahunAkademik = getCurrentTahunAkademik(); // implement sesuai kebutuhan

        // Panggil service - hasil akan di-cache otomatis
        DetailTahunAkademik detailTA = detailTahunAkademikService
                .getDetailTahunAkademikForMahasiswa(tahunAkademik, mahasiswa);

        if (detailTA != null) {
            model.addAttribute("tanggalMulaiKrs", detailTA.getTanggalMulaiKrs());
            model.addAttribute("tanggalSelesaiKrs", detailTA.getTanggalSelesaiKrs());
            model.addAttribute("tanggalMulaiKuliah", detailTA.getTanggalMulaiKuliah());
            model.addAttribute("tanggalSelesaiKuliah", detailTA.getTanggalSelesaiKuliah());
            model.addAttribute("tanggalMulaiUts", detailTA.getTanggalMulaiUts());
            model.addAttribute("tanggalSelesaiUts", detailTA.getTanggalSelesaiUts());
            model.addAttribute("tanggalMulaiUas", detailTA.getTanggalMulaiUas());
            model.addAttribute("tanggalSelesaiUas", detailTA.getTanggalSelesaiUas());
        }

        return "mahasiswa/schedule";
    }

    /**
     * Contoh untuk clear cache ketika admin update data
     */
    @GetMapping("/admin/clear-cache")
    public String clearCache() {
        // Clear semua cache DetailTahunAkademik
        detailTahunAkademikService.clearAllCache();

        return "redirect:/admin/detail-tahun-akademik";
    }

    // Mock methods - implement sesuai dengan sistem authentication yang ada
    private Mahasiswa getCurrentMahasiswa() {
        // TODO: Implement sesuai dengan sistem authentication
        return new Mahasiswa();
    }

    private TahunAkademik getCurrentTahunAkademik() {
        // TODO: Implement sesuai dengan logic aplikasi
        return new TahunAkademik();
    }
}
