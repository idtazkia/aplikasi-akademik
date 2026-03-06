package id.ac.tazkia.smilemahasiswa.controller;

import java.io.IOException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.FillPatternType;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.HorizontalAlignment;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.beans.propertyeditors.StringTrimmerEditor;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.web.multipart.MultipartFile;

import id.ac.tazkia.smilemahasiswa.dao.MahasiswaDao;
import id.ac.tazkia.smilemahasiswa.dao.ProdiDao;
import id.ac.tazkia.smilemahasiswa.dao.RiwayatStatusMahasiswaDao;
import id.ac.tazkia.smilemahasiswa.dao.TahunAkademikDao;
import id.ac.tazkia.smilemahasiswa.dto.BulkUpdateStatusDto;
import id.ac.tazkia.smilemahasiswa.dto.SmartSuggestionDto;
import id.ac.tazkia.smilemahasiswa.dto.StatusBerjalanDto;
import id.ac.tazkia.smilemahasiswa.dto.StatusKeluarDto;
import id.ac.tazkia.smilemahasiswa.dto.StatusMasukDto;
import id.ac.tazkia.smilemahasiswa.entity.JenisMasuk;
import id.ac.tazkia.smilemahasiswa.entity.Mahasiswa;
import id.ac.tazkia.smilemahasiswa.entity.Prodi;
import id.ac.tazkia.smilemahasiswa.entity.RiwayatStatusMahasiswa;
import id.ac.tazkia.smilemahasiswa.entity.StatusKeluar;
import id.ac.tazkia.smilemahasiswa.entity.StatusRecord;
import id.ac.tazkia.smilemahasiswa.entity.TahunAkademik;
import id.ac.tazkia.smilemahasiswa.service.CurrentUserService;
import id.ac.tazkia.smilemahasiswa.service.RekapStatusHelper;
import id.ac.tazkia.smilemahasiswa.service.RiwayatStatusService;
import lombok.extern.slf4j.Slf4j;

/**
 * Controller untuk Manajemen Status Mahasiswa
 *
 * Menangani:
 * - Status Masuk (jalur masuk)
 * - Status Berjalan (per semester)
 * - Status Keluar (lulus, mengundurkan diri, dll)
 */
@Controller
@RequestMapping("/status")
@Slf4j
public class StatusMahasiswaController {

    @Autowired
    private RiwayatStatusService riwayatStatusService;

    @Autowired
    private RiwayatStatusMahasiswaDao riwayatStatusMahasiswaDao;

    @Autowired
    private MahasiswaDao mahasiswaDao;

    @Autowired
    private TahunAkademikDao tahunAkademikDao;

    @Autowired
    private ProdiDao prodiDao;

    @Autowired
    private CurrentUserService currentUserService;

    @Autowired
    private RekapStatusHelper rekapStatusHelper;

    // ========================================
    // Init Binder
    // ========================================

    /**
     * Handle empty string untuk LocalDate fields
     */
    @InitBinder
    public void initBinder(WebDataBinder binder) {
        // Register custom editor untuk LocalDate yang handle empty string
        binder.registerCustomEditor(java.time.LocalDate.class, new java.beans.PropertyEditorSupport() {
            @Override
            public void setAsText(String text) throws IllegalArgumentException {
                if (text == null || text.trim().isEmpty()) {
                    setValue(null);
                } else {
                    try {
                        setValue(java.time.LocalDate.parse(text));
                    } catch (Exception e) {
                        throw new IllegalArgumentException("Invalid date format: " + text);
                    }
                }
            }

            @Override
            public String getAsText() {
                java.time.LocalDate value = (java.time.LocalDate) getValue();
                if (value == null) {
                    return "";
                }
                return value.toString();
            }
        });
    }

    // ========================================
    // Halaman Utama
    // ========================================

    /**
     * Halaman utama manajemen status mahasiswa
     */
    @GetMapping
    public String index(Model model,
            @RequestParam(required = false) String prodi,
            @RequestParam(required = false) String angkatan,
            @RequestParam(required = false) String tahun,
            @PageableDefault(size = 20) Pageable pageable) {

        model.addAttribute("listProdi", prodiDao.findAll());
        model.addAttribute("listAngkatan", mahasiswaDao.findAllDistinctAngkatan());
        model.addAttribute("listTahunAkademik", tahunAkademikDao.findAll());

        // Add filter parameters back to model for form retention
        model.addAttribute("prodi", prodi);
        model.addAttribute("angkatan", angkatan);
        model.addAttribute("tahun", tahun);

        if (prodi != null || angkatan != null || tahun != null) {
            // Resolve tahun akademik ID from kode if needed
            String tahunAkademikId = tahun;
            if (tahun != null && !tahun.isEmpty()) {
                // Check if it's a kode (not UUID format)
                if (!tahun.matches("[a-f0-9]{8}-[a-f0-9]{4}-[a-f0-9]{4}-[a-f0-9]{4}-[a-f0-9]{12}")) {
                    // It's a kode, find the ID
                    TahunAkademik ta = tahunAkademikDao.findByKodeTahunAkademik(tahun);
                    if (ta != null) {
                        tahunAkademikId = ta.getId();
                    }
                }
            }

            // Load data berdasarkan filter dengan pagination
            Page<Object[]> dataPage = mahasiswaDao.laporanStatusMahasiswaLengkapPage(
                    tahunAkademikId,
                    angkatan != null ? Arrays.asList(angkatan.split(",")) : null,
                    prodi,
                    pageable);
            model.addAttribute("dataMahasiswaPage", dataPage);
            model.addAttribute("dataMahasiswa", dataPage.getContent());
        }

        return "status-mahasiswa/index";
    }

