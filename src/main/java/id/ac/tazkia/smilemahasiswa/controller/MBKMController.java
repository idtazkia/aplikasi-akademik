package id.ac.tazkia.smilemahasiswa.controller;

import id.ac.tazkia.smilemahasiswa.dao.*;
import id.ac.tazkia.smilemahasiswa.entity.*;
import id.ac.tazkia.smilemahasiswa.service.CurrentUserService;
import id.ac.tazkia.smilemahasiswa.service.MBKMService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.RandomStringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;

@Controller
@Slf4j
public class MBKMController {

    @Autowired private MBKMService mbkmService;

    @Autowired private PendaftaranMbkmDao pendaftaranMbkmDao;

    @Autowired private CurrentUserService currentUserService;

    @Autowired private KaryawanDao karyawanDao;

    @Autowired private DosenDao dosenDao;

    @Autowired private TahunAkademikDao tahunAkademikDao;

    @Autowired private MatakuliahMbkmDao matakuliahMbkmDao;

    @Autowired private JadwalDao jadwalDao;

    @Autowired private HariDao hariDao;

    @Autowired private KelasDao kelasDao;

    @Autowired private RuanganDao ruanganDao;

    @Autowired private TahunProdiDao tahunProdiDao;

    @Autowired private KrsDao krsDao;

    @Autowired private KrsDetailDao krsDetailDao;

    @Autowired private MahasiswaDao mahasiswaDao;

    @Autowired private GradeDao gradeDao;

    @ModelAttribute("tahunAkademik")
    public Iterable<TahunAkademik> tahunAkademik(){
        return tahunAkademikDao.findByStatusNotInOrderByTahunDesc(Arrays.asList(StatusRecord.HAPUS));
    }

    // api response
    @GetMapping("/get-matkul-mbkm/{id}")
    @ResponseBody
    public List<MatakuliahMbkm> getRequestMatkulMBKM(@PathVariable String id) {
        TahunAkademik tahun = tahunAkademikDao.findByStatus(StatusRecord.AKTIF);
        Mahasiswa mhs = mahasiswaDao.findById(id).get();
        Krs krs = krsDao.findByMahasiswaAndTahunAkademikAndStatus(mhs, tahun, StatusRecord.AKTIF);

        return matakuliahMbkmDao.findByKrsAndStatus(krs, StatusRecord.AKTIF);
    }

    @GetMapping("/get-matkul-mbkm-approved/{id}")
    @ResponseBody
    public List<KrsDetail> getRequestMatkulMBKMApproved(@PathVariable String id) {
        TahunAkademik tahun = tahunAkademikDao.findByStatus(StatusRecord.AKTIF);
        Mahasiswa mhs = mahasiswaDao.findById(id).get();
        Krs krs = krsDao.findByMahasiswaAndTahunAkademikAndStatus(mhs, tahun, StatusRecord.AKTIF);

        return krsDetailDao.findByStatusAndTahunAkademikAndKrsAndStatusKonversi(StatusRecord.AKTIF, tahun, krs, StatusRecord.MBKM);
    }

    @PostMapping("/mbkm/delete/{id}")
    public String deleteMatkul(@PathVariable String id){
        MatakuliahMbkm matkul = matakuliahMbkmDao.findById(id).get();

        matkul.setStatus(StatusRecord.HAPUS);
        matakuliahMbkmDao.save(matkul);

        return "redirect:/studiesActivity/krs/approval/list";
    }

    @PostMapping("/mbkm/waiting")
    public String waitingMbkm(@RequestParam PendaftaranMbkm mbkm){

        mbkm.setStatusApproval(StatusApprove.WAITING);
        pendaftaranMbkmDao.save(mbkm);

        List<MatakuliahMbkm> listMahasiswa = matakuliahMbkmDao.findByPendaftaranMbkmAndStatus(mbkm,  StatusRecord.FINISHED);
        if (listMahasiswa != null || !listMahasiswa.isEmpty()) {
            for (MatakuliahMbkm listMatkur : listMahasiswa){
                listMatkur.setStatus(StatusRecord.AKTIF);
                matakuliahMbkmDao.save(listMatkur);
            }
        }

        Krs krs = krsDao.findByMahasiswaAndTahunAkademikAndStatus(mbkm.getMahasiswa(), mbkm.getTahunAkademik(), StatusRecord.AKTIF);
        if (krs != null) {
            List<KrsDetail> matkul = krsDetailDao.findByKrsAndStatusAndStatusKonversi(krs, StatusRecord.AKTIF, StatusRecord.MBKM);
            if (matkul != null || !matkul.isEmpty()) {
                for (KrsDetail mat : matkul){
                    mat.setStatus(StatusRecord.HAPUS);
                    krsDetailDao.save(mat);
                    Jadwal jadwal = mat.getJadwal();
                    jadwal.setStatus(StatusRecord.HAPUS);
                    jadwalDao.save(jadwal);
                }
            }
        }

        return "redirect:/studiesActivity/krs/approval/list";
    }

