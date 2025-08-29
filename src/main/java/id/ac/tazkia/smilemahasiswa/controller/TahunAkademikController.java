package id.ac.tazkia.smilemahasiswa.controller;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.StringUtils;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import id.ac.tazkia.smilemahasiswa.dao.TahunAkademikDao;
import id.ac.tazkia.smilemahasiswa.dao.TahunProdiDao;
import id.ac.tazkia.smilemahasiswa.dto.tahunakademik.CreateTahunAkademikDto;
import id.ac.tazkia.smilemahasiswa.entity.Prodi;
import id.ac.tazkia.smilemahasiswa.entity.StatusRecord;
import id.ac.tazkia.smilemahasiswa.entity.TahunAkademik;
import id.ac.tazkia.smilemahasiswa.entity.TahunAkademikProdi;
import id.ac.tazkia.smilemahasiswa.service.TahunAkademikService;
import lombok.extern.slf4j.Slf4j;

@Controller
@Slf4j
public class TahunAkademikController {
    @Autowired
    private TahunAkademikDao tahunAkademikDao;
    @Autowired
    private TahunProdiDao tahunProdiDao;
    @Autowired
    private TahunAkademikService tahunAkademikService;

    @GetMapping("/academic/year/list")
    public void academicList(Model model, @PageableDefault(size = 10) Pageable page, String search) {
        List<StatusRecord> statusRecords = new ArrayList<>();
        statusRecords.add(StatusRecord.AKTIF);
        statusRecords.add(StatusRecord.NONAKTIF);
        if (StringUtils.hasText(search)) {
            model.addAttribute("search", search);
            model.addAttribute("list",
                    tahunAkademikDao.findByStatusInAndNamaTahunAkademikContainingIgnoreCaseOrderByKodeTahunAkademikDesc(
                            statusRecords, search, page));
        } else {
            model.addAttribute("list", tahunAkademikDao.cariTahunAkademik(StatusRecord.HAPUS, page));

        }
    }

    @GetMapping("/academic/year/form")
    public void academicForm(Model model, @RequestParam(required = false) String id) {
        model.addAttribute("tahunAkademik", new TahunAkademik());

        if (isEditMode(id)) {
            tahunAkademikService.setupEditMode(id, model);
        } else {
            tahunAkademikService.setupCreateMode(model);
        }
    }

    private boolean isEditMode(String id) {
        return id != null && !id.isEmpty();
    }

    @PostMapping("/academic/year/form")
    public String prosesForm(@Valid @ModelAttribute("tahunAkademikDto") CreateTahunAkademikDto dto,
            BindingResult bindingResult,
            Model model,
            RedirectAttributes redirectAttributes) {

        if (bindingResult.hasErrors()) {
            log.warn("Validation errors: {}", bindingResult.getAllErrors());
            model.addAttribute("statusOptions", StatusRecord.values());
            generateSuggestedValues(model);
            return "academic/year/new-form";
        }

        try {
            TahunAkademik tahunAkademik;

            // Check if this is edit mode (id is present)
            if (dto.getId() != null && !dto.getId().isEmpty()) {
                // Edit mode - update existing tahun akademik without creating details
                tahunAkademik = tahunAkademikService.updateTahunAkademik(dto);

                redirectAttributes.addFlashAttribute("success",
                        String.format("Tahun Akademik '%s' berhasil diperbarui",
                                tahunAkademik.getNamaTahunAkademik()));
            } else {
                // Create mode - create new tahun akademik with details
                tahunAkademik = tahunAkademikService.createTahunAkademikWithDetails(dto);

                redirectAttributes.addFlashAttribute("success",
                        String.format(
                                "Tahun Akademik '%s' berhasil dibuat dengan detail untuk semua kombinasi prodi-program",
                                tahunAkademik.getNamaTahunAkademik()));
            }

            return "redirect:list";

        } catch (Exception e) {
            log.error("Error creating tahun akademik", e);
            redirectAttributes.addFlashAttribute("error",
                    "Gagal membuat tahun akademik: " + e.getMessage());
            return "redirect:/academic/year/form";
        }
    }

