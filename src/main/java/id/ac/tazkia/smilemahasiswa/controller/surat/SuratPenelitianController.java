package id.ac.tazkia.smilemahasiswa.controller.surat;

import id.ac.tazkia.smilemahasiswa.dao.DosenDao;
import id.ac.tazkia.smilemahasiswa.dao.KaryawanDao;
import id.ac.tazkia.smilemahasiswa.dao.surat.SuratPenelitianDao;
import id.ac.tazkia.smilemahasiswa.entity.Dosen;
import id.ac.tazkia.smilemahasiswa.entity.Karyawan;
import id.ac.tazkia.smilemahasiswa.entity.Prodi;
import id.ac.tazkia.smilemahasiswa.entity.StatusRecord;
import id.ac.tazkia.smilemahasiswa.entity.User;
import id.ac.tazkia.smilemahasiswa.entity.surat.SuratPenelitian;
import id.ac.tazkia.smilemahasiswa.service.CurrentUserService;
import id.ac.tazkia.smilemahasiswa.service.surat.SuratPenelitianService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.StringUtils;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.ByteArrayOutputStream;
import java.time.LocalDate;

@Controller
@RequestMapping("/surat")
@Slf4j
public class SuratPenelitianController {

    @Autowired
    private SuratPenelitianDao suratPenelitianDao;

    @Autowired
    private SuratPenelitianService suratPenelitianService;

    @Autowired
    private CurrentUserService currentUserService;

    @Autowired
    private KaryawanDao karyawanDao;

    @Autowired
    private DosenDao dosenDao;

    // List with filter
    @GetMapping("/suratpenelitian/list")
    public String listSuratPenelitian(@RequestParam(required = false) String nim,
                                      @RequestParam(required = false) String status,
                                      Model model,
                                      @PageableDefault(size = 10) Pageable pageable,
                                      Authentication authentication) {

        // Get current user
        User user = currentUserService.currentUser(authentication);

        // Log user info untuk debug
        log.debug("Current User: {}", user != null ? user.getUsername() : "NULL");
        if (user != null) {
            log.debug("User ID: {}", user.getId());
            log.debug("User Role: {}", user.getRole());
            if (user.getRole() != null) {
                log.debug("Role Name: {}", user.getRole().getName());
                log.debug("Role ID: {}", user.getRole().getId());
            }
        }

        // Check user role and filter data accordingly
        if (user != null && user.getRole() != null) {
            String roleName = user.getRole().getName();
            log.info("User {} accessing surat penelitian list with role: {}", user.getUsername(), roleName);
            log.debug("Filter params - nim: {}, status: {}", nim, status);

            if ("kps".equalsIgnoreCase(roleName)) {
                // For KPS, filter by prodi
                Prodi prodi = getProdiFromUser(user);
                if (prodi != null) {
                    // Convert status string to StatusRecord enum
                    StatusRecord statusApproval = null;
                    if (StringUtils.hasText(status)) {
                        try {
                            statusApproval = StatusRecord.valueOf(status);
                        } catch (IllegalArgumentException e) {
                            log.warn("Invalid status value: {}", status);
                        }
                    }

                    // Apply filters
                    if (StringUtils.hasText(nim) && statusApproval != null) {
                        // Filter by NIM and Status
                        model.addAttribute("data", suratPenelitianDao.findByMahasiswaIdProdiAndNimContainingIgnoreCaseAndStatusApproval(prodi, nim, statusApproval, pageable));
                    } else if (StringUtils.hasText(nim)) {
                        // Filter by NIM only
                        model.addAttribute("data", suratPenelitianDao.findByMahasiswaIdProdiAndNimContainingIgnoreCase(prodi, nim, pageable));
                    } else if (statusApproval != null) {
                        // Filter by Status only
                        model.addAttribute("data", suratPenelitianDao.findByMahasiswaIdProdiAndStatusApproval(prodi, statusApproval, pageable));
                    } else {
                        // No filter
                        model.addAttribute("data", suratPenelitianDao.findByMahasiswaIdProdi(prodi, pageable));
                    }
                } else {
                    // If KPS has no prodi, show empty
                    model.addAttribute("data", org.springframework.data.domain.Page.empty(pageable));
                }
            } else if ("admin".equalsIgnoreCase(roleName) || "superuser".equalsIgnoreCase(roleName)) {
                // For admin and superuser, show all data
                // Convert status string to StatusRecord enum
                StatusRecord statusApproval = null;
                if (StringUtils.hasText(status)) {
                    try {
                        statusApproval = StatusRecord.valueOf(status);
                    } catch (IllegalArgumentException e) {
                        log.warn("Invalid status value: {}", status);
                    }
                }

                // Apply filters
                if (StringUtils.hasText(nim) && statusApproval != null) {
                    // Filter by NIM and Status
                    model.addAttribute("data", suratPenelitianDao.findByNimContainingIgnoreCaseAndStatusApproval(nim, statusApproval, pageable));
                } else if (StringUtils.hasText(nim)) {
                    // Filter by NIM only
                    model.addAttribute("data", suratPenelitianDao.findByNimContainingIgnoreCase(nim, pageable));
                } else if (statusApproval != null) {
                    // Filter by Status only
                    model.addAttribute("data", suratPenelitianDao.findByStatusApproval(statusApproval, pageable));
                } else {
                    // No filter
                    model.addAttribute("data", suratPenelitianDao.findAll(pageable));
                }
            } else {
                // For other roles, show empty
                model.addAttribute("data", org.springframework.data.domain.Page.empty(pageable));
            }
        } else {
            // If no role, show empty
            model.addAttribute("data", org.springframework.data.domain.Page.empty(pageable));
        }

        model.addAttribute("title", "Daftar Surat Penelitian");
        model.addAttribute("nim", nim);
        model.addAttribute("status", status);

        return "surat/suratpenelitian/list";
    }