    // ========================================
    // Tab 1: Quick Update (Bulk Update)
    // ========================================

    /**
     * Halaman bulk update status
     */
    @GetMapping("/bulk")
    public String bulkUpdate(Model model) {
        model.addAttribute("dto", new BulkUpdateStatusDto());
        model.addAttribute("listProdi", prodiDao.findAll());
        model.addAttribute("listAngkatan", mahasiswaDao.findAllDistinctAngkatan());
        model.addAttribute("listTahunAkademik", tahunAkademikDao.findAll());
        model.addAttribute("listStatus", StatusRecord.values());
        return "status-mahasiswa/bulk";
    }

    /**
     * Preview bulk update
     */
    @PostMapping("/bulk/preview")
    @ResponseBody
    public Map<String, Object> previewBulkUpdate(@ModelAttribute BulkUpdateStatusDto dto,
            @RequestParam(required = false) List<String> idMahasiswa) {
        dto.setPreviewMode(true);
        dto.setIdMahasiswa(idMahasiswa);
        RiwayatStatusService.BulkUpdateResult result = riwayatStatusService.bulkUpdateStatus(dto, "preview");

        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("updated", result.getUpdated());
        response.put("skipped", result.getSkipped());
        response.put("message", "Ditemukan " + result.getUpdated() + " mahasiswa yang akan diupdate" +
                (result.getSkipped() > 0 ? ". " + result.getSkipped() + " mahasiswa akan di-skip." : ""));

        return response;
    }

    /**
     * Eksekusi bulk update
     */
    @PostMapping("/bulk/execute")
    @ResponseBody
    public Map<String, Object> executeBulkUpdate(@ModelAttribute BulkUpdateStatusDto dto,
            @RequestParam(required = false) List<String> idMahasiswa,
            Authentication authentication) {

        // Debug loggingjadi
        log.info("=== DEBUG BULK UPDATE ===");
        log.info("idMahasiswa parameter: {}", idMahasiswa);
        log.info("dto.idMahasiswa: {}", dto.getIdMahasiswa());
        log.info("dto.statusBaru: {}", dto.getStatusBaru());
        log.info("dto.idProdi: {}", dto.getIdProdi());
        log.info("dto.angkatan: {}", dto.getAngkatan());
        log.info("dto.idTahunAkademik: {}", dto.getIdTahunAkademik());

        Map<String, Object> response = new HashMap<>();

        try {
            // Set idMahasiswa dari parameter ke DTO
            dto.setIdMahasiswa(idMahasiswa);
            log.info("After setIdMahasiswa, dto.idMahasiswa: {}", dto.getIdMahasiswa());

            String username = currentUserService.currentUser(authentication).getUsername();
            RiwayatStatusService.BulkUpdateResult result = riwayatStatusService.bulkUpdateStatus(dto, username);

            response.put("success", true);
            response.put("updated", result.getUpdated());
            response.put("skipped", result.getSkipped());
            response.put("skippedMessages", result.getSkippedMessages());

            // Build message
            StringBuilder message = new StringBuilder();
            message.append("Berhasil mengupdate status ").append(result.getUpdated()).append(" mahasiswa");
            if (result.getSkipped() > 0) {
                message.append(". ").append(result.getSkipped()).append(" mahasiswa di-skip.");
            }
            response.put("message", message.toString());
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Error: " + e.getMessage());
            e.printStackTrace();
        }

        return response;
    }

    // ========================================
    // Tab 2: Table Edit (Inline Edit)
    // ========================================