    @PostMapping("/academic/year/delete")
    public String deleteAcademic(@RequestParam TahunAkademik tahunAkademik) {
        tahunAkademikService.deleteTahunAkademik(tahunAkademik);
        return "redirect:list";
    }

    @PostMapping("/academic/year/active")
    public String academicActive(@RequestParam TahunAkademik tahunAkademik) {
        tahunAkademikService.activateTahunAkademik(tahunAkademik);
        return "redirect:list";
    }

    @GetMapping("/academic/year/prodi/list")
    public void yearProdiList(Model model, @RequestParam(required = false) TahunAkademik tahunAkademik,
            @RequestParam(required = false) Prodi prodi) {
        model.addAttribute("selectedTahun", tahunAkademik);
        model.addAttribute("selectedProdi", prodi);
        if (tahunAkademik != null) {
            model.addAttribute("search", tahunAkademik);
            model.addAttribute("prodiTahunAkademik", tahunProdiDao.tahunAkademikProdiGet(tahunAkademik, prodi));
        }
        model.addAttribute("listTahunAkademik",
                tahunAkademikDao.findByStatusNotInOrderByTahunDesc(Arrays.asList(StatusRecord.HAPUS)));
        model.addAttribute("listProdi", tahunProdiDao.listTahaunAkademikProdi());
    }

    private void generateSuggestedValues(Model model) {
        try {
            int currentYear = LocalDate.now().getYear();

            addSemesterSuggestion(model, currentYear, "1", "Ganjil", "suggestedKodeGanjil", "suggestedNamaGanjil");
            addSemesterSuggestion(model, currentYear, "2", "Genap", "suggestedKodeGenap", "suggestedNamaGenap");
            addSemesterSuggestion(model, currentYear, "3", "Pendek", "suggestedKodePendek", "suggestedNamaPendek");

        } catch (Exception e) {
            log.warn("Error generating suggested values", e);
        }
    }

    private void addSemesterSuggestion(Model model, int currentYear, String semesterCode,
            String semesterName, String kodeAttribute, String namaAttribute) {
        String kode = currentYear + semesterCode;
        String nama = String.format("%d/%d Semester %s", currentYear, currentYear + 1, semesterName);

        model.addAttribute(kodeAttribute, kode);
        model.addAttribute(namaAttribute, nama);
    }

    // Details for tahun akademik
    @GetMapping("/prodi/tahun")
    public String tahunAkademikProdi(Model model,
            @PageableDefault(size = 10) Pageable page) {

        model.addAttribute("listTahunAkademikProdi", tahunProdiDao
                .findByTahunAkademikKodeTahunAkademikContainingOrderByTahunAkademikKodeTahunAkademikDesc("2", page));

        return "prodi/tahun_akademik/list";

    }

    @PostMapping("/prodi/tahun/save")
    public String saveTahunAkademikProdi(@ModelAttribute TahunAkademikProdi tahunAkademikProdi,
            RedirectAttributes redirectAttributes) {

        tahunAkademikService.saveTahunAkademikProdi(tahunAkademikProdi);
        redirectAttributes.addFlashAttribute("success", "Save Data Berhasil");
        return "redirect:/academic/prodi/list";
    }

    @PostMapping("/prodi/tahun/active")
    public String activeTahunAkademikProdi(@RequestParam(required = false) String tahunAkademikProdi,
            RedirectAttributes redirectAttributes) {

        tahunAkademikService.activateTahunAkademikProdi(tahunAkademikProdi);
        redirectAttributes.addFlashAttribute("success", "Save Data Berhasil");
        return "redirect:../tahun";
    }

    @PostMapping("/prodi/tahun/deactive")
    public String deactiveTahunAkademikProdi(@RequestParam(required = false) String tahunAkademikProdi,
            RedirectAttributes redirectAttributes) {

        tahunAkademikService.deactivateTahunAkademikProdi(tahunAkademikProdi);
        redirectAttributes.addFlashAttribute("success", "Save Data Berhasil");
        return "redirect:../tahun";
    }
}
