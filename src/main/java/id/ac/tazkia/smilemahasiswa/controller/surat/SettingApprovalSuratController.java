package id.ac.tazkia.smilemahasiswa.controller.surat;

import id.ac.tazkia.smilemahasiswa.dao.KaryawanDao;
import id.ac.tazkia.smilemahasiswa.dao.surat.SettingApprovalSuratDao;
import id.ac.tazkia.smilemahasiswa.entity.Karyawan;
import id.ac.tazkia.smilemahasiswa.entity.StatusRecord;
import id.ac.tazkia.smilemahasiswa.entity.surat.SettingApprovalSurat;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDate;
import java.util.Optional;

@Controller
@RequestMapping("/surat/setting-approval")
@Slf4j
public class SettingApprovalSuratController {

    @Autowired
    private SettingApprovalSuratDao settingApprovalSuratDao;

    @Autowired
    private KaryawanDao karyawanDao;

    // List semua setting approval
    @GetMapping("/list")
    public String listSettingApproval(@RequestParam(required = false) String jenisSurat,
                                       Model model,
                                       @PageableDefault(size = 10) Pageable pageable) {
        if (jenisSurat != null && !jenisSurat.isEmpty()) {
            model.addAttribute("data", settingApprovalSuratDao.findByJenisSurat(jenisSurat, pageable));
        } else {
            model.addAttribute("data", settingApprovalSuratDao.findByStatusOrderByTanggalUpdateDesc(StatusRecord.AKTIF, pageable));
        }
        model.addAttribute("title", "Setting Approval Surat");
        model.addAttribute("jenisSurat", jenisSurat);
        return "surat/setting-approval/list";
    }

    // Form tambah setting approval baru
    @GetMapping("/form")
    public String formSettingApproval(Model model) {
        try {
            model.addAttribute("title", "Tambah Setting Approval");
            model.addAttribute("setting", new SettingApprovalSurat());

            // Ambil list karyawan aktif
            model.addAttribute("listKaryawan", karyawanDao.findByStatusOrderByNamaKaryawan(StatusRecord.AKTIF, null));

            log.info("Displaying new setting approval form");
            return "surat/setting-approval/form";
        } catch (Exception e) {
            log.error("Error displaying setting approval form", e);
            return "redirect:/surat/setting-approval/list";
        }
    }

    // Form edit setting approval
    @GetMapping("/form/{id}")
    public String editSettingApproval(@PathVariable String id, Model model) {
        try {
            Optional<SettingApprovalSurat> setting = settingApprovalSuratDao.findById(id);
            if (setting.isPresent()) {
                model.addAttribute("title", "Edit Setting Approval");
                model.addAttribute("setting", setting.get());

                // Ambil list karyawan aktif
                model.addAttribute("listKaryawan", karyawanDao.findByStatusOrderByNamaKaryawan(StatusRecord.AKTIF, null));

                log.info("Displaying edit setting approval form for ID: {}", id);
                return "surat/setting-approval/form";
            }
            log.warn("Setting approval not found with ID: {}", id);
            return "redirect:/surat/setting-approval/list";
        } catch (Exception e) {
            log.error("Error displaying edit setting approval form", e);
            return "redirect:/surat/setting-approval/list";
        }
    }

    // Simpan setting approval (create atau update)
    @PostMapping("/save")
    public String saveSettingApproval(@RequestParam(required = false) String id,
                                       @RequestParam String namaSetting,
                                       @RequestParam String jenisSurat,
                                       @RequestParam String idKaryawanApprover,
                                       @RequestParam(required = false) String action,
                                       Model model,
                                       RedirectAttributes attributes) {
        try {
            SettingApprovalSurat setting;
            Karyawan karyawanApprover = karyawanDao.findById(idKaryawanApprover).orElse(null);

            if (karyawanApprover == null) {
                attributes.addFlashAttribute("error", "Karyawan tidak ditemukan");
                return "redirect:/surat/setting-approval/form";
            }

            if (id == null || id.isEmpty() || "null".equals(id)) {
                // Create baru
                setting = new SettingApprovalSurat();
                setting.setTanggalDibuat(LocalDate.now());
                setting.setUserDibuat("admin");
                log.info("Creating new setting approval for jenis surat: {}", jenisSurat);
            } else {
                // Update
                setting = settingApprovalSuratDao.findById(id).orElse(new SettingApprovalSurat());
                log.info("Updating setting approval ID: {}", id);
            }

            setting.setNamaSetting(namaSetting);
            setting.setJenisSurat(jenisSurat);
            setting.setKaryawanApprover(karyawanApprover);
            setting.setStatus(StatusRecord.AKTIF);
            setting.setUserUpdate("admin");
            setting.setTanggalUpdate(LocalDate.now());

            settingApprovalSuratDao.save(setting);

            attributes.addFlashAttribute("success", "Setting approval berhasil disimpan");

            // Jika action adalah "saveAndNew", tetap di form untuk tambah lagi
            if ("saveAndNew".equals(action)) {
                return "redirect:/surat/setting-approval/form";
            } else {
                return "redirect:/surat/setting-approval/list";
            }
        } catch (Exception e) {
            log.error("Error saving setting approval", e);
            attributes.addFlashAttribute("error", "Gagal menyimpan setting approval: " + e.getMessage());
            return "redirect:/surat/setting-approval/form";
        }
    }

    // Nonaktifkan setting approval
    @PostMapping("/delete/{id}")
    public String deleteSettingApproval(@PathVariable String id, RedirectAttributes attributes) {
        try {
            SettingApprovalSurat setting = settingApprovalSuratDao.findById(id).orElse(null);
            if (setting != null) {
                setting.setStatus(StatusRecord.HAPUS);
                settingApprovalSuratDao.save(setting);
                attributes.addFlashAttribute("success", "Setting approval berhasil dinonaktifkan");
            } else {
                attributes.addFlashAttribute("error", "Setting approval tidak ditemukan");
            }
        } catch (Exception e) {
            log.error("Error deleting setting approval", e);
            attributes.addFlashAttribute("error", "Gagal menonaktifkan setting approval: " + e.getMessage());
        }

        return "redirect:/surat/setting-approval/list";
    }

    // Aktifkan kembali setting approval
    @PostMapping("/activate/{id}")
    public String activateSettingApproval(@PathVariable String id, RedirectAttributes attributes) {
        try {
            SettingApprovalSurat setting = settingApprovalSuratDao.findById(id).orElse(null);
            if (setting != null) {
                setting.setStatus(StatusRecord.AKTIF);
                settingApprovalSuratDao.save(setting);
                attributes.addFlashAttribute("success", "Setting approval berhasil diaktifkan kembali");
            } else {
                attributes.addFlashAttribute("error", "Setting approval tidak ditemukan");
            }
        } catch (Exception e) {
            log.error("Error activating setting approval", e);
            attributes.addFlashAttribute("error", "Gagal mengaktifkan setting approval: " + e.getMessage());
        }

        return "redirect:/surat/setting-approval/list";
    }
}