    // Helper method to get prodi from user (for KPS)
    private Prodi getProdiFromUser(User user) {
        try {
            // User -> Karyawan -> Dosen -> Prodi
            Karyawan karyawan = karyawanDao.findByIdUser(user);
            if (karyawan != null) {
                Dosen dosen = dosenDao.findByKaryawan(karyawan);
                if (dosen != null && dosen.getProdi() != null) {
                    return dosen.getProdi();
                }
            }
        } catch (Exception e) {
            log.error("Error getting prodi from user: {}", user.getUsername(), e);
        }
        return null;
    }

    // Form untuk create/edit (for admin/kps)
    @GetMapping("/suratpenelitian/form")
    public String formSuratPenelitian(@RequestParam(required = false) String id, Model model) {
        SuratPenelitian suratPenelitian;

        if (StringUtils.hasText(id)) {
            suratPenelitian = suratPenelitianDao.findById(id).orElse(new SuratPenelitian());
        } else {
            suratPenelitian = new SuratPenelitian();
            suratPenelitian.setTanggalDibuat(LocalDate.now());
            suratPenelitian.setStatus(StatusRecord.AKTIF);
        }

        model.addAttribute("suratPenelitian", suratPenelitian);
        model.addAttribute("title", suratPenelitian.getId() != null ? "Edit Surat Penelitian" : "Tambah Surat Penelitian");

        return "surat/suratpenelitian/form";
    }

    // Save (for admin/kps)
    @PostMapping("/suratpenelitian/form")
    public String saveSuratPenelitian(@ModelAttribute SuratPenelitian suratPenelitian,
                                     BindingResult errors,
                                     RedirectAttributes attributes) {

        if (errors.hasErrors()) {
            return "surat/suratpenelitian/form";
        }

        if (suratPenelitian.getTanggalDibuat() == null) {
            suratPenelitian.setTanggalDibuat(LocalDate.now());
        }

        suratPenelitianDao.save(suratPenelitian);
        attributes.addFlashAttribute("success", "Data berhasil disimpan");

        return "redirect:/surat/suratpenelitian/list";
    }

