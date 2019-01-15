package id.ac.tazkia.akademik.aplikasiakademik.controller;

import id.ac.tazkia.akademik.aplikasiakademik.dao.*;
import id.ac.tazkia.akademik.aplikasiakademik.dto.MahasiswaDto;
import id.ac.tazkia.akademik.aplikasiakademik.entity.*;
import id.ac.tazkia.akademik.aplikasiakademik.service.CurrentUserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;

import javax.validation.Valid;

@Controller
public class UserController {

    private static final Logger LOGGER = LoggerFactory.getLogger(UserController.class);


    @Autowired
    private UserPasswordDao userPasswordDao;
    @Autowired
    private MahasiswaDao mahasiswaDao;
    @Autowired
    private MahasiswaDetailKeluargaDao mahasiswaDetailKeluargaDao;
    @Autowired
    private AgamaDao agamaDao;
    @Autowired
    private JenisTinggalDao jenisTinggalDao;
    @Autowired
    private ProgramDao programDao;
    @Autowired
    private ProdiDao prodiDao;
    @Autowired
    private KonsentrasiDao konsentrasiDao;
    @Autowired
    private AyahDao ayahDao;
    @Autowired
    private IbuDao ibuDao;
    @Autowired
    private CurrentUserService currentUserService;

    @Autowired
    private PendidikanDao pendidikanDao;

    @Autowired
    private PekerjaanDao pekerjaanDao;

    @Autowired
    private PenghasilanDao penghasilanDao;

    @Autowired
    private KebutuhanKhususDao kebutuhanKhususDao;

    @Autowired
    private TransportasiDao transportasiDao;

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

    @GetMapping("/user/data")
    public void dataUser(Model model, Authentication authentication) {
        LOGGER.debug("Authentication class : {}", authentication.getClass().getName());

        if (authentication == null) {
            LOGGER.warn("Current user is null");
        }

        User user = currentUserService.currentUser(authentication);

        Mahasiswa mahasiswa = mahasiswaDao.findByUser(user);
        MahasiswaDetailKeluarga detail = mahasiswaDetailKeluargaDao.findByMahasiswa(mahasiswa);
        model.addAttribute("mahasiswa",detail);
        System.out.println(detail.getMahasiswa().getJenisKelamin());


    }

