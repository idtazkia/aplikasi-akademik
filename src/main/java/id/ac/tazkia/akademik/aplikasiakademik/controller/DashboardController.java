package id.ac.tazkia.akademik.aplikasiakademik.controller;

import id.ac.tazkia.akademik.aplikasiakademik.dao.*;
import id.ac.tazkia.akademik.aplikasiakademik.dto.RekapMissAttendance;
import id.ac.tazkia.akademik.aplikasiakademik.entity.*;
import id.ac.tazkia.akademik.aplikasiakademik.service.CurrentUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;
import java.util.Objects;

@Controller
public class DashboardController {

    @Autowired
    private MahasiswaDao mahasiswaDao;

    @Autowired
    private DosenDao dosenDao;

    @Autowired
    private KaryawanDao karyawanDao;

    @Autowired
    private KrsDetailDao krsDetailDao;

    @Autowired
    private KrsDao krsDao;

    @Autowired
    private PresensiMahasiswaDao presensiMahasiswaDao;

    @Autowired
    private TahunAkademikDao tahunAkademikDao;

    @Autowired
    private CurrentUserService currentUserService;

    @GetMapping("/dashboardmahasiswa")
    public String dashboardMahasiswa(Model model, Authentication authentication, RedirectAttributes attributes) {
        User user = currentUserService.currentUser(authentication);

        Mahasiswa mahasiswa = mahasiswaDao.findByUser(user);
        if (mahasiswa.getTerakhirUpdate() == null){
            attributes.addFlashAttribute("profil","lengkapi profil");
            return "redirect:user/form";
        }
        model.addAttribute("datamahasiswa", mahasiswaDao.findByStatusNotInAndUser(StatusRecord.HAPUS, user));

        TahunAkademik ta = tahunAkademikDao.findByStatus(StatusRecord.AKTIF);

        Krs k = krsDao.findByMahasiswaAndTahunAkademik(mahasiswa, ta);

        model.addAttribute("krsdetail", krsDetailDao.findByMahasiswaAndKrsAndStatus(mahasiswa, k, StatusRecord.AKTIF));

        return "dashboardmahasiswa";

    }

        @ResponseBody
        @GetMapping("/dashboardmahasiswa/presensi")
        public List<RekapMissAttendance> rekapMissAttendances(Authentication authentication){

            User user = currentUserService.currentUser(authentication);

            Mahasiswa mahasiswa = mahasiswaDao.findByUser(user);
            TahunAkademik ta=tahunAkademikDao.findByStatus(StatusRecord.AKTIF);
            Krs k=krsDao.findByMahasiswaAndTahunAkademik(mahasiswa,ta);

            return presensiMahasiswaDao.rekapMissAttendance(k);
        }

    @GetMapping("/dashboard")
    public String daftarDashboard(Model model, Authentication authentication){
        User user = currentUserService.currentUser(authentication);

        if (user == null){
            return "dashboard";
        }

        Mahasiswa mahasiswa = mahasiswaDao.findByUser(user);

        TahunAkademik tahunAkademik = tahunAkademikDao.findByStatus(StatusRecord.AKTIF);

        model.addAttribute("JmlKar",karyawanDao.countKaryawanByStatus(StatusRecord.AKTIF));
        model.addAttribute("jmlDosen",dosenDao.countDosenByStatus(StatusRecord.Y));
        model.addAttribute("jmlL",krsDao.countKrsByTahunAkademikAndMahasiswaJenisKelamin(tahunAkademik,JenisKelamin.PRIA));
        model.addAttribute("jmlP",krsDao.countKrsByTahunAkademikAndMahasiswaJenisKelamin(tahunAkademik,JenisKelamin.WANITA));
        model.addAttribute("jmlMhsInA",krsDao.countKrsByTahunAkademikAndMahasiswa(tahunAkademik,mahasiswa));
        model.addAttribute("jmlMhsA" ,krsDao.countKrsByTahunAkademikAndMahasiswaStatus(tahunAkademik,StatusRecord.AKTIF));

        if (Objects.equals(user.getRole().getId(), "mahasiswa")){
            return "redirect:dashboardmahasiswa";
        }else{
            return "dashboard";
        }

    }



    @RequestMapping(value = "/login", method = RequestMethod.GET)
    public String login(Model model) {
        model.addAttribute("loginError", true);
        return "login";
    }

    @GetMapping("/")
    public String formAwal(){
        return "redirect:/dashboard";
    }

    @GetMapping("/404")
    public void form404(){
    }

}