    @PostMapping("/mbkm/approved")
    public String approvedMBKM(@RequestParam PendaftaranMbkm mbkm){

        mbkm.setStatusApproval(StatusApprove.APPROVED);
        pendaftaranMbkmDao.save(mbkm);

        TahunAkademik tahun = tahunAkademikDao.findByStatus(StatusRecord.AKTIF);
        TahunAkademikProdi tahunProdi = tahunProdiDao.findByTahunAkademikAndProdi(tahun, mbkm.getMahasiswa().getIdProdi());

        List<MatakuliahMbkm> listMatakuliah = matakuliahMbkmDao.findByPendaftaranMbkmAndStatus(mbkm,  StatusRecord.AKTIF);
        if (listMatakuliah != null || !listMatakuliah.isEmpty()) {
            for (MatakuliahMbkm listMatkur : listMatakuliah){
                listMatkur.setStatus(StatusRecord.FINISHED);
                matakuliahMbkmDao.save(listMatkur);

                Dosen dosen = dosenDao.findById("YaserT").get();
                Hari hari = hariDao.findById("0").get();
                Kelas kelas = kelasDao.findById("-").get();
                Ruangan ruangan = ruanganDao.findById("01").get();

                mbkmService.buatJadwalKrsMbkm(listMatkur, dosen, tahun, tahunProdi, hari, kelas, ruangan);
            }
        }

        return "redirect:/studiesActivity/krs/approval/list";
    }

    @PostMapping("/mbkm/reject")
    public String rejectMBKM(@RequestParam PendaftaranMbkm mbkm){

        mbkm.setStatusApproval(StatusApprove.REJECTED);
        pendaftaranMbkmDao.save(mbkm);

        List<MatakuliahMbkm> listMahasiswa = matakuliahMbkmDao.findByPendaftaranMbkmAndStatus(mbkm,  StatusRecord.AKTIF);
        if (listMahasiswa != null || !listMahasiswa.isEmpty()) {
            for (MatakuliahMbkm listMatkur : listMahasiswa){
                listMatkur.setStatus(StatusRecord.REJECTED);
                matakuliahMbkmDao.save(listMatkur);
            }
        }

        Krs krs = krsDao.findByMahasiswaAndTahunAkademikAndStatus(mbkm.getMahasiswa(), mbkm.getTahunAkademik(), StatusRecord.AKTIF);
        if (krs != null) {
            List<KrsDetail> matkul = krsDetailDao.findByKrsAndStatusAndStatusKonversi(krs, StatusRecord.AKTIF, StatusRecord.MBKM);
            if (matkul != null || !matkul.isEmpty()) {
                for (KrsDetail mat : matkul){
                    mat.setStatus(StatusRecord.HAPUS);
                    krsDetailDao.save(mat);
                    Jadwal jadwal = mat.getJadwal();
                    jadwal.setStatus(StatusRecord.HAPUS);
                    jadwalDao.save(jadwal);
                }
            }
        }

        return "redirect:/studiesActivity/krs/approval/list";
    }

    // penilaian MBKM

    @GetMapping("/studiesActivity/mbkm/list")
    public void penilaianMBKM(Model model, @PageableDefault(size = 10) Pageable page, @RequestParam(required = false) TahunAkademik tahun, Authentication authentication){

        User user = currentUserService.currentUser(authentication);
        Dosen dosen = dosenDao.findByKaryawanIdUser(user);
        TahunAkademik aktif = tahunAkademikDao.findByStatus(StatusRecord.AKTIF);
        if (tahun != null) {
            model.addAttribute("listMahasiswa", pendaftaranMbkmDao.penilaianMbkmMahasiswa(tahun.getId(), dosen.getId(), page));
            model.addAttribute("selectedTahun", tahun);
        }else{
            model.addAttribute("listMahasiswa", pendaftaranMbkmDao.penilaianMbkmMahasiswa(aktif.getId(), dosen.getId(), page));
            model.addAttribute("selectedTahun", aktif);
        }
    }

    @GetMapping("/studiesActivity/mbkm/detail")
    public void detailPenilaian(Model model, @RequestParam(required = false) Krs krs, Authentication authentication){

        List<KrsDetail> listMatkul = krsDetailDao.findByKrsAndStatusAndStatusKonversi(krs, StatusRecord.AKTIF, StatusRecord.MBKM);
        model.addAttribute("krs", krs);
        model.addAttribute("grade", gradeDao.findByStatus(StatusRecord.AKTIF));
        model.addAttribute("matkul", listMatkul);
        model.addAttribute("mhs", krs.getMahasiswa());

    }

    @PostMapping("/mbkm/penilaian/input")
    public String inputNilai(@RequestParam Krs krs, HttpServletRequest request){

        List<KrsDetail> listMatkul = krsDetailDao.findByKrsAndStatusAndStatusKonversi(krs, StatusRecord.AKTIF, StatusRecord.MBKM);
        for (KrsDetail matkul : listMatkul){
            String pilihan = request.getParameter("grade-"+matkul.getId());
            if (pilihan != null || !pilihan.trim().isEmpty()) {
                Grade grade = gradeDao.findByNama(pilihan);
                matkul.setGrade(pilihan);
                matkul.setNilaiAkhir(grade.getBawah());
                matkul.setBobot(grade.getBobot());
                krsDetailDao.save(matkul);
            }
        }

        return "redirect:/studiesActivity/mbkm/list";
    }
}
