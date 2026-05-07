package id.ac.tazkia.smilemahasiswa.controller;

import id.ac.tazkia.smilemahasiswa.dao.surat.SuratKeteranganMahasiswaDao;
import id.ac.tazkia.smilemahasiswa.dao.surat.SuratMagangDao;
import id.ac.tazkia.smilemahasiswa.dao.surat.SuratPenelitianDao;
import id.ac.tazkia.smilemahasiswa.dao.surat.SuratTugasKuliahDao;
import id.ac.tazkia.smilemahasiswa.entity.StatusRecord;
import id.ac.tazkia.smilemahasiswa.entity.surat.SuratKeteranganMahasiswa;
import id.ac.tazkia.smilemahasiswa.entity.surat.SuratMagang;
import id.ac.tazkia.smilemahasiswa.entity.surat.SuratPenelitian;
import id.ac.tazkia.smilemahasiswa.entity.surat.SuratTugasKuliah;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.Optional;

@Controller
@RequestMapping("/surat")
@Slf4j
public class VerifikasiSuratController {

    @Autowired
    private SuratTugasKuliahDao suratTugasKuliahDao;

    @Autowired
    private SuratKeteranganMahasiswaDao suratKeteranganMahasiswaDao;

    @Autowired
    private SuratMagangDao suratMagangDao;

    @Autowired
    private SuratPenelitianDao suratPenelitianDao;

    @GetMapping("/verifikasi/{id}")
    public String verifikasiSurat(@PathVariable String id, Model model) {
        log.info("Memverifikasi surat dengan ID: {}", id);

        // Cari di semua jenis surat
        Optional<SuratTugasKuliah> tugasKuliah = suratTugasKuliahDao.findById(id);
        if (tugasKuliah.isPresent() && tugasKuliah.get().getStatus() == StatusRecord.AKTIF) {
            model.addAttribute("surat", tugasKuliah.get());
            model.addAttribute("jenisSurat", "Surat Tugas Kuliah");
            return "surat/verifikasi-surat";
        }

        Optional<SuratKeteranganMahasiswa> keteranganMahasiswa = suratKeteranganMahasiswaDao.findById(id);
        if (keteranganMahasiswa.isPresent() && keteranganMahasiswa.get().getStatus() == StatusRecord.AKTIF) {
            model.addAttribute("surat", keteranganMahasiswa.get());
            model.addAttribute("jenisSurat", "Surat Keterangan Mahasiswa");
            return "surat/verifikasi-surat";
        }

        Optional<SuratMagang> magang = suratMagangDao.findById(id);
        if (magang.isPresent() && magang.get().getStatus() == StatusRecord.AKTIF) {
            model.addAttribute("surat", magang.get());
            model.addAttribute("jenisSurat", "Surat Magang");
            return "surat/verifikasi-surat";
        }

        Optional<SuratPenelitian> penelitian = suratPenelitianDao.findById(id);
        if (penelitian.isPresent() && penelitian.get().getStatus() == StatusRecord.AKTIF) {
            model.addAttribute("surat", penelitian.get());
            model.addAttribute("jenisSurat", "Surat Penelitian");
            return "surat/verifikasi-surat";
        }

        model.addAttribute("id", id);
        return "surat/verifikasi-tidak-ditemukan";
    }
}
