package id.ac.tazkia.smilemahasiswa.controller.surat.mahasiswa;

import id.ac.tazkia.smilemahasiswa.dao.DosenDao;
import id.ac.tazkia.smilemahasiswa.dao.JadwalDao;
import id.ac.tazkia.smilemahasiswa.dao.KaryawanDao;
import id.ac.tazkia.smilemahasiswa.dao.KrsDao;
import id.ac.tazkia.smilemahasiswa.dao.KrsDetailDao;
import id.ac.tazkia.smilemahasiswa.dao.MahasiswaDao;
import id.ac.tazkia.smilemahasiswa.dao.TahunAkademikDao;
import id.ac.tazkia.smilemahasiswa.dao.surat.SuratTugasKuliahDao;
import id.ac.tazkia.smilemahasiswa.service.surat.SuratTugasKuliahService;
import id.ac.tazkia.smilemahasiswa.entity.Dosen;
import id.ac.tazkia.smilemahasiswa.entity.Jadwal;
import id.ac.tazkia.smilemahasiswa.entity.Karyawan;
import id.ac.tazkia.smilemahasiswa.entity.Krs;
import id.ac.tazkia.smilemahasiswa.entity.KrsDetail;
import id.ac.tazkia.smilemahasiswa.entity.Mahasiswa;
import id.ac.tazkia.smilemahasiswa.entity.Prodi;
import id.ac.tazkia.smilemahasiswa.entity.StatusApprove;
import id.ac.tazkia.smilemahasiswa.entity.StatusRecord;
import id.ac.tazkia.smilemahasiswa.entity.TahunAkademik;
import id.ac.tazkia.smilemahasiswa.entity.User;
import id.ac.tazkia.smilemahasiswa.entity.surat.SuratTugasKuliah;
import id.ac.tazkia.smilemahasiswa.service.CurrentUserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Controller
@RequestMapping("/surat/tugas_kuliah")
@Slf4j
public class TugasKuliahController {

    @Autowired
    private SuratTugasKuliahDao suratTugasKuliahDao;

    @Autowired
    private SuratTugasKuliahService suratTugasKuliahService;

    @Autowired
    private MahasiswaDao mahasiswaDao;

    @Autowired
    private JadwalDao jadwalDao;

    @Autowired
    private TahunAkademikDao tahunAkademikDao;

    @Autowired
    private KaryawanDao karyawanDao;

    @Autowired
    private DosenDao dosenDao;

    @Autowired
    private KrsDao krsDao;

    @Autowired
    private KrsDetailDao krsDetailDao;

    @Autowired
    private CurrentUserService currentUserService;

    @GetMapping("/form")
    public String formSuratTugasKuliah(Model model, Authentication authentication) {
        model.addAttribute("title", "Surat Tugas Kuliah");

        // Get current mahasiswa
        Mahasiswa mahasiswa = mahasiswaDao.findByUser(currentUserService.currentUser(authentication));
        model.addAttribute("mahasiswa", mahasiswa);

        // Get active tahun akademik
        TahunAkademik tahunAkademik = tahunAkademikDao.findByStatus(StatusRecord.AKTIF);

        // Get KRS mahasiswa di semester aktif (pattern dari dashboard)
        Krs k = krsDao.findByTahunAkademikAndMahasiswaAndStatus(tahunAkademik, mahasiswa, StatusRecord.AKTIF);

        // Get list jadwal yang diambil mahasiswa (from KrsDetail)
        List<Jadwal> listJadwal = new ArrayList<>();
        if (k != null) {
            List<KrsDetail> krsDetails = krsDetailDao.findByMahasiswaAndKrsAndStatus(mahasiswa, k, StatusRecord.AKTIF);
            for (KrsDetail kd : krsDetails) {
                if (kd.getJadwal() != null && kd.getJadwal().getStatus() == StatusRecord.AKTIF) {
                    listJadwal.add(kd.getJadwal());
                }
            }
        }
        model.addAttribute("listJadwal", listJadwal);

        // Create new object
        model.addAttribute("suratTugasKuliah", new SuratTugasKuliah());

        // Get list surat for this mahasiswa
        List<SuratTugasKuliah> listSurat = suratTugasKuliahDao
                .findByMahasiswaAndStatusOrderByTanggalPengajuanDesc(mahasiswa, StatusRecord.AKTIF);
        model.addAttribute("listSurat", listSurat);

        return "surat/surattugaskuliah/form";
    }