    // Delete
    @PostMapping("/suratpenelitian/delete/{id}")
    public String deleteSuratPenelitian(@PathVariable String id, RedirectAttributes attributes) {
        suratPenelitianDao.findById(id).ifPresent(surat -> {
            surat.setStatus(StatusRecord.HAPUS);
            suratPenelitianDao.save(surat);
        });

        attributes.addFlashAttribute("success", "Data berhasil dihapus");
        return "redirect:/surat/suratpenelitian/list";
    }

    // Approve Surat Penelitian (KPS only)
    @PostMapping("/suratpenelitian/approve/{id}")
    public String approveSuratPenelitian(@PathVariable String id,
                                        Authentication authentication,
                                        RedirectAttributes attributes) {
        User user = currentUserService.currentUser(authentication);

        log.info("User {} attempting to approve surat penelitian with ID: {}",
            user != null ? user.getUsername() : "NULL", id);

        suratPenelitianDao.findById(id).ifPresent(surat -> {
            log.info("Approving surat penelitian ID {} for mahasiswa: {}",
                id, surat.getMahasiswa() != null ? surat.getMahasiswa().getNama() : "NULL");

            surat.setStatusApproval(StatusRecord.APPROVED);
            surat.setTanggalApproval(LocalDate.now());

            // Set user approval dari user yang sedang login
            if (user != null) {
                Karyawan karyawan = karyawanDao.findByIdUser(user);
                if (karyawan != null) {
                    Dosen dosen = dosenDao.findByKaryawan(karyawan);
                    surat.setUserApproval(dosen);
                    log.info("Approval by: {} (Dosen: {})", user.getUsername(), dosen.getKaryawan().getNamaKaryawan());
                }
            }

            suratPenelitianDao.save(surat);
            log.info("Surat penelitian ID {} successfully approved", id);
        });

        attributes.addFlashAttribute("success", "Surat berhasil diapprove");
        return "redirect:/surat/suratpenelitian/list";
    }

    // Reject Surat Penelitian (KPS only)
    @PostMapping("/suratpenelitian/reject/{id}")
    public String rejectSuratPenelitian(@PathVariable String id,
                                       Authentication authentication,
                                       RedirectAttributes attributes) {
        User user = currentUserService.currentUser(authentication);

        log.info("User {} attempting to reject surat penelitian with ID: {}",
            user != null ? user.getUsername() : "NULL", id);

        suratPenelitianDao.findById(id).ifPresent(surat -> {
            log.info("Rejecting surat penelitian ID {} for mahasiswa: {}",
                id, surat.getMahasiswa() != null ? surat.getMahasiswa().getNama() : "NULL");

            surat.setStatusApproval(StatusRecord.REJECTED);
            surat.setTanggalApproval(LocalDate.now());

            // Set user approval dari user yang sedang login
            if (user != null) {
                Karyawan karyawan = karyawanDao.findByIdUser(user);
                if (karyawan != null) {
                    Dosen dosen = dosenDao.findByKaryawan(karyawan);
                    surat.setUserApproval(dosen);
                    log.info("Rejection by: {} (Dosen: {})", user.getUsername(), dosen.getKaryawan().getNamaKaryawan());
                }
            }

            suratPenelitianDao.save(surat);
            log.info("Surat penelitian ID {} successfully rejected", id);
        });

        attributes.addFlashAttribute("success", "Surat berhasil direject");
        return "redirect:/surat/suratpenelitian/list";
    }

    // Download Surat Penelitian
    @GetMapping("/suratpenelitian/download/{id}")
    @ResponseBody
    public ResponseEntity<byte[]> downloadSuratPenelitian(@PathVariable String id) {
        try {
            SuratPenelitian surat = suratPenelitianDao.findById(id)
                    .orElseThrow(() -> new IllegalArgumentException("Surat tidak ditemukan"));

            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            suratPenelitianService.cetakSuratPenelitian(id, outputStream);

            String fileName = "Surat_Penelitian_" + surat.getMahasiswa().getNama().replace(" ", "_") + ".pdf";

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
}
