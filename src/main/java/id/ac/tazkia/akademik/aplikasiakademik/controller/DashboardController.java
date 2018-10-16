package id.ac.tazkia.akademik.aplikasiakademik.controller;

import id.ac.tazkia.akademik.aplikasiakademik.dao.*;
import id.ac.tazkia.akademik.aplikasiakademik.dto.RekapMissAttendance;
import id.ac.tazkia.akademik.aplikasiakademik.entity.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;
import java.util.Objects;

@Controller
//@RequestMapping("/dashboardmahasiswa")
public class DashboardController {

    @Autowired
    private UserDao userDao;

    @Autowired
    private MahasiswaDao mahasiswaDao;

    @Autowired
    private ProdiDao prodiDao;

    @Autowired
    private JurusanDao jurusanDao;

    @Autowired
    private FakultasDao fakultasDao;

    @Autowired
    private KaryawanDao karyawanDao;

    @Autowired
    private KrsDetailDao krsDetailDao;

    @Autowired
    private JadwalDao jadwalDao;

    @Autowired
    private KrsDao krsDao;

    @Autowired
    private PresensiMahasiswaDao presensiMahasiswaDao;

    @Autowired
    private TahunAkademikDao tahunAkademikDao;

    @Autowired
    private TahunAkademikProdiDao tahunAkademikProdiDao;

    @Autowired
    private MatakuliahKurikulumDao matakuliahKurikulumDao;

    @Autowired
    private MataKuliahDao mataKuliahDao;

    @GetMapping("/dashboardmahasiswa")
    public void dashboardMahasiswa(Model model, Authentication currentUser) {
        String username = ((UserDetails) currentUser.getPrincipal()).getUsername();
        User u = userDao.findByUsername(username);
        Mahasiswa mahasiswa = mahasiswaDao.findByUser(u);
        model.addAttribute("datamahasiswa", mahasiswaDao.findByStatusNotInAndUser(StatusRecord.HAPUS, u));

        // Prodi pi=prodiDao.findById(mahasiswa.getIdProdi().getId()).get();
        TahunAkademik ta = tahunAkademikDao.findByStatus(StatusRecord.AKTIF);

        Krs k = krsDao.findByMahasiswaAndTahunAkademik(mahasiswa, ta);
        // model.addAttribute("presensimahasiswa",presensiMahasiswaDao.findByKrsDetailIdKrsIdAndStatus(k,StatusRecord.AKTIF,page));
        model.addAttribute("krsdetail", krsDetailDao.findByMahasiswaAndKrsAndStatus(mahasiswa, k, StatusRecord.AKTIF));

    }

        @ResponseBody
        @GetMapping("/dashboardmahasiswa/presensi")
        public List<RekapMissAttendance> rekapMissAttendances(Authentication currentUser){

            String username = ((UserDetails) currentUser.getPrincipal()).getUsername();
            User u=userDao.findByUsername(username);
            Mahasiswa mahasiswa=mahasiswaDao.findByUser(u);
            TahunAkademik ta=tahunAkademikDao.findByStatus(StatusRecord.AKTIF);
            Krs k=krsDao.findByMahasiswaAndTahunAkademik(mahasiswa,ta);

            return presensiMahasiswaDao.rekapMissAttendance(k);
        }

       // Page<PresensiMahasiswa> hasil=presensiMahasiswaDao.findByKrsDetailIdKrsIdAndStatus(k,StatusRecord.AKTIF,page);
       // System.out.println("data jadwal : " + hasil);




    @GetMapping("/dashboard")
    public String daftarDashboard(Model model, Authentication currentUser){
        String username = ((UserDetails) currentUser.getPrincipal()).getUsername();
        User u=userDao.findByUsername(username);
        if (Objects.equals(u.getRole().getId(), "mahasiswa")){
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
        return "redirect:/login";
    }

}
