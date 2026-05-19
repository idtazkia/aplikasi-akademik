package id.ac.tazkia.smilemahasiswa.controller.surat.mahasiswa;

import id.ac.tazkia.smilemahasiswa.dao.KrsDetailDao;
import id.ac.tazkia.smilemahasiswa.dao.MahasiswaDao;
import id.ac.tazkia.smilemahasiswa.dao.surat.SuratPenelitianDao;
import id.ac.tazkia.smilemahasiswa.entity.Mahasiswa;
import id.ac.tazkia.smilemahasiswa.entity.StatusRecord;
import id.ac.tazkia.smilemahasiswa.entity.User;
import id.ac.tazkia.smilemahasiswa.entity.surat.SuratPenelitian;
import id.ac.tazkia.smilemahasiswa.service.CurrentUserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDate;
import java.util.List;

@Controller
@Slf4j
public class PenelitianController {

    @Autowired
    private SuratPenelitianDao suratPenelitianDao;

    @Autowired
    private MahasiswaDao mahasiswaDao;

    @Autowired
    private KrsDetailDao krsDetailDao;

    @Autowired
    private CurrentUserService currentUserService;

    @GetMapping("/surat/penelitian/form")
    public String formSuratPenelitian(Model model, Authentication authentication) {
        Mahasiswa mahasiswa = getCurrentMahasiswa(authentication);

        // Always create new form for students
        SuratPenelitian suratPenelitian = new SuratPenelitian();
        suratPenelitian.setTanggalDibuat(LocalDate.now());
        suratPenelitian.setStatus(StatusRecord.AKTIF);
        suratPenelitian.setStatusApproval(StatusRecord.WAITING);
        suratPenelitian.setMahasiswa(mahasiswa);

        suratPenelitian.setUserApproval(mahasiswa.getIdProdi().getDosen());

        // Get list of surat penelitian for this mahasiswa
        List<SuratPenelitian> listSurat = suratPenelitianDao.findByMahasiswaNimOrderByTanggalDibuatDesc(mahasiswa.getNim());

        // Check if mahasiswa has taken skripsi course
        boolean sudahAmbilSkripsi = krsDetailDao.existsByMahasiswaAndMatakuliahSkripsi(mahasiswa);

        model.addAttribute("suratPenelitian", suratPenelitian);
        model.addAttribute("mahasiswa", mahasiswa);
        model.addAttribute("listSurat", listSurat);
        model.addAttribute("sudahAmbilSkripsi", sudahAmbilSkripsi);
        model.addAttribute("jenisSkripsi", getJenisSkripsi(mahasiswa));
        log.info("Skripsi taken: {}", sudahAmbilSkripsi);
        model.addAttribute("title", "Form Surat Penelitian");

        return "study/surat/penelitian-form";
    }

    @PostMapping("/surat/penelitian/form")
    public String saveSuratPenelitian(@ModelAttribute SuratPenelitian suratPenelitian,
                                     BindingResult errors,
                                     RedirectAttributes attributes,
                                     Authentication authentication) {

        if (errors.hasErrors()) {
            return "study/surat/penelitian-form";
        }

        // Set current mahasiswa if not already set
        if (suratPenelitian.getMahasiswa() == null) {
            Mahasiswa mahasiswa = getCurrentMahasiswa(authentication);
            suratPenelitian.setMahasiswa(mahasiswa);
            suratPenelitian.setUserApproval(mahasiswa.getIdProdi().getDosen());
        }

        // Check if mahasiswa has taken skripsi/thesis course
        boolean sudahAmbilSkripsi = krsDetailDao.existsByMahasiswaAndMatakuliahSkripsi(suratPenelitian.getMahasiswa());
        if (!sudahAmbilSkripsi) {
            String jenisSkripsi = getJenisSkripsi(suratPenelitian.getMahasiswa());
            attributes.addFlashAttribute("error", "Maaf, Anda belum dapat mengajukan surat penelitian. Anda harus sudah mengambil KRS " + jenisSkripsi + " terlebih dahulu.");
            return "redirect:/surat/penelitian/form";
        }

        if (suratPenelitian.getTanggalDibuat() == null) {
            suratPenelitian.setTanggalDibuat(LocalDate.now());
        }

        if (suratPenelitian.getStatus() == null) {
            suratPenelitian.setStatus(StatusRecord.AKTIF);
        }

        // Always set statusApproval to WAITING for new submissions
        if (suratPenelitian.getId() == null || suratPenelitian.getStatusApproval() == null) {
            suratPenelitian.setStatusApproval(StatusRecord.WAITING);
        }

        suratPenelitianDao.save(suratPenelitian);
        attributes.addFlashAttribute("success", "Data berhasil disimpan");

        return "redirect:/surat/penelitian/form";
    }

    private Mahasiswa getCurrentMahasiswa(Authentication authentication) {
        // Get current user using CurrentUserService
        User user = currentUserService.currentUser(authentication);
        if (user == null) {
            throw new RuntimeException("Current user not found");
        }

        // Get mahasiswa associated with this user
        Mahasiswa mahasiswa = mahasiswaDao.findByUser(user);
        if (mahasiswa == null) {
            throw new RuntimeException("Mahasiswa not found for current user");
        }

        return mahasiswa;
    }

    /**
     * Mendapatkan jenis skripsi berdasarkan jenjang mahasiswa (S1 = Skripsi, S2 = Thesis)
     */
    private String getJenisSkripsi(Mahasiswa mahasiswa) {
        if (mahasiswa != null && mahasiswa.getIdProdi() != null &&
                mahasiswa.getIdProdi().getIdJenjang() != null) {
            String kodeJenjang = mahasiswa.getIdProdi().getIdJenjang().getKodeJenjang();
            if (kodeJenjang != null && kodeJenjang.equalsIgnoreCase("S2")) {
                return "Thesis";
            }
        }
        return "Skripsi";
    }
}