    @PostMapping("/simpan")
    public String simpanSuratTugasKuliah(@RequestParam Jadwal jadwal,
                                         @RequestParam String namaPerusahaan,
                                         @RequestParam String alamatPerusahaan,
                                         Authentication authentication,
                                         RedirectAttributes attributes) {
        try {
            Mahasiswa mahasiswa = mahasiswaDao.findByUser(currentUserService.currentUser(authentication));

            SuratTugasKuliah surat = new SuratTugasKuliah();
            surat.setMahasiswa(mahasiswa);
            surat.setJadwal(jadwal);
            surat.setNamaPerusahaan(namaPerusahaan);
            surat.setAlamatPerusahaan(alamatPerusahaan);
            surat.setHal("Permohonan Wawancara Tugas Kuliah");
            surat.setStatus(StatusRecord.AKTIF);
            surat.setTanggalPengajuan(LocalDate.now());

            suratTugasKuliahDao.save(surat);

            log.info("Surat Tugas Kuliah berhasil disimpan untuk mahasiswa: {}", mahasiswa.getNim());

            attributes.addFlashAttribute("success", "Surat Tugas Kuliah berhasil diajukan!");
        } catch (Exception e) {
            log.error("Error menyimpan Surat Tugas Kuliah: {}", e.getMessage(), e);
            attributes.addFlashAttribute("error", "Gagal menyimpan surat: " + e.getMessage());
        }

        return "redirect:/surat/tugas_kuliah/form";
    }

    @GetMapping("/list")
    public String listSuratTugasKuliah(Model model,
                                       Authentication authentication,
                                       @PageableDefault(size = 10) Pageable pageable) {
        model.addAttribute("title", "Daftar Surat Tugas Kuliah");

        Mahasiswa mahasiswa = mahasiswaDao.findByUser(currentUserService.currentUser(authentication));

        model.addAttribute("data", suratTugasKuliahDao.findByMahasiswaAndStatusOrderByTanggalPengajuanDesc(
                mahasiswa, StatusRecord.AKTIF));

        return "surat/surattugaskuliah/list";
    }

    // ================== APPROVAL KAPRODI ==================

    @GetMapping("/kaprodi/list")
    public String listApprovalKaprodi(Model model,
                                      Authentication authentication,
                                      @PageableDefault(size = 10) Pageable pageable) {
        model.addAttribute("title", "Approval Surat Tugas Kuliah - Kaprodi");

        // Get kaprodi dari user yang login (pattern dari dashboard)
        User user = currentUserService.currentUser(authentication);
        Karyawan karyawan = karyawanDao.findByIdUser(user);

        if (karyawan == null) {
            model.addAttribute("error", "Anda tidak terdaftar sebagai Dosen/Kaprodi");
            return "surat/surattugaskuliah/kaprodi-list";
        }

        Dosen dosen = dosenDao.findByKaryawan(karyawan);
        if (dosen == null) {
            model.addAttribute("error", "Data dosen tidak ditemukan");
            return "surat/surattugaskuliah/kaprodi-list";
        }

        // Get prodi dari kaprodi (pakai dosen.getProdi() - field di entity Dosen)
        Prodi prodi = dosen.getProdi();
        if (prodi == null) {
            model.addAttribute("error", "Anda tidak menaungi prodi manapun");
            return "surat/surattugaskuliah/kaprodi-list";
        }

        // Get surat yang menunggu approval dari prodi ini
        model.addAttribute("data", suratTugasKuliahDao.findByProdiAndStatusAndStatusApproveKaprodi(
                prodi, StatusRecord.AKTIF, StatusApprove.WAITING, pageable));

        model.addAttribute("prodi", prodi);
        model.addAttribute("kaprodi", karyawan);

        return "surat/surattugaskuliah/kaprodi-list";
    }

