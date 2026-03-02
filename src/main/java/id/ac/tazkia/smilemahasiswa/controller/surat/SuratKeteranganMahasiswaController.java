package id.ac.tazkia.smilemahasiswa.controller.surat;

import id.ac.tazkia.smilemahasiswa.dao.surat.SuratKeteranganMahasiswaDao;
import id.ac.tazkia.smilemahasiswa.entity.User;
import id.ac.tazkia.smilemahasiswa.entity.surat.SuratKeteranganMahasiswa;
import id.ac.tazkia.smilemahasiswa.service.CurrentUserService;
import id.ac.tazkia.smilemahasiswa.service.surat.SuratKeteranganMahasiswaService;
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
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.io.ByteArrayOutputStream;

@Controller
@RequestMapping("/surat")
@Slf4j
public class SuratKeteranganMahasiswaController {

    @Autowired
    private SuratKeteranganMahasiswaDao suratKeteranganMahasiswaDao;

    @Autowired
    private SuratKeteranganMahasiswaService suratKeteranganMahasiswaService;

    @Autowired
    private CurrentUserService currentUserService;

    // List with filter
    @GetMapping("/suratketerangan/list")
    public String listSuratKeterangan(@RequestParam(required = false) String nim,
                                       Model model,
                                       @PageableDefault(size = 10) Pageable pageable,
                                       Authentication authentication) {

        // Get current user with null check
        User user = null;
        try {
            user = currentUserService.currentUser(authentication);
        } catch (Exception e) {
            log.error("Error getting current user", e);
            model.addAttribute("error", "Sesi Anda telah kadaluarsa. Silakan login kembali.");
            model.addAttribute("data", org.springframework.data.domain.Page.empty(pageable));
            return "surat/suratketerangan/list";
        }

        log.debug("Current User: {}", user != null ? user.getUsername() : "NULL");
        if (user != null && user.getId() != null) {
            log.debug("User ID: {}", user.getId());
            log.debug("User Role: {}", user.getRole());
            if (user.getRole() != null) {
                log.debug("Role Name: {}", user.getRole().getName());
                log.debug("Role ID: {}", user.getRole().getId());
            }
        }

        // Check user role and filter data accordingly
        if (user != null && user.getId() != null && user.getRole() != null) {
            String roleName = user.getRole().getName();
            log.info("User {} accessing surat keterangan list with role: {}", user.getUsername(), roleName);
            log.debug("Filter params - nim: {}", nim);

            if ("kps".equalsIgnoreCase(roleName) || "admin".equalsIgnoreCase(roleName) || "superuser".equalsIgnoreCase(roleName)) {
                // For KPS, admin, and superuser, show all data
                if (StringUtils.hasText(nim)) {
                    // Filter by NIM only
                    model.addAttribute("data", suratKeteranganMahasiswaDao.findByNimContainingIgnoreCase(nim, pageable));
                } else {
                    // No filter
                    model.addAttribute("data", suratKeteranganMahasiswaDao.findAll(pageable));
                }
            } else {
                // For other roles, show empty
                model.addAttribute("data", org.springframework.data.domain.Page.empty(pageable));
            }
        } else {
            // If no role or invalid user, show empty
            model.addAttribute("data", org.springframework.data.domain.Page.empty(pageable));
        }

        model.addAttribute("title", "Daftar Surat Keterangan Mahasiswa");
        model.addAttribute("nim", nim);

        return "surat/suratketerangan/list";
    }

    // Download Surat Keterangan
    @GetMapping("/suratketerangan/download/{id}")
    @ResponseBody
    public ResponseEntity<byte[]> downloadSuratKeterangan(@PathVariable String id) {
        try {
            SuratKeteranganMahasiswa surat = suratKeteranganMahasiswaDao.findById(id)
                    .orElseThrow(() -> new IllegalArgumentException("Surat tidak ditemukan"));

            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            suratKeteranganMahasiswaService.cetakSuratKeteranganMahasiswa(id, outputStream);

            String fileName = "Surat_Keterangan_Mahasiswa_" + surat.getMahasiswa().getNama().replace(" ", "_") + ".pdf";

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
