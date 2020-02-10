package id.ac.tazkia.smilemahasiswa.controller;

import id.ac.tazkia.smilemahasiswa.dao.*;
import id.ac.tazkia.smilemahasiswa.dto.user.ProfileDto;
import id.ac.tazkia.smilemahasiswa.entity.*;
import id.ac.tazkia.smilemahasiswa.service.CurrentUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;

import javax.validation.Valid;
import java.util.Arrays;

@Controller
public class DashboardController {

    @Autowired
    private UserDao userDao;

    @Autowired
    private CurrentUserService currentUserService;

    @Autowired
    private MahasiswaDao mahasiswaDao;

    @Autowired
    private TahunAkademikDao tahunAkademikDao;

    @Autowired
    private KrsDao krsDao;

    @Autowired
    private KrsDetailDao krsDetailDao;

    @Autowired
    private DetailKeluargaDao detailKeluargaDao;

    @Autowired
    private AgamaDao agamaDao;

    @Autowired
    private PenghasilanDao penghasilanDao;

    @Autowired
    private PekerjaanDao pekerjaanDao;

    @Autowired
    private PendidikanDao pendidikanDao;

    @Autowired
    private TransportasiDao transportasiDao;

    @Autowired
    private KebutuhanKhususDao kebutuhanKhususDao;

    @Autowired
    private DosenDao dosenDao;

    @Autowired
    private KaryawanDao karyawanDao;

    @Autowired
    private JadwalDosenDao jadwalDosenDao;

    @ModelAttribute("agama")
    public Iterable<Agama> agama() {
        return agamaDao.findByStatus(StatusRecord.AKTIF);
    }

    @ModelAttribute("penghasilan")
    public Iterable<Penghasilan> penghasilan() {
        return penghasilanDao.findAll();
    }

    @ModelAttribute("pekerjaan")
    public Iterable<Pekerjaan> pekerjaan() {
        return pekerjaanDao.findAll();
    }

    @ModelAttribute("pendidikan")
    public Iterable<Pendidikan> pendidikan() {
        return pendidikanDao.findAll();
    }

    @GetMapping("/dashboard")
    public String dashboardUtama(Model model, Authentication authentication){
        User user = currentUserService.currentUser(authentication);
        Mahasiswa mahasiswa = mahasiswaDao.findByUser(user);

        if (mahasiswa == null){
            return "redirect:admin";
        }

        TahunAkademik ta = tahunAkademikDao.findByStatus(StatusRecord.AKTIF);

        Krs k = krsDao.findByMahasiswaAndTahunAkademikAndStatus(mahasiswa, ta,StatusRecord.AKTIF);

        model.addAttribute("krsdetail", krsDetailDao.findByMahasiswaAndKrsAndStatus(mahasiswa, k, StatusRecord.AKTIF));
        model.addAttribute("persentase", krsDetailDao.persentaseKehadiran(mahasiswa,ta));


        return "dashboard";
    }

    @GetMapping("/")
    public String formAwal(){
        return "redirect:/dashboard";
    }

    @GetMapping("/login")
    public void login(){

    }

    @GetMapping("/user/profile")
    public void UserProfile(Model model, Authentication authentication){
        User user = currentUserService.currentUser(authentication);
        Mahasiswa mahasiswa = mahasiswaDao.findByUser(user);
        model.addAttribute("mhsw", mahasiswa);
        model.addAttribute("mahasiswa", detailKeluargaDao.userProfile(mahasiswa));
        model.addAttribute("transportasi", transportasiDao.findAll());
        model.addAttribute("kebutuhan",kebutuhanKhususDao.findAll());


    }

    @PostMapping("/user/profile")
    public String prosesUser(@ModelAttribute @Valid ProfileDto profileDto){


        return null;
    }

    @GetMapping("/admin")
    public String adminDashboard(Model model,Authentication authentication){
        TahunAkademik tahunAkademik = tahunAkademikDao.findByStatus(StatusRecord.AKTIF);

        model.addAttribute("jmlDosen",dosenDao.countDosenByStatus(StatusRecord.AKTIF));
        model.addAttribute("jmlMhsA" ,krsDao.countKrsByTahunAkademikAndMahasiswaStatus(tahunAkademik,StatusRecord.AKTIF));
        model.addAttribute("jmlL",krsDao.countKrsByTahunAkademikAndMahasiswaJenisKelamin(tahunAkademik,JenisKelamin.PRIA));
        model.addAttribute("jmlP",krsDao.countKrsByTahunAkademikAndMahasiswaJenisKelamin(tahunAkademik,JenisKelamin.WANITA));

        User user = currentUserService.currentUser(authentication);
        Karyawan karyawan = karyawanDao.findByIdUser(user);
        Dosen dosen = dosenDao.findByKaryawan(karyawan);

        Iterable<JadwalDosen> jadwal = jadwalDosenDao.findByJadwalStatusNotInAndJadwalTahunAkademikAndDosenAndJadwalHariNotNullAndJadwalKelasNotNull(Arrays.asList(StatusRecord.HAPUS), tahunAkademik,dosen);
        model.addAttribute("jadwal", jadwal);



        return "dashboardadmin";
    }

}