    @PostMapping("/kaprodi/approve")
    public String approveKaprodi(@RequestParam SuratTugasKuliah suratTugasKuliah,
                                  @RequestParam(required = false) String komentar,
                                  Authentication authentication) {
        try {
            User user = currentUserService.currentUser(authentication);
            Karyawan kaprodi = karyawanDao.findByIdUser(user);

            suratTugasKuliah.setStatusApproveKaprodi(StatusApprove.APPROVED);
            suratTugasKuliah.setUserApprovalKaprodi(kaprodi);
            suratTugasKuliah.setKomentarKaprodi(komentar);
            suratTugasKuliah.setTanggalApproveKaprodi(LocalDate.now());

            suratTugasKuliahDao.save(suratTugasKuliah);

            log.info("Surat Tugas Kuliah {} disetujui oleh Kaprodi {}", suratTugasKuliah.getId(), kaprodi.getNamaKaryawan());
        } catch (Exception e) {
            log.error("Error approve surat: {}", e.getMessage(), e);
        }

        return "redirect:/surat/tugas_kuliah/kaprodi/list";
    }

    @PostMapping("/kaprodi/reject")
    public String rejectKaprodi(@RequestParam SuratTugasKuliah suratTugasKuliah,
                                 @RequestParam(required = false) String komentar,
                                 Authentication authentication) {
        try {
            User user = currentUserService.currentUser(authentication);
            Karyawan kaprodi = karyawanDao.findByIdUser(user);

            suratTugasKuliah.setStatusApproveKaprodi(StatusApprove.REJECTED);
            suratTugasKuliah.setUserApprovalKaprodi(kaprodi);
            suratTugasKuliah.setKomentarKaprodi(komentar);
            suratTugasKuliah.setTanggalApproveKaprodi(LocalDate.now());

            suratTugasKuliahDao.save(suratTugasKuliah);

            log.info("Surat Tugas Kuliah {} ditolak oleh Kaprodi {}", suratTugasKuliah.getId(), kaprodi.getNamaKaryawan());
        } catch (Exception e) {
            log.error("Error reject surat: {}", e.getMessage(), e);
        }

        return "redirect:/surat/tugas_kuliah/kaprodi/list";
    }

    @GetMapping("/cetak")
    public void cetakSuratTugasKuliah(@RequestParam("surat") String suratId,
                                      HttpServletResponse response) throws IOException {
        SuratTugasKuliah surat = suratTugasKuliahDao.findById(suratId)
                .orElseThrow(() -> new RuntimeException("Data tidak ditemukan"));

        String fileName = "Surat_Tugas_Kuliah_" + surat.getMahasiswa().getNim() +
                        "_" + surat.getMahasiswa().getNama().replace(" ", "_") + ".pdf";

        response.setContentType("application/pdf");
        response.setHeader("Content-Disposition", "attachment; filename=\"" + fileName + "\"");

        try {
            suratTugasKuliahService.cetakSuratTugasKuliah(suratId, response.getOutputStream());
        } catch (Exception e) {
            log.error("Error generating PDF: {}", e.getMessage(), e);
            throw new RuntimeException("Gagal generate PDF: " + e.getMessage());
        }
    }

    // ================== ADMIN VIEW ==================

    @GetMapping("/admin/list")
    public String listAdmin(Model model,
                            @PageableDefault(size = 10) Pageable pageable) {
        model.addAttribute("title", "Daftar Surat Tugas Kuliah - Admin");

        model.addAttribute("data", suratTugasKuliahDao
                .findAllByStatusOrderByTanggalPengajuanDesc(StatusRecord.AKTIF, pageable));

        return "surat/surattugaskuliah/admin-list";
    }
}
