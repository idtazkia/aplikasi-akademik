package id.ac.tazkia.smilemahasiswa.controller.surat;

import id.ac.tazkia.smilemahasiswa.dao.surat.SuratMagangDao;
import id.ac.tazkia.smilemahasiswa.entity.Mahasiswa;
import id.ac.tazkia.smilemahasiswa.entity.StatusRecord;
import id.ac.tazkia.smilemahasiswa.entity.surat.SuratMagang;
import id.ac.tazkia.smilemahasiswa.service.surat.SuratMagangService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
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
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.ByteArrayOutputStream;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/surat")
@Slf4j
public class SuratMagangController {

    @Autowired
    private SuratMagangDao suratMagangDao;

    @Autowired
    private SuratMagangService suratMagangService;

    // List with filter
    @GetMapping("/suratmagang/list")
    public String listSuratMagang(@RequestParam(required = false) String namaPerusahaan,
                                 @RequestParam(required = false) String search,
                                 Model model,
                                 @PageableDefault(size = 10) Pageable pageable) {

        if (StringUtils.hasText(namaPerusahaan) || StringUtils.hasText(search)) {
            // Apply filters
            if (StringUtils.hasText(namaPerusahaan)) {
                model.addAttribute("data", suratMagangDao.findByNamaPerusahaanContainingIgnoreCase(namaPerusahaan, pageable));
            } else {
                model.addAttribute("data", suratMagangDao.findAll(pageable));
            }
        } else {
            model.addAttribute("data", suratMagangDao.findAll(pageable));
        }

        model.addAttribute("title", "Daftar Surat Magang");
        model.addAttribute("namaPerusahaan", namaPerusahaan);
        model.addAttribute("search", search);

        return "surat/suratmagang/list";
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
}