    /**
     * Halaman table edit dengan filter
     */
    @GetMapping("/table")
    public String tableEdit(Model model,
            @RequestParam(required = false) String prodi,
            @RequestParam(required = false) String angkatan,
            @RequestParam(required = false) String tahun,
            @RequestParam(required = false) String status,
            @PageableDefault(size = 20) Pageable pageable) {

        // Resolve tahun akademik ID from kode if needed
        String tahunAkademikId = tahun;
        if (tahun != null && !tahun.isEmpty()) {
            // Check if it's a kode (not UUID format)
            if (!tahun.matches("[a-f0-9]{8}-[a-f0-9]{4}-[a-f0-9]{4}-[a-f0-9]{4}-[a-f0-9]{12}")) {
                // It's a kode, find the ID
                TahunAkademik ta = tahunAkademikDao.findByKodeTahunAkademik(tahun);
                if (ta != null) {
                    tahunAkademikId = ta.getId();
                }
            }
        }

        Page<Object[]> data = riwayatStatusMahasiswaDao.findMahasiswaForBulkUpdate(
                tahunAkademikId, prodi, angkatan, status, pageable);

        model.addAttribute("dataMahasiswa", data);
        model.addAttribute("listProdi", prodiDao.findAll());
        model.addAttribute("listAngkatan", mahasiswaDao.findAllDistinctAngkatan());
        model.addAttribute("listTahunAkademik", tahunAkademikDao.findAll());
        model.addAttribute("listStatus", StatusRecord.values());

        // Filter status berjalan yang umum digunakan
        List<StatusRecord> statusBerjalanUmum = Arrays.asList(
            StatusRecord.AKTIF,
            StatusRecord.CUTI,
            StatusRecord.NONAKTIF,
            StatusRecord.MBKM,
            StatusRecord.MENUNGGU_UJIAN,
            StatusRecord.DOUBLE_DEGREE,
            StatusRecord.LULUS,
            StatusRecord.MENGUNDURKAN_DIRI,
            StatusRecord.MENINGGAL_DUNIA,
            StatusRecord.PUTUS_STUDI,
            StatusRecord.DIKELUARKAN,
            StatusRecord.MUTASI
        );
        model.addAttribute("listStatusBerjalanUmum", statusBerjalanUmum);

        // Add filter parameters back to model for form retention
        model.addAttribute("prodi", prodi);
        model.addAttribute("angkatan", angkatan);
        model.addAttribute("tahun", tahun);
        model.addAttribute("status", status);

        return "status-mahasiswa/table";
    }

    /**
     * Save single row update (Ajax)
     */
    @PostMapping("/table/save")
    @ResponseBody
    @Transactional
    public Map<String, Object> saveRowUpdate(@Valid StatusBerjalanDto dto,
            BindingResult bindingResult,
            Authentication authentication) {

        Map<String, Object> response = new HashMap<>();

        if (bindingResult.hasErrors()) {
            response.put("success", false);
            response.put("errors", bindingResult.getAllErrors());
            return response;
        }

        try {
            String username = currentUserService.currentUser(authentication).getUsername();
            RiwayatStatusMahasiswa result = riwayatStatusService.simpanRiwayatStatus(dto, username);

            response.put("success", true);
            response.put("message", "Status berhasil diupdate");
            response.put("data", result);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Error: " + e.getMessage());
        }

        return response;
    }

    // ========================================
    // Tab 3: Smart Suggestions
    // ========================================

    /**
     * Halaman smart suggestions - Update Status Berjalan
     * Data hanya di-load ketika user menekan tombol Filter (search=true)
     */
    @GetMapping("/suggestions")
    public String smartSuggestions(@RequestParam(required = false) String prodi,
            @RequestParam(required = false) String tahun,
            @RequestParam(required = false, defaultValue = "false") boolean search,
            @PageableDefault(size = 20) Pageable pageable,
            Model model) {

        // Jika tahun akademik tidak ditentukan, gunakan tahun akademik aktif
        String idTahunAkademik = tahun;
        if (idTahunAkademik == null || idTahunAkademik.isEmpty()) {
            TahunAkademik taAktif = tahunAkademikDao.findByStatus(StatusRecord.AKTIF);
            if (taAktif != null) {
                idTahunAkademik = taAktif.getId();
            }
        }

        // Hanya load data jika user menekan tombol Filter (search=true)
        if (search) {
            // Ambil data smart suggestions dengan status rekomendasi (paginated)
            Page<SmartSuggestionDto> suggestionsPage = riwayatStatusService.getSmartSuggestions(prodi, idTahunAkademik, pageable);
            List<SmartSuggestionDto> suggestions = suggestionsPage.getContent();

            // Hitung ringkasan dari semua data (bukan hanya halaman ini)
            long jumlahAktif = suggestions.stream()
                    .filter(s -> s.getStatusRekomendasi() == StatusRecord.AKTIF)
                    .count();
            long jumlahCuti = suggestions.stream()
                    .filter(s -> s.getStatusRekomendasi() == StatusRecord.CUTI)
                    .count();
            long jumlahNonaktif = suggestions.stream()
                    .filter(s -> s.getStatusRekomendasi() == StatusRecord.NONAKTIF)
                    .count();
            long jumlahLulus = suggestions.stream()
                    .filter(s -> s.getStatusRekomendasi() == StatusRecord.LULUS)
                    .count();
            long jumlahPutusStudi = suggestions.stream()
                    .filter(s -> s.getStatusRekomendasi() == StatusRecord.PUTUS_STUDI)
                    .count();

            model.addAttribute("suggestionsPage", suggestionsPage);
            model.addAttribute("suggestions", suggestions);
            model.addAttribute("totalMahasiswa", suggestionsPage.getTotalElements());
            model.addAttribute("jumlahAktif", jumlahAktif);
            model.addAttribute("jumlahCuti", jumlahCuti);
            model.addAttribute("jumlahNonaktif", jumlahNonaktif);
            model.addAttribute("jumlahLulus", jumlahLulus);
            model.addAttribute("jumlahPutusStudi", jumlahPutusStudi);
        } else {
            // Pertama kali buka, tidak load data
            model.addAttribute("suggestionsPage", null);
            model.addAttribute("suggestions", null);
            model.addAttribute("totalMahasiswa", 0L);
            model.addAttribute("jumlahAktif", 0L);
            model.addAttribute("jumlahCuti", 0L);
            model.addAttribute("jumlahNonaktif", 0L);
            model.addAttribute("jumlahLulus", 0L);
            model.addAttribute("jumlahPutusStudi", 0L);
        }

        model.addAttribute("listProdi", prodiDao.findAll());
        model.addAttribute("listTahunAkademik", tahunAkademikDao.findAll());
        model.addAttribute("selectedProdi", prodi);
        model.addAttribute("selectedTahun", idTahunAkademik);

        return "status-mahasiswa/suggestions";
    }

