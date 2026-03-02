package id.ac.tazkia.smilemahasiswa.controller.surat.mahasiswa;

import id.ac.tazkia.smilemahasiswa.dao.MahasiswaDao;
import id.ac.tazkia.smilemahasiswa.dao.surat.SuratKeteranganMahasiswaDao;
import id.ac.tazkia.smilemahasiswa.entity.Mahasiswa;
import id.ac.tazkia.smilemahasiswa.entity.StatusRecord;
import id.ac.tazkia.smilemahasiswa.entity.User;
import id.ac.tazkia.smilemahasiswa.entity.surat.SuratKeteranganMahasiswa;
import id.ac.tazkia.smilemahasiswa.service.CurrentUserService;
import id.ac.tazkia.smilemahasiswa.service.surat.SuratKeteranganMahasiswaService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.ByteArrayOutputStream;
import java.time.LocalDate;
import java.util.List;

@Controller
@Slf4j
public class KeteranganMahasiswaController {

    @Autowired
    private SuratKeteranganMahasiswaDao suratKeteranganMahasiswaDao;

    @Autowired
    private MahasiswaDao mahasiswaDao;

    @Autowired
    private CurrentUserService currentUserService;

    @Autowired
    private SuratKeteranganMahasiswaService suratKeteranganMahasiswaService;

    @GetMapping("/surat/keterangan/form")
    public String formSuratKeterangan(Model model, Authentication authentication) {
        Mahasiswa mahasiswa = getCurrentMahasiswa(authentication);

        // Get list of surat keterangan for this mahasiswa
        List<SuratKeteranganMahasiswa> listSurat = suratKeteranganMahasiswaDao
                .findByMahasiswaNimOrderByTanggalDibuatDesc(mahasiswa.getNim());

        model.addAttribute("mahasiswa", mahasiswa);
        model.addAttribute("listSurat", listSurat);
        model.addAttribute("title", "Surat Keterangan Mahasiswa");

        return "study/surat/keterangan-form";
    }

    @PostMapping("/surat/keterangan/minta")
    public String mintaSuratKeterangan(Model model,
                                       @RequestParam String keperluan,
                                       Authentication authentication,
                                       RedirectAttributes attributes) {
        try {
            Mahasiswa mahasiswa = getCurrentMahasiswa(authentication);

            // Create new surat keterangan - auto approved for direct download
            SuratKeteranganMahasiswa surat = new SuratKeteranganMahasiswa();
            surat.setMahasiswa(mahasiswa);
            surat.setTanggalDibuat(LocalDate.now());
            surat.setStatus(StatusRecord.AKTIF);
            surat.setStatusApproval(StatusRecord.APPROVED);
            surat.setTanggalApproval(LocalDate.now());
            surat.setKeperluan(keperluan);
            surat.setUserApproval(null); // No approver needed anymore

            suratKeteranganMahasiswaDao.save(surat);

            log.info("Surat Keterangan Mahasiswa berhasil dibuat untuk NIM: {} dengan keperluan: {}", mahasiswa.getNim(), keperluan);
            attributes.addFlashAttribute("success", "Surat berhasil dibuat dan siap diunduh.");
        } catch (Exception e) {
            log.error("Error creating Surat Keterangan Mahasiswa", e);
            attributes.addFlashAttribute("error", "Gagal mengajukan surat: " + e.getMessage());
        }

        return "redirect:/surat/keterangan/form";
    }

    @GetMapping("/surat/keterangan/download/{id}")
    @ResponseBody
    public ResponseEntity<byte[]> downloadSuratKeterangan(@PathVariable String id) {
        try {
            SuratKeteranganMahasiswa surat = suratKeteranganMahasiswaDao.findById(id)
                    .orElseThrow(() -> new IllegalArgumentException("Surat tidak ditemukan"));

            // Removed approval check - students can download immediately
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            suratKeteranganMahasiswaService.cetakSuratKeteranganMahasiswa(id, outputStream);

            String fileName = "Surat_Keterangan_Mahasiswa_" +
                    surat.getMahasiswa().getNama().replace(" ", "_") + ".pdf";

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_PDF);
            headers.setContentDispositionFormData("inline", fileName);
            headers.setCacheControl("must-revalidate, post-check=0, pre-check=0");

            return new ResponseEntity<>(outputStream.toByteArray(), headers, HttpStatus.OK);
        } catch (Exception e) {
            log.error("Error generating PDF for ID: {}", id, e);
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
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
