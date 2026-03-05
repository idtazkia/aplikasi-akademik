package id.ac.tazkia.smilemahasiswa.controller.surat;

import id.ac.tazkia.smilemahasiswa.dao.KaryawanDao;
import id.ac.tazkia.smilemahasiswa.dao.surat.SuratKeteranganMahasiswaDao;
import id.ac.tazkia.smilemahasiswa.entity.Karyawan;
import id.ac.tazkia.smilemahasiswa.entity.StatusRecord;
import id.ac.tazkia.smilemahasiswa.entity.surat.SuratKeteranganMahasiswa;
import id.ac.tazkia.smilemahasiswa.service.CurrentUserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDate;

@Controller
@RequestMapping("/surat/approval")
@Slf4j
public class ApprovalSuratController {

    @Autowired
    private SuratKeteranganMahasiswaDao suratKeteranganMahasiswaDao;

    @Autowired
    private KaryawanDao karyawanDao;

    @Autowired
    private CurrentUserService currentUserService;

    // Halaman list permohonan untuk approver
    @GetMapping("/list")
    public String listPermohonan(@RequestParam(required = false) String status,
                                    Authentication authentication,
                                    Model model,
                                    @PageableDefault(size = 10) Pageable pageable) {
        try {
            // Get current user (karyawan)
            Karyawan currentKaryawan = getCurrentKaryawan(authentication);

            // Filter berdasarkan status approval jika ada parameter
            Page<SuratKeteranganMahasiswa> data;
            if (status != null && !status.isEmpty()) {
                StatusRecord statusRecord = StatusRecord.valueOf(status);
                data = suratKeteranganMahasiswaDao.findByUserApprovalAndStatusApprovalOrderByTanggalDibuatDesc(
                    currentKaryawan, statusRecord, pageable);
            } else {
                data = suratKeteranganMahasiswaDao.findByUserApprovalOrderByTanggalDibuatDesc(
                    currentKaryawan, pageable);
            }

            model.addAttribute("data", data);
            model.addAttribute("title", "Approval Surat Keterangan Mahasiswa");
            model.addAttribute("status", status);

            log.info("Displaying approval list for karyawan: {} with status filter: {}",
                currentKaryawan.getNamaKaryawan(), status);
            return "surat/approval/list";
        } catch (Exception e) {
            log.error("Error displaying approval list", e);
            return "redirect:/";
        }
    }

    // Approve permohonan
    @PostMapping("/approve/{id}")
    public String approvePermohonan(@PathVariable String id,
                                   Authentication authentication,
                                   RedirectAttributes attributes) {
        try {
            Karyawan currentKaryawan = getCurrentKaryawan(authentication);
            SuratKeteranganMahasiswa surat = suratKeteranganMahasiswaDao.findById(id)
                    .orElseThrow(() -> new IllegalArgumentException("Surat tidak ditemukan"));

            // Cek apakah ini permohonan untuk approver ini
            if (!surat.getUserApproval().getId().equals(currentKaryawan.getId())) {
                attributes.addFlashAttribute("error", "Anda tidak berhak menyetujui permohonan ini");
                return "redirect:/surat/approval/list";
            }

            // Update status
            surat.setStatusApproval(StatusRecord.APPROVED);
            surat.setTanggalApproval(LocalDate.now());
            suratKeteranganMahasiswaDao.save(surat);

            log.info("Surat Keterangan Mahasiswa ID: {} di-approve oleh karyawan: {}",
                id, currentKaryawan.getNamaKaryawan());
            attributes.addFlashAttribute("success", "Permohonan berhasil disetujui");
            return "redirect:/surat/approval/list";
        } catch (Exception e) {
            log.error("Error approving surat", e);
            attributes.addFlashAttribute("error", "Gagal menyetujui permohonan: " + e.getMessage());
            return "redirect:/surat/approval/list";
        }
    }

    // Reject permohonan
    @PostMapping("/reject/{id}")
    public String rejectPermohonan(@PathVariable String id,
                                   @RequestParam(required = false) String alasan,
                                   Authentication authentication,
                                   RedirectAttributes attributes) {
        try {
            Karyawan currentKaryawan = getCurrentKaryawan(authentication);
            SuratKeteranganMahasiswa surat = suratKeteranganMahasiswaDao.findById(id)
                    .orElseThrow(() -> new IllegalArgumentException("Surat tidak ditemukan"));

            // Cek apakah ini permohonan untuk approver ini
            if (!surat.getUserApproval().getId().equals(currentKaryawan.getId())) {
                attributes.addFlashAttribute("error", "Anda tidak berhak menolak permohonan ini");
                return "redirect:/surat/approval/list";
            }

            // Update status
            surat.setStatusApproval(StatusRecord.HAPUS);
            suratKeteranganMahasiswaDao.save(surat);

            log.info("Surat Keterangan Mahasiswa ID: {} di-reject oleh karyawan: {} dengan alasan: {}",
                id, currentKaryawan.getNamaKaryawan(), alasan);
            attributes.addFlashAttribute("success", "Permohonan berhasil ditolak");
            return "redirect:/surat/approval/list";
        } catch (Exception e) {
            log.error("Error rejecting surat", e);
            attributes.addFlashAttribute("error", "Gagal menolak permohonan: " + e.getMessage());
            return "redirect:/surat/approval/list";
        }
    }

    // Lihat detail permohonan
    @GetMapping("/detail/{id}")
    public String detailPermohonan(@PathVariable String id,
                                    Model model) {
        try {
            SuratKeteranganMahasiswa surat = suratKeteranganMahasiswaDao.findById(id)
                    .orElseThrow(() -> new IllegalArgumentException("Surat tidak ditemukan"));

            model.addAttribute("surat", surat);
            model.addAttribute("title", "Detail Permohonan Surat");
            return "surat/approval/detail";
        } catch (Exception e) {
            log.error("Error displaying surat detail", e);
            return "redirect:/surat/approval/list";
        }
    }

    // Helper method untuk get current karyawan
    private Karyawan getCurrentKaryawan(Authentication authentication) {
        var user = currentUserService.currentUser(authentication);
        if (user == null) {
            throw new RuntimeException("Current user not found");
        }

        Karyawan karyawan = karyawanDao.findByIdUser(user);
        if (karyawan == null) {
            throw new RuntimeException("Karyawan not found for current user");
        }

        return karyawan;
    }
}