    /**
     * Update status berjalan untuk mahasiswa yang dipilih
     */
    @PostMapping("/suggestions/update-status-berjalan")
    public String updateStatusBerjalan(
            @RequestParam("idMahasiswa") List<String> idMahasiswaList,
            @RequestParam("statusRekomendasi") List<String> statusRekomendasiList,
            @RequestParam(value = "prodiFilter", required = false) String prodiFilter,
            @RequestParam(value = "tahunFilter", required = false) String tahunFilter,
            @RequestParam(value = "pageFilter", required = false, defaultValue = "0") int pageFilter,
            @RequestParam(value = "sizeFilter", required = false, defaultValue = "20") int sizeFilter,
            Authentication authentication,
            RedirectAttributes redirectAttributes) {

        log.info("=== UPDATE STATUS BERJALAN ===");
        log.info("idMahasiswaList: {}", idMahasiswaList);
        log.info("tahunFilter: '{}'", tahunFilter);
        log.info("prodiFilter: '{}'", prodiFilter);

        // Build redirect URL with all filters
        String redirectUrl = buildRedirectUrl(prodiFilter, tahunFilter, pageFilter, sizeFilter);

        if (idMahasiswaList == null || idMahasiswaList.isEmpty()) {
            redirectAttributes.addFlashAttribute("error", "Pilih minimal 1 mahasiswa");
            return "redirect:/status/suggestions" + redirectUrl;
        }

        String username = currentUserService.currentUser(authentication).getUsername();

        RiwayatStatusService.BulkUpdateResult result = riwayatStatusService
                .updateStatusBerjalanTerpilih(idMahasiswaList, statusRekomendasiList, tahunFilter, username);

        String message = "Berhasil mengupdate status berjalan untuk " + result.getUpdated() + " mahasiswa";

        if (result.getSkipped() > 0) {
            message += ". " + result.getSkipped() + " mahasiswa dilewati.";
        }

        redirectAttributes.addFlashAttribute("success", message);

        return "redirect:/status/suggestions" + redirectUrl;
    }

    /**
     * Build redirect URL with filter parameters
     */
    private String buildRedirectUrl(String prodi, String tahun, int page, int size) {
        StringBuilder sb = new StringBuilder("?search=true");
        boolean hasParam = true; // Already has search=true

        if (prodi != null && !prodi.isEmpty()) {
            sb.append("&prodi=").append(prodi);
        }

        if (tahun != null && !tahun.isEmpty()) {
            sb.append("&tahun=").append(tahun);
        }

        if (page > 0) {
            sb.append("&page=").append(page);
        }

        if (size != 20) {
            sb.append("&size=").append(size);
        }

        return sb.toString();
    }

    /**
     * Auto-create riwayat status untuk mahasiswa yang belum punya
     */
    @PostMapping("/suggestions/auto-create")
    public String autoCreateRiwayatStatus(@RequestParam String tahunAkademik,
            Authentication authentication,
            RedirectAttributes redirectAttributes) {

        String username = currentUserService.currentUser(authentication).getUsername();
        riwayatStatusService.autoCreateRiwayatStatus(tahunAkademik, username);

        redirectAttributes.addFlashAttribute("success",
                "Berhasil auto-create riwayat status");
        return "redirect:/status/suggestions";
    }

    /**
     * Bulk update status keluar menjadi LULUS untuk mahasiswa yang lulus
     */
    @PostMapping("/suggestions/bulk-set-status-keluar-lulus")
    public String bulkSetStatusKeluarLulus(Authentication authentication,
            RedirectAttributes redirectAttributes) {

        String username = currentUserService.currentUser(authentication).getUsername();
        List<Mahasiswa> results = riwayatStatusService.bulkUpdateStatusKeluarLulus(username);

        redirectAttributes.addFlashAttribute("success",
                "Berhasil mengupdate status keluar LULUS untuk " + results.size() + " mahasiswa");
        return "redirect:/status/suggestions";
    }

