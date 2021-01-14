package id.ac.tazkia.smilemahasiswa.controller;

import id.ac.tazkia.smilemahasiswa.dao.*;
import id.ac.tazkia.smilemahasiswa.dto.user.MahasiswaDto;
import id.ac.tazkia.smilemahasiswa.dto.user.ProfileDto;
import id.ac.tazkia.smilemahasiswa.entity.*;
import id.ac.tazkia.smilemahasiswa.service.CurrentUserService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import javax.script.Bindings;
import javax.validation.Valid;
import java.time.LocalDate;
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
    private AyahDao ayahDao;

    @Autowired
    private MahasiswaDetailKeluargaDao mahasiswaDetailKeluargaDao;

    @Autowired
    private IbuDao ibuDao;

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
        model.addAttribute("transportasi", transportasiDao.findAll());
        model.addAttribute("kebutuhan",kebutuhanKhususDao.findAll());


        model.addAttribute("agama",agamaDao.findByStatus(StatusRecord.AKTIF));

        model.addAttribute("mhsw",mahasiswa);
        MahasiswaDetailKeluarga mahasiswaDetailKeluarga = mahasiswaDetailKeluargaDao.findByMahasiswa(mahasiswa);
        MahasiswaDto mahasiswaDto = new MahasiswaDto();
        if (mahasiswaDetailKeluarga.getWali() != null) {
            mahasiswaDto.setId(mahasiswaDetailKeluarga.getMahasiswa().getId());
            mahasiswaDto.setAngkatan(mahasiswaDetailKeluarga.getMahasiswa().getAngkatan());
            mahasiswaDto.setIdProdi(mahasiswaDetailKeluarga.getMahasiswa().getIdProdi());
            mahasiswaDto.setIdKonsentrasi(mahasiswaDetailKeluarga.getMahasiswa().getIdKonsentrasi());
            mahasiswaDto.setNim(mahasiswaDetailKeluarga.getMahasiswa().getNim());
            mahasiswaDto.setNama(mahasiswaDetailKeluarga.getMahasiswa().getNama());
            mahasiswaDto.setStatusMatrikulasi(mahasiswaDetailKeluarga.getMahasiswa().getStatusMatrikulasi());
            mahasiswaDto.setIdProgram(mahasiswaDetailKeluarga.getMahasiswa().getIdProgram());
            mahasiswaDto.setJenisKelamin(mahasiswaDetailKeluarga.getMahasiswa().getJenisKelamin());
            mahasiswaDto.setReligion(mahasiswaDetailKeluarga.getMahasiswa().getIdAgama());
            mahasiswaDto.setTempat(mahasiswaDetailKeluarga.getMahasiswa().getTempatLahir());
            mahasiswaDto.setTanggalLahir(mahasiswaDetailKeluarga.getMahasiswa().getTanggalLahir());
            mahasiswaDto.setIdKelurahan(mahasiswaDetailKeluarga.getMahasiswa().getIdKelurahan());
            mahasiswaDto.setIdKecamatan(mahasiswaDetailKeluarga.getMahasiswa().getIdKecamatan());
            mahasiswaDto.setIdKotaKabupaten(mahasiswaDetailKeluarga.getMahasiswa().getIdKotaKabupaten());
            mahasiswaDto.setIdProvinsi(mahasiswaDetailKeluarga.getMahasiswa().getIdProvinsi());
            mahasiswaDto.setIdNegara(mahasiswaDetailKeluarga.getMahasiswa().getIdNegara());
            mahasiswaDto.setKewarganegaraan(mahasiswaDetailKeluarga.getMahasiswa().getKewarganegaraan());
            mahasiswaDto.setNik(mahasiswaDetailKeluarga.getMahasiswa().getNik());
            mahasiswaDto.setNisn(mahasiswaDetailKeluarga.getMahasiswa().getNisn());
            mahasiswaDto.setNamaJalan(mahasiswaDetailKeluarga.getMahasiswa().getNamaJalan());
            mahasiswaDto.setRt(mahasiswaDetailKeluarga.getMahasiswa().getRt());
            mahasiswaDto.setRw(mahasiswaDetailKeluarga.getMahasiswa().getRw());
            mahasiswaDto.setNamaDusun(mahasiswaDetailKeluarga.getMahasiswa().getNamaDusun());
            mahasiswaDto.setKodepos(mahasiswaDetailKeluarga.getMahasiswa().getKodepos());
            mahasiswaDto.setJenisTinggal(mahasiswaDetailKeluarga.getMahasiswa().getJenisTinggal());
            mahasiswaDto.setAlatTransportasi(mahasiswaDetailKeluarga.getMahasiswa().getAlatTransportasi());
            mahasiswaDto.setTeleponRumah(mahasiswaDetailKeluarga.getMahasiswa().getTeleponRumah());
            mahasiswaDto.setTeleponSeluler(mahasiswaDetailKeluarga.getMahasiswa().getTeleponSeluler());
            mahasiswaDto.setEmailPribadi(mahasiswaDetailKeluarga.getMahasiswa().getEmailPribadi());
            mahasiswaDto.setEmailTazkia(mahasiswaDetailKeluarga.getMahasiswa().getEmailTazkia());
            mahasiswaDto.setStatusAktif(mahasiswaDetailKeluarga.getMahasiswa().getStatusAktif());
            mahasiswaDto.setIdUser(mahasiswaDetailKeluarga.getMahasiswa().getUser());
            mahasiswaDto.setRfid(mahasiswa.getRfid());

            mahasiswaDto.setIbu(mahasiswaDetailKeluarga.getIbu().getId());
            mahasiswaDto.setNamaIbuKandung(mahasiswaDetailKeluarga.getIbu().getNamaIbuKandung());
            mahasiswaDto.setNikIbu(mahasiswaDetailKeluarga.getIbu().getNik());
            mahasiswaDto.setKebutuhanKhususIbu(mahasiswaDetailKeluarga.getIbu().getKebutuhanKhusus());
            mahasiswaDto.setTempatLahirIbu(mahasiswaDetailKeluarga.getIbu().getTempatLahir());
            mahasiswaDto.setTanggalLahirIbu(mahasiswaDetailKeluarga.getIbu().getTanggalLahir());
            mahasiswaDto.setIdJenjangPendidikanIbu(mahasiswaDetailKeluarga.getIbu().getIdJenjangPendidikan());
            mahasiswaDto.setIdPekerjaanIbu(mahasiswaDetailKeluarga.getIbu().getIdPekerjaan());
            mahasiswaDto.setPenghasilanIbu(mahasiswaDetailKeluarga.getIbu().getPenghasilan());
            mahasiswaDto.setAgamaIbu(mahasiswaDetailKeluarga.getIbu().getAgama());
            mahasiswaDto.setStatusHidupIbu(mahasiswaDetailKeluarga.getIbu().getStatusHidup());

            mahasiswaDto.setAyah(mahasiswaDetailKeluarga.getAyah().getId());
            mahasiswaDto.setNamaAyah(mahasiswaDetailKeluarga.getAyah().getNamaAyah());
            mahasiswaDto.setNikAyah(mahasiswaDetailKeluarga.getAyah().getNik());
            mahasiswaDto.setKebutuhanKhusus(mahasiswaDetailKeluarga.getAyah().getKebutuhanKhusus());
            mahasiswaDto.setTempatLahirAyah(mahasiswaDetailKeluarga.getAyah().getTempatLahir());
            mahasiswaDto.setTanggalLahirAyah(mahasiswaDetailKeluarga.getAyah().getTanggalLahir());
            mahasiswaDto.setIdJenjangPendidikan(mahasiswaDetailKeluarga.getAyah().getIdJenjangPendidikan());
            mahasiswaDto.setIdPekerjaan(mahasiswaDetailKeluarga.getAyah().getIdPekerjaan());
            mahasiswaDto.setPenghasilan(mahasiswaDetailKeluarga.getAyah().getPenghasilan());
            mahasiswaDto.setAgama(mahasiswaDetailKeluarga.getAyah().getAgama());
            mahasiswaDto.setHidup(mahasiswaDetailKeluarga.getAyah().getStatusHidup());

            mahasiswaDto.setWali(mahasiswaDetailKeluarga.getWali().getId());
            mahasiswaDto.setNamaWali(mahasiswaDetailKeluarga.getWali().getNamaWali());
            mahasiswaDto.setKebutuhanKhususWali(mahasiswaDetailKeluarga.getWali().getKebutuhanKhusus());
            mahasiswaDto.setTempatLahirWali(mahasiswaDetailKeluarga.getWali().getTempatLahir());
            mahasiswaDto.setTanggalLahirWali(mahasiswaDetailKeluarga.getWali().getTanggalLahir());
            mahasiswaDto.setIdJenjangPendidikanWali(mahasiswaDetailKeluarga.getWali().getIdJenjangPendidikan());
            mahasiswaDto.setIdPekerjaanWali(mahasiswaDetailKeluarga.getWali().getIdPekerjaan());
            mahasiswaDto.setIdPenghasilanWali(mahasiswaDetailKeluarga.getWali().getIdPenghasilan());
            mahasiswaDto.setAgamaWali(mahasiswaDetailKeluarga.getWali().getAgama());
            mahasiswaDto.setUkuranBaju(mahasiswaDetailKeluarga.getMahasiswa().getUkuranBaju());
            mahasiswaDto.setKps(mahasiswaDetailKeluarga.getMahasiswa().getKps());
            mahasiswaDto.setNomorKps(mahasiswaDetailKeluarga.getMahasiswa().getNomorKps());
            model.addAttribute("mahasiswa", mahasiswaDto);
        }

        if (mahasiswaDetailKeluarga.getWali() == null){
            mahasiswaDto.setId(mahasiswaDetailKeluarga.getMahasiswa().getId());
            mahasiswaDto.setAngkatan(mahasiswaDetailKeluarga.getMahasiswa().getAngkatan());
            mahasiswaDto.setIdProdi(mahasiswaDetailKeluarga.getMahasiswa().getIdProdi());
            mahasiswaDto.setIdKonsentrasi(mahasiswaDetailKeluarga.getMahasiswa().getIdKonsentrasi());
            mahasiswaDto.setNim(mahasiswaDetailKeluarga.getMahasiswa().getNim());
            mahasiswaDto.setNama(mahasiswaDetailKeluarga.getMahasiswa().getNama());
            mahasiswaDto.setStatusMatrikulasi(mahasiswaDetailKeluarga.getMahasiswa().getStatusMatrikulasi());
            mahasiswaDto.setUkuranBaju(mahasiswaDetailKeluarga.getMahasiswa().getUkuranBaju());
            mahasiswaDto.setKps(mahasiswaDetailKeluarga.getMahasiswa().getKps());
            mahasiswaDto.setNomorKps(mahasiswaDetailKeluarga.getMahasiswa().getNomorKps());
            mahasiswaDto.setIdProgram(mahasiswaDetailKeluarga.getMahasiswa().getIdProgram());
            mahasiswaDto.setJenisKelamin(mahasiswaDetailKeluarga.getMahasiswa().getJenisKelamin());
            mahasiswaDto.setReligion(mahasiswaDetailKeluarga.getMahasiswa().getIdAgama());
            mahasiswaDto.setTempat(mahasiswaDetailKeluarga.getMahasiswa().getTempatLahir());
            mahasiswaDto.setTanggalLahir(mahasiswaDetailKeluarga.getMahasiswa().getTanggalLahir());
            mahasiswaDto.setIdKelurahan(mahasiswaDetailKeluarga.getMahasiswa().getIdKelurahan());
            mahasiswaDto.setIdKecamatan(mahasiswaDetailKeluarga.getMahasiswa().getIdKecamatan());
            mahasiswaDto.setIdKotaKabupaten(mahasiswaDetailKeluarga.getMahasiswa().getIdKotaKabupaten());
            mahasiswaDto.setIdProvinsi(mahasiswaDetailKeluarga.getMahasiswa().getIdProvinsi());
            mahasiswaDto.setIdNegara(mahasiswaDetailKeluarga.getMahasiswa().getIdNegara());
            mahasiswaDto.setKewarganegaraan(mahasiswaDetailKeluarga.getMahasiswa().getKewarganegaraan());
            mahasiswaDto.setNik(mahasiswaDetailKeluarga.getMahasiswa().getNik());
            mahasiswaDto.setNisn(mahasiswaDetailKeluarga.getMahasiswa().getNisn());
            mahasiswaDto.setNamaJalan(mahasiswaDetailKeluarga.getMahasiswa().getNamaJalan());
            mahasiswaDto.setRt(mahasiswaDetailKeluarga.getMahasiswa().getRt());
            mahasiswaDto.setRw(mahasiswaDetailKeluarga.getMahasiswa().getRw());
            mahasiswaDto.setNamaDusun(mahasiswaDetailKeluarga.getMahasiswa().getNamaDusun());
            mahasiswaDto.setKodepos(mahasiswaDetailKeluarga.getMahasiswa().getKodepos());
            mahasiswaDto.setJenisTinggal(mahasiswaDetailKeluarga.getMahasiswa().getJenisTinggal());
            mahasiswaDto.setAlatTransportasi(mahasiswaDetailKeluarga.getMahasiswa().getAlatTransportasi());
            mahasiswaDto.setTeleponRumah(mahasiswaDetailKeluarga.getMahasiswa().getTeleponRumah());
            mahasiswaDto.setTeleponSeluler(mahasiswaDetailKeluarga.getMahasiswa().getTeleponSeluler());
            mahasiswaDto.setEmailPribadi(mahasiswaDetailKeluarga.getMahasiswa().getEmailPribadi());
            mahasiswaDto.setEmailTazkia(mahasiswaDetailKeluarga.getMahasiswa().getEmailTazkia());
            mahasiswaDto.setStatusAktif(mahasiswaDetailKeluarga.getMahasiswa().getStatusAktif());
            mahasiswaDto.setIdUser(mahasiswaDetailKeluarga.getMahasiswa().getUser());

            mahasiswaDto.setIbu(mahasiswaDetailKeluarga.getIbu().getId());
            mahasiswaDto.setNamaIbuKandung(mahasiswaDetailKeluarga.getIbu().getNamaIbuKandung());
            mahasiswaDto.setKebutuhanKhususIbu(mahasiswaDetailKeluarga.getIbu().getKebutuhanKhusus());
            mahasiswaDto.setTempatLahirIbu(mahasiswaDetailKeluarga.getIbu().getTempatLahir());
            mahasiswaDto.setTanggalLahirIbu(mahasiswaDetailKeluarga.getIbu().getTanggalLahir());
            mahasiswaDto.setIdJenjangPendidikanIbu(mahasiswaDetailKeluarga.getIbu().getIdJenjangPendidikan());
            mahasiswaDto.setIdPekerjaanIbu(mahasiswaDetailKeluarga.getIbu().getIdPekerjaan());
            mahasiswaDto.setPenghasilanIbu(mahasiswaDetailKeluarga.getIbu().getPenghasilan());
            mahasiswaDto.setAgamaIbu(mahasiswaDetailKeluarga.getIbu().getAgama());
            mahasiswaDto.setStatusHidupIbu(mahasiswaDetailKeluarga.getIbu().getStatusHidup());

            mahasiswaDto.setAyah(mahasiswaDetailKeluarga.getAyah().getId());
            mahasiswaDto.setNamaAyah(mahasiswaDetailKeluarga.getAyah().getNamaAyah());
            mahasiswaDto.setKebutuhanKhusus(mahasiswaDetailKeluarga.getAyah().getKebutuhanKhusus());
            mahasiswaDto.setTempatLahirAyah(mahasiswaDetailKeluarga.getAyah().getTempatLahir());
            mahasiswaDto.setTanggalLahirAyah(mahasiswaDetailKeluarga.getAyah().getTanggalLahir());
            mahasiswaDto.setIdJenjangPendidikan(mahasiswaDetailKeluarga.getAyah().getIdJenjangPendidikan());
            mahasiswaDto.setIdPekerjaan(mahasiswaDetailKeluarga.getAyah().getIdPekerjaan());
            mahasiswaDto.setPenghasilan(mahasiswaDetailKeluarga.getAyah().getPenghasilan());
            mahasiswaDto.setAgama(mahasiswaDetailKeluarga.getAyah().getAgama());
            mahasiswaDto.setHidup(mahasiswaDetailKeluarga.getAyah().getStatusHidup());
            model.addAttribute("mahasiswa", mahasiswaDto);
            mahasiswaDto.setRfid(mahasiswa.getRfid());
        }
        model.addAttribute("mahasiswa", mahasiswaDto);


    }

    @PostMapping("/user/profile")
    public String prosesUser(@ModelAttribute @Valid MahasiswaDto mahasiswaDto, BindingResult result){


        Mahasiswa mahasiswa = mahasiswaDao.findByUser(mahasiswaDto.getIdUser());
        mahasiswa.setStatus(mahasiswa.getStatus());
        mahasiswa.setStatusAktif("A");
        mahasiswa.setIdAgama(mahasiswaDto.getAgama());
        mahasiswa.setEmailPribadi(mahasiswaDto.getEmailPribadi());
        mahasiswa.setEmailTazkia(mahasiswaDto.getEmailTazkia());
        mahasiswa.setUkuranBaju(mahasiswaDto.getUkuranBaju());
        mahasiswa.setTeleponSeluler(mahasiswaDto.getTeleponSeluler());
        mahasiswa.setTeleponRumah(mahasiswaDto.getTeleponRumah());
        mahasiswa.setKewarganegaraan(mahasiswaDto.getKewarganegaraan());
        mahasiswa.setAlatTransportasi(mahasiswaDto.getAlatTransportasi());
        mahasiswa.setTempatLahir(mahasiswaDto.getTempat());
        mahasiswa.setIdNegara(mahasiswaDto.getIdNegara());
        mahasiswa.setNamaJalan(mahasiswaDto.getNamaJalan());
        mahasiswa.setTerakhirUpdate(LocalDate.now());
        mahasiswaDao.save(mahasiswa);

        Ibu ibu = ibuDao.findById(mahasiswaDto.getIbu()).get();
        ibu.setId(mahasiswaDto.getIbu());
        ibu.setNamaIbuKandung(mahasiswaDto.getNamaIbuKandung());
        ibu.setKebutuhanKhusus(mahasiswaDto.getKebutuhanKhususIbu());
        ibu.setTempatLahir(mahasiswaDto.getTempatLahirIbu());
        ibu.setTanggalLahir(mahasiswaDto.getTanggalLahirIbu());
        ibu.setIdJenjangPendidikan(mahasiswaDto.getIdJenjangPendidikanIbu());
        ibu.setIdPekerjaan(mahasiswaDto.getIdPekerjaanIbu());
        ibu.setPenghasilan(mahasiswaDto.getPenghasilanIbu());
        ibu.setAgama(mahasiswaDto.getAgamaIbu());
        ibu.setStatusHidup(mahasiswaDto.getStatusHidupIbu());
        ibu.setNik(mahasiswaDto.getNikIbu());
        ibuDao.save(ibu);

        Ayah ayah = ayahDao.findById(mahasiswaDto.getAyah()).get();
        BeanUtils.copyProperties(mahasiswaDto, ayah);
        ayah.setId(mahasiswaDto.getAyah());
        ayah.setTanggalLahir(mahasiswaDto.getTanggalLahirAyah());
        ayah.setTempatLahir(mahasiswaDto.getTempatLahirAyah());
        ayah.setStatusHidup(mahasiswaDto.getHidup());
        ayah.setNik(mahasiswaDto.getNikAyah());
        ayahDao.save(ayah);
        return "redirect:profile";
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