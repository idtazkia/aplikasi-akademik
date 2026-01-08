package id.ac.tazkia.smilemahasiswa.controller.surat;

import id.ac.tazkia.smilemahasiswa.dao.DosenDao;
import id.ac.tazkia.smilemahasiswa.dao.KaryawanDao;
import id.ac.tazkia.smilemahasiswa.dao.surat.SuratMagangDao;
import id.ac.tazkia.smilemahasiswa.entity.Dosen;
import id.ac.tazkia.smilemahasiswa.entity.Karyawan;
import id.ac.tazkia.smilemahasiswa.entity.Prodi;
import id.ac.tazkia.smilemahasiswa.entity.StatusRecord;
import id.ac.tazkia.smilemahasiswa.entity.User;
import id.ac.tazkia.smilemahasiswa.entity.surat.SuratMagang;
import id.ac.tazkia.smilemahasiswa.service.CurrentUserService;
import id.ac.tazkia.smilemahasiswa.service.surat.SuratMagangService;
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
import java.time.LocalDateTime;

@Controller
@RequestMapping("/surat")
@Slf4j
public class SuratMagangController {

    @Autowired
    private SuratMagangDao suratMagangDao;

    @Autowired
    private SuratMagangService suratMagangService;

    @Autowired
    private CurrentUserService currentUserService;

    @Autowired
    private KaryawanDao karyawanDao;

    @Autowired
    private DosenDao dosenDao;