    /**
     * Bulk update status keluar untuk mahasiswa yang dipilih via checkbox
     */
    @PostMapping("/suggestions/bulk-update-status-keluar")
    public String bulkUpdateStatusKeluarTerpilih(
            @RequestParam("idMahasiswa") List<String> idMahasiswaList,
            @RequestParam("statusKeluar") String statusKeluarStr,
            Authentication authentication,
            RedirectAttributes redirectAttributes) {

        if (idMahasiswaList == null || idMahasiswaList.isEmpty()) {
            redirectAttributes.addFlashAttribute("error", "Pilih minimal 1 mahasiswa");
            return "redirect:/status/suggestions";
        }

        String username = currentUserService.currentUser(authentication).getUsername();

        try {
            StatusKeluar statusKeluar = StatusKeluar.valueOf(statusKeluarStr);
            RiwayatStatusService.BulkUpdateResult result = riwayatStatusService
                    .bulkUpdateStatusKeluarByIds(idMahasiswaList, statusKeluar, username);

            String message = "Berhasil mengupdate status keluar " + statusKeluar
                    + " untuk " + result.getUpdated() + " mahasiswa";

            if (result.getSkipped() > 0) {
                message += ". " + result.getSkipped() + " mahasiswa dilewati.";
                redirectAttributes.addFlashAttribute("warning", result.getSkippedMessages());
            }

            redirectAttributes.addFlashAttribute("success", message);

        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("error", "Status keluar tidak valid: " + statusKeluarStr);
        }

        return "redirect:/status/suggestions";
    }

    // ========================================
    // Detail Mahasiswa
    // ========================================

    /**
     * Halaman detail riwayat status mahasiswa
     */
    @GetMapping("/mahasiswa/{id}")
    public String detailMahasiswa(@PathVariable String id, Model model) {
        Mahasiswa mahasiswa = mahasiswaDao.findById(id)
                .orElseThrow(() -> new RuntimeException("Mahasiswa tidak ditemukan"));

        List<RiwayatStatusMahasiswa> riwayatStatus = riwayatStatusService.getRiwayatStatusLengkap(id);

        model.addAttribute("listTahunAkademik", tahunAkademikDao.findAll());
        model.addAttribute("mahasiswa", mahasiswa);
        model.addAttribute("riwayatStatus", riwayatStatus);

        return "status-mahasiswa/detail";
    }

    /**
     * Update status keluar mahasiswa
     */
    @PostMapping("/mahasiswa/{id}/keluar")
    public String updateStatusKeluar(@PathVariable String id,
            @ModelAttribute StatusKeluarDto dto,
            Authentication authentication,
            RedirectAttributes redirectAttributes) {

        Mahasiswa mahasiswa = mahasiswaDao.findById(id)
                .orElseThrow(() -> new RuntimeException("Mahasiswa tidak ditemukan"));

        String username = currentUserService.currentUser(authentication).getUsername();

        // Update status keluar
        mahasiswa.setStatusKeluar(dto.getStatusKeluar());
        mahasiswa.setTanggalKeluar(dto.getTanggalKeluar());
        mahasiswa.setAlasanKeluar(dto.getAlasanKeluar());
        mahasiswa.setStatusKeluarUpdateUser(username);
        mahasiswa.setStatusKeluarUpdateDate(java.time.LocalDateTime.now());

        // Set tahun akademik keluar jika ada
        if (dto.getIdTahunAkademikKeluar() != null) {
            TahunAkademik ta = tahunAkademikDao.findById(dto.getIdTahunAkademikKeluar())
                    .orElse(null);
            mahasiswa.setTahunAkademikKeluar(ta);
        }

        mahasiswaDao.save(mahasiswa);

        // Update juga riwayat status jika diminta
        if (Boolean.TRUE.equals(dto.getUpdateRiwayatStatus()) && dto.getIdTahunAkademikKeluar() != null) {
            StatusBerjalanDto statusDto = new StatusBerjalanDto();
            statusDto.setIdMahasiswa(id);
            statusDto.setIdTahunAkademik(dto.getIdTahunAkademikKeluar());
            statusDto.setStatusBerjalan(StatusRecord.valueOf(dto.getStatusKeluar().name()));
            statusDto.setKeterangan("Status keluar: " + dto.getStatusKeluar().getLabel());

            riwayatStatusService.simpanRiwayatStatus(statusDto, username);
        }

        redirectAttributes.addFlashAttribute("success",
                "Status keluar berhasil diupdate");
        return "redirect:/status/mahasiswa/" + id;
    }

    // ========================================
    // Rekap & Laporan
    // ========================================

    /**
     * Halaman rekap status masuk
     */
    @GetMapping("/rekap/masuk")
    public String rekapStatusMasuk(Model model,
            @RequestParam(required = false) String angkatan) {

        List<String> angkatanList = angkatan != null
                ? Arrays.asList(angkatan.split(","))
                : mahasiswaDao.findAllDistinctAngkatan();

        List<Object[]> rekap = mahasiswaDao.rekapJenisMasukByAngkatan(angkatanList);

        model.addAttribute("angkatan", angkatan);
        model.addAttribute("listAngkatan", mahasiswaDao.findAllDistinctAngkatan());
        model.addAttribute("rekap", rekap);

        return "status-mahasiswa/rekap-masuk";
    }