    @GetMapping("/user/form")
    public void formUser(Model model, Authentication authentication) {
        LOGGER.debug("Authentication class : {}", authentication.getClass().getName());

        if (authentication == null) {
            LOGGER.warn("Current user is null");
        }

        User u = currentUserService.currentUser(authentication);

        Mahasiswa m = mahasiswaDao.findByUser(u);


        model.addAttribute("kebutuhan",kebutuhanKhususDao.findAll());
        model.addAttribute("agama",agamaDao.findByStatus(StatusRecord.AKTIF));
        model.addAttribute("tinggal",jenisTinggalDao.findByStatus(StatusRecord.AKTIF));
        model.addAttribute("program",programDao.findByStatus(StatusRecord.AKTIF));
        model.addAttribute("prodi",prodiDao.findByStatus(StatusRecord.AKTIF));
        model.addAttribute("konsentrasi",konsentrasiDao.findByStatus(StatusRecord.AKTIF));
        model.addAttribute("transportasi", transportasiDao.findAll());

        model.addAttribute("mhsw",m);

        MahasiswaDto mahasiswaDto = new MahasiswaDto();
        MahasiswaDetailKeluarga mahasiswa = mahasiswaDetailKeluargaDao.findByMahasiswa(m);

        if (mahasiswa.getWali() != null) {
            UserPassword up = userPasswordDao.findByUser(u);
            mahasiswaDto.setUserPassword(up);
            mahasiswaDto.setId(mahasiswa.getMahasiswa().getId());
            mahasiswaDto.setAngkatan(mahasiswa.getMahasiswa().getAngkatan());
            mahasiswaDto.setIdProdi(mahasiswa.getMahasiswa().getIdProdi());
            mahasiswaDto.setIdKonsentrasi(mahasiswa.getMahasiswa().getIdKonsentrasi());
            mahasiswaDto.setNim(mahasiswa.getMahasiswa().getNim());
            mahasiswaDto.setNama(mahasiswa.getMahasiswa().getNama());
            mahasiswaDto.setStatusMatrikulasi(mahasiswa.getMahasiswa().getStatusMatrikulasi());
            mahasiswaDto.setIdProgram(mahasiswa.getMahasiswa().getIdProgram());
            mahasiswaDto.setJenisKelamin(mahasiswa.getMahasiswa().getJenisKelamin());
            mahasiswaDto.setReligion(mahasiswa.getMahasiswa().getIdAgama());
            mahasiswaDto.setTempat(mahasiswa.getMahasiswa().getTempatLahir());
            mahasiswaDto.setTanggalLahir(mahasiswa.getMahasiswa().getTanggalLahir());
            mahasiswaDto.setIdKelurahan(mahasiswa.getMahasiswa().getIdKelurahan());
            mahasiswaDto.setIdKecamatan(mahasiswa.getMahasiswa().getIdKecamatan());
            mahasiswaDto.setIdKotaKabupaten(mahasiswa.getMahasiswa().getIdKotaKabupaten());
            mahasiswaDto.setIdProvinsi(mahasiswa.getMahasiswa().getIdProvinsi());
            mahasiswaDto.setIdNegara(mahasiswa.getMahasiswa().getIdNegara());
            mahasiswaDto.setKewarganegaraan(mahasiswa.getMahasiswa().getKewarganegaraan());
            mahasiswaDto.setNik(mahasiswa.getMahasiswa().getNik());
            mahasiswaDto.setNisn(mahasiswa.getMahasiswa().getNisn());
            mahasiswaDto.setNamaJalan(mahasiswa.getMahasiswa().getNamaJalan());
            mahasiswaDto.setRt(mahasiswa.getMahasiswa().getRt());
            mahasiswaDto.setRw(mahasiswa.getMahasiswa().getRw());
            mahasiswaDto.setNamaDusun(mahasiswa.getMahasiswa().getNamaDusun());
            mahasiswaDto.setKodepos(mahasiswa.getMahasiswa().getKodepos());
            mahasiswaDto.setJenisTinggal(mahasiswa.getMahasiswa().getJenisTinggal());
            mahasiswaDto.setAlatTransportasi(mahasiswa.getMahasiswa().getAlatTransportasi());
            mahasiswaDto.setTeleponRumah(mahasiswa.getMahasiswa().getTeleponRumah());
            mahasiswaDto.setTeleponSeluler(mahasiswa.getMahasiswa().getTeleponSeluler());
            mahasiswaDto.setEmailPribadi(mahasiswa.getMahasiswa().getEmailPribadi());
            mahasiswaDto.setEmailTazkia(mahasiswa.getMahasiswa().getEmailTazkia());
            mahasiswaDto.setStatusAktif(mahasiswa.getMahasiswa().getStatusAktif());
            mahasiswaDto.setIdUser(mahasiswa.getMahasiswa().getUser());

            mahasiswaDto.setIbu(mahasiswa.getIbu().getId());
            mahasiswaDto.setNamaIbuKandung(mahasiswa.getIbu().getNamaIbuKandung());
            mahasiswaDto.setKebutuhanKhususIbu(mahasiswa.getIbu().getKebutuhanKhusus());
            mahasiswaDto.setTempatLahirIbu(mahasiswa.getIbu().getTempatLahir());
            mahasiswaDto.setTanggalLahirIbu(mahasiswa.getIbu().getTanggalLahir());
            mahasiswaDto.setIdJenjangPendidikanIbu(mahasiswa.getIbu().getIdJenjangPendidikan());
            mahasiswaDto.setIdPekerjaanIbu(mahasiswa.getIbu().getIdPekerjaan());
            mahasiswaDto.setPenghasilanIbu(mahasiswa.getIbu().getPenghasilan());
            mahasiswaDto.setAgamaIbu(mahasiswa.getIbu().getAgama());
            mahasiswaDto.setStatusHidupIbu(mahasiswa.getIbu().getStatusHidup());

            mahasiswaDto.setAyah(mahasiswa.getAyah().getId());
            mahasiswaDto.setNamaAyah(mahasiswa.getAyah().getNamaAyah());
            mahasiswaDto.setKebutuhanKhusus(mahasiswa.getAyah().getKebutuhanKhusus());
            mahasiswaDto.setTempatLahirAyah(mahasiswa.getAyah().getTempatLahir());
            mahasiswaDto.setTanggalLahirAyah(mahasiswa.getAyah().getTanggalLahir());
            mahasiswaDto.setIdJenjangPendidikan(mahasiswa.getAyah().getIdJenjangPendidikan());
            mahasiswaDto.setIdPekerjaan(mahasiswa.getAyah().getIdPekerjaan());
            mahasiswaDto.setPenghasilan(mahasiswa.getAyah().getPenghasilan());
            mahasiswaDto.setAgama(mahasiswa.getAyah().getAgama());
            mahasiswaDto.setHidup(mahasiswa.getAyah().getStatusHidup());

            mahasiswaDto.setWali(mahasiswa.getWali().getId());
            mahasiswaDto.setNamaWali(mahasiswa.getWali().getNamaWali());
            mahasiswaDto.setKebutuhanKhususWali(mahasiswa.getWali().getKebutuhanKhusus());
            mahasiswaDto.setTempatLahirWali(mahasiswa.getWali().getTempatLahir());
            mahasiswaDto.setTanggalLahirWali(mahasiswa.getWali().getTanggalLahir());
            mahasiswaDto.setIdJenjangPendidikanWali(mahasiswa.getWali().getIdJenjangPendidikan());
            mahasiswaDto.setIdPekerjaanWali(mahasiswa.getWali().getIdPekerjaan());
            mahasiswaDto.setIdPenghasilanWali(mahasiswa.getWali().getIdPenghasilan());
            mahasiswaDto.setAgamaWali(mahasiswa.getWali().getAgama());
            model.addAttribute("mahasiswa", mahasiswaDto);
        }
//
        if (mahasiswa.getWali() == null){
            UserPassword up = userPasswordDao.findByUser(u);
            mahasiswaDto.setUserPassword(up);
            mahasiswaDto.setId(mahasiswa.getMahasiswa().getId());
            mahasiswaDto.setAngkatan(mahasiswa.getMahasiswa().getAngkatan());
            mahasiswaDto.setIdProdi(mahasiswa.getMahasiswa().getIdProdi());
            mahasiswaDto.setIdKonsentrasi(mahasiswa.getMahasiswa().getIdKonsentrasi());
            mahasiswaDto.setNim(mahasiswa.getMahasiswa().getNim());
            mahasiswaDto.setNama(mahasiswa.getMahasiswa().getNama());
            mahasiswaDto.setStatusMatrikulasi(mahasiswa.getMahasiswa().getStatusMatrikulasi());
            mahasiswaDto.setIdProgram(mahasiswa.getMahasiswa().getIdProgram());
            mahasiswaDto.setJenisKelamin(mahasiswa.getMahasiswa().getJenisKelamin());
            mahasiswaDto.setReligion(mahasiswa.getMahasiswa().getIdAgama());
            mahasiswaDto.setTempat(mahasiswa.getMahasiswa().getTempatLahir());
            mahasiswaDto.setTanggalLahir(mahasiswa.getMahasiswa().getTanggalLahir());
            mahasiswaDto.setIdKelurahan(mahasiswa.getMahasiswa().getIdKelurahan());
            mahasiswaDto.setIdKecamatan(mahasiswa.getMahasiswa().getIdKecamatan());
            mahasiswaDto.setIdKotaKabupaten(mahasiswa.getMahasiswa().getIdKotaKabupaten());
            mahasiswaDto.setIdProvinsi(mahasiswa.getMahasiswa().getIdProvinsi());
            mahasiswaDto.setIdNegara(mahasiswa.getMahasiswa().getIdNegara());
            mahasiswaDto.setKewarganegaraan(mahasiswa.getMahasiswa().getKewarganegaraan());
            mahasiswaDto.setNik(mahasiswa.getMahasiswa().getNik());
            mahasiswaDto.setNisn(mahasiswa.getMahasiswa().getNisn());
            mahasiswaDto.setNamaJalan(mahasiswa.getMahasiswa().getNamaJalan());
            mahasiswaDto.setRt(mahasiswa.getMahasiswa().getRt());
            mahasiswaDto.setRw(mahasiswa.getMahasiswa().getRw());
            mahasiswaDto.setNamaDusun(mahasiswa.getMahasiswa().getNamaDusun());
            mahasiswaDto.setKodepos(mahasiswa.getMahasiswa().getKodepos());
            mahasiswaDto.setJenisTinggal(mahasiswa.getMahasiswa().getJenisTinggal());
            mahasiswaDto.setAlatTransportasi(mahasiswa.getMahasiswa().getAlatTransportasi());
            mahasiswaDto.setTeleponRumah(mahasiswa.getMahasiswa().getTeleponRumah());
            mahasiswaDto.setTeleponSeluler(mahasiswa.getMahasiswa().getTeleponSeluler());
            mahasiswaDto.setEmailPribadi(mahasiswa.getMahasiswa().getEmailPribadi());
            mahasiswaDto.setEmailTazkia(mahasiswa.getMahasiswa().getEmailTazkia());
            mahasiswaDto.setStatusAktif(mahasiswa.getMahasiswa().getStatusAktif());
            mahasiswaDto.setIdUser(mahasiswa.getMahasiswa().getUser());

            mahasiswaDto.setIbu(mahasiswa.getIbu().getId());
            mahasiswaDto.setNamaIbuKandung(mahasiswa.getIbu().getNamaIbuKandung());
            mahasiswaDto.setKebutuhanKhususIbu(mahasiswa.getIbu().getKebutuhanKhusus());
            mahasiswaDto.setTempatLahirIbu(mahasiswa.getIbu().getTempatLahir());
            mahasiswaDto.setTanggalLahirIbu(mahasiswa.getIbu().getTanggalLahir());
            mahasiswaDto.setIdJenjangPendidikanIbu(mahasiswa.getIbu().getIdJenjangPendidikan());
            mahasiswaDto.setIdPekerjaanIbu(mahasiswa.getIbu().getIdPekerjaan());
            mahasiswaDto.setPenghasilanIbu(mahasiswa.getIbu().getPenghasilan());
            mahasiswaDto.setAgamaIbu(mahasiswa.getIbu().getAgama());
            mahasiswaDto.setStatusHidupIbu(mahasiswa.getIbu().getStatusHidup());

            mahasiswaDto.setAyah(mahasiswa.getAyah().getId());
            mahasiswaDto.setNamaAyah(mahasiswa.getAyah().getNamaAyah());
            mahasiswaDto.setKebutuhanKhusus(mahasiswa.getAyah().getKebutuhanKhusus());
            mahasiswaDto.setTempatLahirAyah(mahasiswa.getAyah().getTempatLahir());
            mahasiswaDto.setTanggalLahirAyah(mahasiswa.getAyah().getTanggalLahir());
            mahasiswaDto.setIdJenjangPendidikan(mahasiswa.getAyah().getIdJenjangPendidikan());
            mahasiswaDto.setIdPekerjaan(mahasiswa.getAyah().getIdPekerjaan());
            mahasiswaDto.setPenghasilan(mahasiswa.getAyah().getPenghasilan());
            mahasiswaDto.setAgama(mahasiswa.getAyah().getAgama());
            mahasiswaDto.setHidup(mahasiswa.getAyah().getStatusHidup());
            model.addAttribute("mahasiswa", mahasiswaDto);
        }

    }

    @PostMapping("/user/form")
    public String prosesUpdateUser(@ModelAttribute @Valid MahasiswaDto mahasiswaDto, BindingResult errors) {

        if(errors.hasErrors()){
            return "form";
        }


        Mahasiswa mahasiswa = mahasiswaDao.findByUser(mahasiswaDto.getIdUser());
        BeanUtils.copyProperties(mahasiswaDto,mahasiswa);
        mahasiswa.setStatus(mahasiswa.getStatus());
        mahasiswa.setStatusAktif("A");
        mahasiswa.setIdAgama(mahasiswaDto.getReligion());
        mahasiswa.setTempatLahir(mahasiswaDto.getTempat());
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
        ibuDao.save(ibu);

        Ayah ayah = ayahDao.findById(mahasiswaDto.getAyah()).get();
        BeanUtils.copyProperties(mahasiswaDto, ayah);
        ayah.setId(mahasiswaDto.getAyah());
        ayah.setTanggalLahir(mahasiswaDto.getTanggalLahirAyah());
        ayah.setTempatLahir(mahasiswaDto.getTempatLahirAyah());
        ayah.setStatusHidup(mahasiswaDto.getHidup());
        ayahDao.save(ayah);

        return "redirect:data";

    }
}
