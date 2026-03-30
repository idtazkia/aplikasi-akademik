package id.ac.tazkia.smilemahasiswa.controller.surat.mahasiswa;

import id.ac.tazkia.smilemahasiswa.dao.KrsDetailDao;
import id.ac.tazkia.smilemahasiswa.dao.MahasiswaDao;
import id.ac.tazkia.smilemahasiswa.dao.surat.SuratMagangDao;
import id.ac.tazkia.smilemahasiswa.entity.Mahasiswa;
import id.ac.tazkia.smilemahasiswa.entity.StatusRecord;
import id.ac.tazkia.smilemahasiswa.entity.User;
import id.ac.tazkia.smilemahasiswa.entity.surat.SuratMagang;
import id.ac.tazkia.smilemahasiswa.entity.surat.HariKerja;
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
import java.time.LocalDateTime;
import java.util.List;

@Controller
@Slf4j
public class MagangController {

    @Autowired
    private SuratMagangDao suratMagangDao;

    @Autowired
    private MahasiswaDao mahasiswaDao;

    @Autowired
    private KrsDetailDao krsDetailDao;

    @Autowired
    private CurrentUserService currentUserService;

    @ModelAttribute("magangController")
    public MagangController magangController() {
        return this;
    }

    @GetMapping("/surat/form")
    public String formSuratMagang(Model model, Authentication authentication) {
        Mahasiswa mahasiswa = getCurrentMahasiswa(authentication);

        // Always create new form for students
        SuratMagang suratMagang = new SuratMagang();
        suratMagang.setTanggalDibuat(LocalDateTime.now());
        suratMagang.setStatus(StatusRecord.AKTIF);
        suratMagang.setStatusApproval(StatusRecord.WAITING);
        suratMagang.setMahasiswa(mahasiswa);

        suratMagang.setUserApproval(mahasiswa.getIdProdi().getDosen());

        // Get list of surat magang for this mahasiswa
        List<SuratMagang> listSurat = suratMagangDao.findByMahasiswaNimOrderByTanggalDibuatDesc(mahasiswa.getNim());

        // Get total SKS for this mahasiswa
        Long totalSks = krsDetailDao.totalSks(mahasiswa);
        boolean sksMemenuhi = totalSks != null && totalSks >= 100;

        model.addAttribute("suratMagang", suratMagang);
        model.addAttribute("mahasiswa", mahasiswa);
        model.addAttribute("listSurat", listSurat);
        model.addAttribute("totalSks", totalSks != null ? totalSks : 0);
        model.addAttribute("sksMemenuhi", sksMemenuhi);
        model.addAttribute("title", "Form Surat Magang");

        return "study/surat/magang-form";
    }

    // Method untuk menghitung jumlah hari kerja
    public Integer calculateHaiKerja(SuratMagang suratMagang) {
        if (suratMagang == null) return 0;

        LocalDate mulai = suratMagang.getTanggalMulaiMagang();
        LocalDate selesai = suratMagang.getTanggalSelesaiMagang();
        HariKerja hariKerja = suratMagang.getHariKerja();

        if (mulai == null || selesai == null || hariKerja == null) {
            // Jika hariKerja belum diisi, hitung semua hari
            if (mulai != null && selesai != null) {
                return (int) java.time.temporal.ChronoUnit.DAYS.between(mulai, selesai) + 1;
            }
            return 0;
        }

        int totalHaiKerja = 0;
        LocalDate current = mulai;

        while (!current.isAfter(selesai)) {
            int dayOfWeek = current.getDayOfWeek().getValue(); // 1=Monday, 7=Sunday

            if (hariKerja == HariKerja.SENIN_JUMAT) {
                // Monday to Friday (1-5)
                if (dayOfWeek >= 1 && dayOfWeek <= 5) {
                    totalHaiKerja++;
                }
            } else if (hariKerja == HariKerja.SENIN_SABTU) {
                // Monday to Saturday (1-6)
                if (dayOfWeek >= 1 && dayOfWeek <= 6) {
                    totalHaiKerja++;
                }
            }

            current = current.plusDays(1);
        }

        return totalHaiKerja;
    }

    @PostMapping("/surat/form")
    public String saveSuratMagang(@ModelAttribute SuratMagang suratMagang,
                                 BindingResult errors,
                                 RedirectAttributes attributes,
                                 Authentication authentication) {

        if (errors.hasErrors()) {
            return "study/surat/magang-form";
        }

        // Set current mahasiswa if not already set
        if (suratMagang.getMahasiswa() == null) {
            Mahasiswa mahasiswa = getCurrentMahasiswa(authentication);
            suratMagang.setMahasiswa(mahasiswa);
            suratMagang.setUserApproval(mahasiswa.getIdProdi().getDosen());
        }

        // Check if SKS meets minimum requirement
        Long totalSks = krsDetailDao.totalSks(suratMagang.getMahasiswa());
        if (totalSks == null || totalSks < 100) {
            attributes.addFlashAttribute("error", "Maaf, Anda belum dapat mengajukan surat magang. Total SKS Anda saat ini: " + (totalSks != null ? totalSks : 0) + ". Minimal SKS yang diperlukan: 100 SKS.");
            return "redirect:/surat/form";
        }

        if (suratMagang.getTanggalDibuat() == null) {
            suratMagang.setTanggalDibuat(LocalDateTime.now());
        }

        if (suratMagang.getStatus() == null) {
            suratMagang.setStatus(StatusRecord.AKTIF);
        }

        // Always set statusApproval to WAITING for new submissions
        if (suratMagang.getId() == null || suratMagang.getStatusApproval() == null) {
            suratMagang.setStatusApproval(StatusRecord.WAITING);
        }

        suratMagangDao.save(suratMagang);
        attributes.addFlashAttribute("success", "Data berhasil disimpan");

        return "redirect:/surat/form";
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
}