    /**
     * Halaman rekap status keluar
     */
    @GetMapping("/rekap/keluar")
    public String rekapStatusKeluar(Model model,
            @RequestParam(required = false) String tahunAkademik) {

        List<String> tahunList = tahunAkademik != null
                ? Arrays.asList(tahunAkademik.split(","))
                : convertToList(tahunAkademikDao.findAll());

        List<Object[]> rekap = mahasiswaDao.rekapStatusKeluarByTahunAkademik(tahunList);

        model.addAttribute("tahunAkademik", tahunAkademik);
        model.addAttribute("listTahunAkademik", tahunAkademikDao.findAll());
        model.addAttribute("rekap", rekap);

        return "status-mahasiswa/rekap-keluar";
    }

    /**
     * Export rekap status ke Excel
     */
    @GetMapping("/rekap/export")
    public void exportRekapToExcel(
            @RequestParam(required = false) String tahunAkademik,
            @RequestParam(required = false) String angkatan,
            HttpServletResponse response) throws IOException {

        response.setContentType("application/vnd.ms-excel");
        response.setHeader("Content-Disposition", "attachment; filename=rekap_status.xlsx");

        Workbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet("Rekap Status");

        // Header
        Row headerRow = sheet.createRow(0);
        CellStyle headerStyle = workbook.createCellStyle();
        Font headerFont = workbook.createFont();
        headerFont.setBold(true);
        headerFont.setFontHeightInPoints((short) 11);
        headerStyle.setFont(headerFont);
        headerStyle.setAlignment(HorizontalAlignment.CENTER);
        headerStyle.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
        headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);

        String[] headers = {"NIM", "Nama", "Prodi", "Angkatan", "Jenis Masuk",
                "Status Berjalan", "Status Keluar", "Tahun Akademik"};

        for (int i = 0; i < headers.length; i++) {
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(headers[i]);
            cell.setCellStyle(headerStyle);
        }

        // Data
        List<Object[]> data = mahasiswaDao.laporanStatusMahasiswaLengkap(
                tahunAkademik,
                angkatan != null ? Arrays.asList(angkatan.split(",")) : null,
                null);

        int rowNum = 1;
        for (Object[] row : data) {
            Row dataRow = sheet.createRow(rowNum++);
            dataRow.createCell(0).setCellValue((String) row[2]); // NIM
            dataRow.createCell(1).setCellValue((String) row[3]); // Nama
            dataRow.createCell(2).setCellValue((String) row[4]); // Prodi
            dataRow.createCell(3).setCellValue((String) row[5]); // Angkatan
            dataRow.createCell(4).setCellValue(row[6] != null ? row[6].toString() : "-"); // Jenis Masuk
            dataRow.createCell(5).setCellValue(row[11] != null ? row[11].toString() : "-"); // Status Berjalan
            dataRow.createCell(6).setCellValue(row[7] != null ? row[7].toString() : "-"); // Status Keluar
            dataRow.createCell(7).setCellValue(row[9] != null ? row[9].toString() : "-"); // Tahun Akademik
        }

        // Auto-size columns
        for (int i = 0; i < headers.length; i++) {
            sheet.autoSizeColumn(i);
        }