    // List with filter
    @GetMapping("/suratmagang/list")
    public String listSuratMagang(@RequestParam(required = false) String nim,
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
            log.info("User {} accessing surat magang list with role: {}", user.getUsername(), roleName);
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
                        model.addAttribute("data", suratMagangDao.findByMahasiswaIdProdiAndNimContainingIgnoreCaseAndStatusApproval(prodi, nim, statusApproval, pageable));
                    } else if (StringUtils.hasText(nim)) {
                        // Filter by NIM only
                        model.addAttribute("data", suratMagangDao.findByMahasiswaIdProdiAndNimContainingIgnoreCase(prodi, nim, pageable));
                    } else if (statusApproval != null) {
                        // Filter by Status only
                        model.addAttribute("data", suratMagangDao.findByMahasiswaIdProdiAndStatusApproval(prodi, statusApproval, pageable));
                    } else {
                        // No filter
                        model.addAttribute("data", suratMagangDao.findByMahasiswaIdProdi(prodi, pageable));
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
                    model.addAttribute("data", suratMagangDao.findByNimContainingIgnoreCaseAndStatusApproval(nim, statusApproval, pageable));
                } else if (StringUtils.hasText(nim)) {
                    // Filter by NIM only
                    model.addAttribute("data", suratMagangDao.findByNimContainingIgnoreCase(nim, pageable));
                } else if (statusApproval != null) {
                    // Filter by Status only
                    model.addAttribute("data", suratMagangDao.findByStatusApproval(statusApproval, pageable));
                } else {
                    // No filter
                    model.addAttribute("data", suratMagangDao.findAll(pageable));
                }
            } else {
                // For other roles, show empty
                model.addAttribute("data", org.springframework.data.domain.Page.empty(pageable));
            }
        } else {
            // If no role, show empty
            model.addAttribute("data", org.springframework.data.domain.Page.empty(pageable));
        }

        model.addAttribute("title", "Daftar Surat Magang");
        model.addAttribute("nim", nim);
        model.addAttribute("status", status);

        return "surat/suratmagang/list";
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

    // Form untuk create/edit
    @GetMapping("/suratmagang/form")
    public String formSuratMagang(@RequestParam(required = false) String id, Model model) {
        SuratMagang suratMagang;

        if (StringUtils.hasText(id)) {
            suratMagang = suratMagangDao.findById(id).orElse(new SuratMagang());
        } else {
            suratMagang = new SuratMagang();
            suratMagang.setTanggalDibuat(LocalDateTime.now());
            suratMagang.setStatus(StatusRecord.AKTIF);
        }

        model.addAttribute("suratMagang", suratMagang);
        model.addAttribute("title", suratMagang.getId() != null ? "Edit Surat Magang" : "Tambah Surat Magang");

        // TODO: Get actual mahasiswa list
        // model.addAttribute("listMahasiswa", mahasiswaDao.findByStatus(StatusRecord.AKTIF));

        return "surat/suratmagang/form";
    }

    // Save
    @PostMapping("/suratmagang/form")
    public String saveSuratMagang(@ModelAttribute SuratMagang suratMagang,
                                 BindingResult errors,
                                 RedirectAttributes attributes) {

        if (errors.hasErrors()) {
            return "surat/suratmagang/form";
        }

        if (suratMagang.getTanggalDibuat() == null) {
            suratMagang.setTanggalDibuat(LocalDateTime.now());
        }

        suratMagangDao.save(suratMagang);
        attributes.addFlashAttribute("success", "Data berhasil disimpan");

        return "redirect:/surat/suratmagang/list";
    }

    // Delete
    @PostMapping("/suratmagang/delete/{id}")
    public String deleteSuratMagang(@PathVariable String id, RedirectAttributes attributes) {
        suratMagangDao.findById(id).ifPresent(surat -> {
            surat.setStatus(StatusRecord.HAPUS);
            suratMagangDao.save(surat);
        });

        attributes.addFlashAttribute("success", "Data berhasil dihapus");
        return "redirect:/surat/suratmagang/list";
    }

    // Download
    @GetMapping("/suratmagang/download/{id}")
    @ResponseBody
    public ResponseEntity<byte[]> downloadSuratMagang(@PathVariable String id) {
        try {
            SuratMagang surat = suratMagangDao.findById(id)
                    .orElseThrow(() -> new IllegalArgumentException("Surat tidak ditemukan"));

            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            suratMagangService.cetakSuratMagang(id, outputStream);

            String fileName = "Surat_Magang_" + surat.getMahasiswa().getNama().replace(" ", "_") + ".pdf";

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

    // View Detail
    @GetMapping("/suratmagang/view/{id}")
    public String viewSuratMagang(@PathVariable String id, Model model) {
        SuratMagang surat = suratMagangDao.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Surat tidak ditemukan"));

        model.addAttribute("surat", surat);
        model.addAttribute("title", "Detail Surat Magang");

        return "surat/suratmagang/view";
    }

    // Approve Surat Magang (KPS only)
    @PostMapping("/suratmagang/approve/{id}")
    public String approveSuratMagang(@PathVariable String id,
                                     Authentication authentication,
                                     RedirectAttributes attributes) {
        User user = currentUserService.currentUser(authentication);

        log.info("User {} attempting to approve surat magang with ID: {}",
            user != null ? user.getUsername() : "NULL", id);

        suratMagangDao.findById(id).ifPresent(surat -> {
            log.info("Approving surat magang ID {} for mahasiswa: {}",
                id, surat.getMahasiswa() != null ? surat.getMahasiswa().getNama() : "NULL");

            surat.setStatusApproval(StatusRecord.APPROVED);
            surat.setTanggalApproval(LocalDateTime.now());

            // Set user approval dari user yang sedang login
            if (user != null) {
                Karyawan karyawan = karyawanDao.findByIdUser(user);
                if (karyawan != null) {
                    Dosen dosen = dosenDao.findByKaryawan(karyawan);
                    surat.setUserApproval(dosen);
                    log.info("Approval by: {} (Dosen: {})", user.getUsername(), dosen.getKaryawan().getNamaKaryawan());
                }
            }

            suratMagangDao.save(surat);
            log.info("Surat magang ID {} successfully approved", id);
        });

        attributes.addFlashAttribute("success", "Surat berhasil diapprove");
        return "redirect:/surat/suratmagang/list";
    }

    // Reject Surat Magang (KPS only)
    @PostMapping("/suratmagang/reject/{id}")
    public String rejectSuratMagang(@PathVariable String id,
                                    Authentication authentication,
                                    RedirectAttributes attributes) {
        User user = currentUserService.currentUser(authentication);

        log.info("User {} attempting to reject surat magang with ID: {}",
            user != null ? user.getUsername() : "NULL", id);

        suratMagangDao.findById(id).ifPresent(surat -> {
            log.info("Rejecting surat magang ID {} for mahasiswa: {}",
                id, surat.getMahasiswa() != null ? surat.getMahasiswa().getNama() : "NULL");

            surat.setStatusApproval(StatusRecord.REJECTED);
            surat.setTanggalApproval(LocalDateTime.now());

            // Set user approval dari user yang sedang login
            if (user != null) {
                Karyawan karyawan = karyawanDao.findByIdUser(user);
                if (karyawan != null) {
                    Dosen dosen = dosenDao.findByKaryawan(karyawan);
                    surat.setUserApproval(dosen);
                    log.info("Rejection by: {} (Dosen: {})", user.getUsername(), dosen.getKaryawan().getNamaKaryawan());
                }
            }

            suratMagangDao.save(surat);
            log.info("Surat magang ID {} successfully rejected", id);
        });

        attributes.addFlashAttribute("success", "Surat berhasil direject");
        return "redirect:/surat/suratmagang/list";
    }
}