        workbook.write(response.getOutputStream());
        workbook.close();
    }

    // ========================================
    // Import/Export Excel untuk Bulk Update
    // ========================================

    /**
     * Export template Excel untuk bulk update status
     */
    @GetMapping("/export-template")
    public void exportTemplate(HttpServletResponse response) throws IOException {
        response.setContentType("application/vnd.ms-excel");
        response.setHeader("Content-Disposition", "attachment; filename=template_status_mahasiswa.xlsx");

        Workbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet("Template Status");

        // Header
        Row headerRow = sheet.createRow(0);
        CellStyle headerStyle = workbook.createCellStyle();
        Font headerFont = workbook.createFont();
        headerFont.setBold(true);
        headerFont.setFontHeightInPoints((short) 11);
        headerStyle.setFont(headerFont);
        headerStyle.setAlignment(HorizontalAlignment.CENTER);
        headerStyle.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
        headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);

        String[] headers = {"NIM*", "Tahun Akademik (Kode)", "Status Berjalan*", "Tanggal Keluar (yyyy-MM-dd)", "Keterangan"};

        for (int i = 0; i < headers.length; i++) {
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(headers[i]);
            cell.setCellStyle(headerStyle);
        }

        // Contoh data
        Row exampleRow1 = sheet.createRow(1);
        exampleRow1.createCell(0).setCellValue("1115001");
        exampleRow1.createCell(1).setCellValue("20242");
        exampleRow1.createCell(2).setCellValue("AKTIF");
        exampleRow1.createCell(3).setCellValue("");
        exampleRow1.createCell(4).setCellValue("Status aktif semester genap 2024/2025");

        Row exampleRow2 = sheet.createRow(2);
        exampleRow2.createCell(0).setCellValue("1115002");
        exampleRow2.createCell(1).setCellValue("20242");
        exampleRow2.createCell(2).setCellValue("LULUS");
        exampleRow2.createCell(3).setCellValue("2025-02-20");
        exampleRow2.createCell(4).setCellValue("Lulus tepat waktu");

        // Info sheet
        Sheet infoSheet = workbook.createSheet("Info");
        Row infoRow = infoSheet.createRow(0);
        infoRow.createCell(0).setCellValue("PETUNJUK PENGISIAN TEMPLATE");

        int infoRowNum = 2;
        createInfoRow(infoSheet, infoRowNum++, "Kolom", "Keterangan");
        createInfoRow(infoSheet, infoRowNum++, "NIM*", "Nomor Induk Mahasiswa (wajib diisi)");
        createInfoRow(infoSheet, infoRowNum++, "Tahun Akademik", "Kode tahun akademik (opsional, jika kosong pakai dari filter)");
        createInfoRow(infoSheet, infoRowNum++, "Status Berjalan*", "Pilih salah satu:");
        createInfoRow(infoSheet, infoRowNum++, "", "Status Berjalan: AKTIF, CUTI, NONAKTIF, MBKM, MENUNGGU_UJIAN, DOUBLE_DEGREE");
        createInfoRow(infoSheet, infoRowNum++, "", "Status Keluar: LULUS, MENGUNDURKAN_DIRI, MENINGGAL_DUNIA, PUTUS_STUDI, DIKELUARKAN, MUTASI");
        createInfoRow(infoSheet, infoRowNum++, "Tanggal Keluar", "Format: yyyy-MM-dd (hanya untuk status keluar)");
        createInfoRow(infoSheet, infoRowNum++, "Keterangan", "Keterangan tambahan (opsional)");
        createInfoRow(infoSheet, infoRowNum++, "", "");
        createInfoRow(infoSheet, infoRowNum++, "CATATAN:", "");
        createInfoRow(infoSheet, infoRowNum++, "", "- Kolom dengan tanda * wajib diisi");
        createInfoRow(infoSheet, infoRowNum++, "", "- Jika Status Berjalan adalah status keluar, tanggal keluar akan diset ke hari ini jika kosong");
        createInfoRow(infoSheet, infoRowNum++, "", "- Pastikan NIM terdaftar di sistem");

        // Auto-size columns
        for (int i = 0; i < headers.length; i++) {
            sheet.autoSizeColumn(i);
        }
        infoSheet.autoSizeColumn(0);
        infoSheet.autoSizeColumn(1);

        workbook.write(response.getOutputStream());
        workbook.close();
    }

    private void createInfoRow(Sheet sheet, int rowNum, String col1, String col2) {
        Row row = sheet.createRow(rowNum);
        row.createCell(0).setCellValue(col1);
        row.createCell(1).setCellValue(col2);
    }

    /**
     * Import Excel untuk bulk update status
     */
    @PostMapping("/import-excel")
    @ResponseBody
    @Transactional
    public Map<String, Object> importExcel(@RequestParam("file") MultipartFile file,
            @RequestParam(required = false) String prodi,
            @RequestParam(required = false) String angkatan,
            @RequestParam(required = false) String tahun,
            Authentication authentication) {

        Map<String, Object> response = new HashMap<>();

        try {
            if (file.isEmpty()) {
                response.put("success", false);
                response.put("message", "File tidak boleh kosong");
                return response;
            }

            // Get default tahun akademik from filter
            String defaultTahunAkademikId = tahun;
            if (tahun != null && !tahun.isEmpty()) {
                if (!tahun.matches("[a-f0-9]{8}-[a-f0-9]{4}-[a-f0-9]{4}-[a-f0-9]{4}-[a-f0-9]{12}")) {
                    TahunAkademik ta = tahunAkademikDao.findByKodeTahunAkademik(tahun);
                    if (ta != null) {
                        defaultTahunAkademikId = ta.getId();
                    }
                }
            }

            int successCount = 0;
            int errorCount = 0;
            List<String> errors = new ArrayList<>();

            Workbook workbook = new XSSFWorkbook(file.getInputStream());
            Sheet sheet = workbook.getSheetAt(0);

            // Skip header row
            for (int i = 1; i <= sheet.getLastRowNum(); i++) {
                Row row = sheet.getRow(i);
                if (row == null) continue;

                try {
                    // Read cells
                    String nim = getCellValueAsString(row.getCell(0));
                    String tahunAkademikKode = getCellValueAsString(row.getCell(1));
                    String statusBaruStr = getCellValueAsString(row.getCell(2));
                    String tanggalKeluarStr = getCellValueAsString(row.getCell(3));
                    String keterangan = getCellValueAsString(row.getCell(4));

                    // Validate required fields
                    if (nim == null || nim.trim().isEmpty()) {
                        errors.add("Baris " + (i + 1) + ": NIM wajib diisi");
                        errorCount++;
                        continue;
                    }

                    if (statusBaruStr == null || statusBaruStr.trim().isEmpty()) {
                        errors.add("Baris " + (i + 1) + ": Status Berjalan wajib diisi");
                        errorCount++;
                        continue;
                    }

                    // Find mahasiswa
                    Mahasiswa mahasiswa = mahasiswaDao.findByNim(nim);
                    if (mahasiswa == null) {
                        errors.add("Baris " + (i + 1) + ": Mahasiswa dengan NIM " + nim + " tidak ditemukan");
                        errorCount++;
                        continue;
                    }

                    // Parse status
                    StatusRecord statusBaru;
                    try {
                        statusBaru = StatusRecord.valueOf(statusBaruStr.trim().toUpperCase());
                    } catch (IllegalArgumentException e) {
                        errors.add("Baris " + (i + 1) + ": Status '" + statusBaruStr + "' tidak valid");
                        errorCount++;
                        continue;
                    }

                    // Get tahun akademik
                    String tahunAkademikId = defaultTahunAkademikId;
                    if (tahunAkademikKode != null && !tahunAkademikKode.trim().isEmpty()) {
                        // Try to find by kode first
                        TahunAkademik ta = tahunAkademikDao.findByKodeTahunAkademik(tahunAkademikKode.trim());
                        if (ta != null) {
                            tahunAkademikId = ta.getId();
                        } else {
                            // Try as ID
                            ta = tahunAkademikDao.findById(tahunAkademikKode.trim()).orElse(null);
                            if (ta != null) {
                                tahunAkademikId = ta.getId();
                            }
                        }
                    }

                    if (tahunAkademikId == null || tahunAkademikId.trim().isEmpty()) {
                        errors.add("Baris " + (i + 1) + ": Tahun Akademik tidak ditentukan (tidak ada di file dan tidak ada di filter)");
                        errorCount++;
                        continue;
                    }

                    // Create DTO
                    BulkUpdateStatusDto dto = new BulkUpdateStatusDto();
                    dto.setIdMahasiswa(Arrays.asList(mahasiswa.getId()));
                    dto.setIdTahunAkademik(tahunAkademikId);
                    dto.setStatusBaru(statusBaru);
                    dto.setKeterangan(keterangan);

                    // Parse tanggal keluar if provided
                    if (tanggalKeluarStr != null && !tanggalKeluarStr.trim().isEmpty()) {
                        try {
                            dto.setTanggalKeluar(LocalDate.parse(tanggalKeluarStr.trim()));
                        } catch (Exception e) {
                            errors.add("Baris " + (i + 1) + ": Format tanggal keluar salah, gunakan yyyy-MM-dd");
                            errorCount++;
                            continue;
                        }
                    }

                    // Execute update
                    String username = currentUserService.currentUser(authentication).getUsername();
                    riwayatStatusService.bulkUpdateStatus(dto, username);
                    successCount++;

                } catch (Exception e) {
                    log.error("Error processing row " + (i + 1) + ": " + e.getMessage(), e);
                    errors.add("Baris " + (i + 1) + ": " + e.getMessage());
                    errorCount++;
                }
            }

            workbook.close();

            StringBuilder message = new StringBuilder();
            message.append("Import selesai. ");
            message.append("Berhasil: ").append(successCount).append(", ");
            message.append("Gagal: ").append(errorCount);

            if (!errors.isEmpty()) {
                message.append(".\n\nError:\n");
                // Limit error messages to avoid too long response
                int maxErrors = 10;
                for (int i = 0; i < Math.min(errors.size(), maxErrors); i++) {
                    message.append(errors.get(i)).append("\n");
                }
                if (errors.size() > maxErrors) {
                    message.append("... dan ").append(errors.size() - maxErrors).append(" error lainnya");
                }
            }

            response.put("success", true);
            response.put("message", message.toString());
            response.put("successCount", successCount);
            response.put("errorCount", errorCount);

        } catch (Exception e) {
            log.error("Error importing Excel: " + e.getMessage(), e);
            response.put("success", false);
            response.put("message", "Error: " + e.getMessage());
        }

        return response;
    }

    private String getCellValueAsString(Cell cell) {
        if (cell == null) {
            return null;
        }

        switch (cell.getCellType()) {
            case STRING:
                return cell.getStringCellValue().trim();
            case NUMERIC:
                return String.valueOf((long) cell.getNumericCellValue());
            case BOOLEAN:
                return String.valueOf(cell.getBooleanCellValue());
            case FORMULA:
                return cell.getCellFormula();
            default:
                return null;
        }
    }

    // ========================================
    // Model Attributes
    // ========================================

    @ModelAttribute("listJenisMasuk")
    public List<JenisMasuk> listJenisMasuk() {
        return Arrays.asList(JenisMasuk.values());
    }

    @ModelAttribute("listStatusKeluar")
    public List<StatusKeluar> listStatusKeluar() {
        return Arrays.asList(StatusKeluar.values());
    }

    @ModelAttribute("listStatusRecord")
    public List<StatusRecord> listStatusRecord() {
        return Arrays.asList(StatusRecord.values());
    }

    @ModelAttribute("rekapStatusHelper")
    public RekapStatusHelper rekapStatusHelper() {
        return rekapStatusHelper;
    }

    /**
     * Helper method untuk convert Iterable ke List<String>
     */
    private List<String> convertToList(Iterable<TahunAkademik> iterable) {
        List<String> result = new ArrayList<>();
        for (TahunAkademik t : iterable) {
            result.add(t.getId());
        }
        return result;
    }
}
