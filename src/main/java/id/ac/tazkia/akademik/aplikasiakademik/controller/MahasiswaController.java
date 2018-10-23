package id.ac.tazkia.akademik.aplikasiakademik.controller;

import id.ac.tazkia.akademik.aplikasiakademik.dao.*;
import id.ac.tazkia.akademik.aplikasiakademik.dto.MahasiswaDto;
import id.ac.tazkia.akademik.aplikasiakademik.entity.*;
import id.ac.tazkia.akademik.aplikasiakademik.service.MahasiswaService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.StringUtils;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

@Controller
public class MahasiswaController {
    @Autowired
    private MahasiswaDao mahasiswaDao;

    @Autowired
    private KodeposDao kodeposDao;

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
    private MahasiswaService mahasiswaService;

    @Autowired
    private AyahDao ayahDao;

    @Autowired
    private WaliDao waliDao;

    @Autowired
    private IbuDao ibuDao;

    @Autowired
    private UserDao userDao;

    @Autowired
    private RoleDao roleDao;



    @GetMapping("/api/kelurahan")
    @ResponseBody
    public Page<Kodepos> cariData(@RequestParam(required = false) String search, Pageable page){
        if(!StringUtils.hasText(search)) {
            return kodeposDao.findAll(page);
        }
        return kodeposDao.findByKelurahanContainingIgnoreCaseOrKabupatenContainingIgnoreCaseOrKecamatanContainingIgnoreCaseOrPropinsiContainingIgnoreCase(search,search,search,search,page);

    }

    @GetMapping("/mahasiswa/list")
    public void daftarMahasiswa(Model model, @PageableDefault(size = 10) Pageable page, String search){
        if (StringUtils.hasText(search)) {
            model.addAttribute("search", search);
            model.addAttribute("list", mahasiswaDao.findByStatusNotInAndNamaContainingIgnoreCaseOrNimContainingIgnoreCaseOrderByNama(StatusRecord.HAPUS, search,search, page));
        } else {
            model.addAttribute("list",mahasiswaDao.findByStatusNotIn(StatusRecord.HAPUS,page));

        }
    }

    @GetMapping("/mahasiswa/form")
    public void  formMahasiswa(Model model,Mahasiswa mahasiswa){
        model.addAttribute("agama",agamaDao.findByStatus(StatusRecord.AKTIF));
        model.addAttribute("tinggal",jenisTinggalDao.findByStatus(StatusRecord.AKTIF));
        model.addAttribute("program",programDao.findByStatus(StatusRecord.AKTIF));
        model.addAttribute("prodi",prodiDao.findByStatus(StatusRecord.AKTIF));
        model.addAttribute("konsentrasi",konsentrasiDao.findByStatus(StatusRecord.AKTIF));

        model.addAttribute("mhsw",mahasiswa);

        MahasiswaDto mahasiswaDto = new MahasiswaDto();

        if (mahasiswa.getWali() != null) {
            mahasiswaDto.setId(mahasiswa.getId());
            mahasiswaDto.setAngkatan(mahasiswa.getAngkatan());
            mahasiswaDto.setIdProdi(mahasiswa.getIdProdi());
            mahasiswaDto.setIdKonsentrasi(mahasiswa.getIdKonsentrasi());
            mahasiswaDto.setNim(mahasiswa.getNim());
            mahasiswaDto.setNama(mahasiswa.getNama());
            mahasiswaDto.setStatusMatrikulasi(mahasiswa.getStatusMatrikulasi());
            mahasiswaDto.setIdProgram(mahasiswa.getIdProgram());
            mahasiswaDto.setJenisKelamin(mahasiswa.getJenisKelamin());
            mahasiswaDto.setReligion(mahasiswa.getIdAgama());
            mahasiswaDto.setTempat(mahasiswa.getTempatLahir());
            mahasiswaDto.setTanggalLahir(mahasiswa.getTanggalLahir());
            mahasiswaDto.setIdKelurahan(mahasiswa.getIdKelurahan());
            mahasiswaDto.setIdKecamatan(mahasiswa.getIdKecamatan());
            mahasiswaDto.setIdKotaKabupaten(mahasiswa.getIdKotaKabupaten());
            mahasiswaDto.setIdProvinsi(mahasiswa.getIdProvinsi());
            mahasiswaDto.setIdNegara(mahasiswa.getIdNegara());
            mahasiswaDto.setKewarganegaraan(mahasiswa.getKewarganegaraan());
            mahasiswaDto.setNik(mahasiswa.getNik());
            mahasiswaDto.setNisn(mahasiswa.getNisn());
            mahasiswaDto.setNamaJalan(mahasiswa.getNamaJalan());
            mahasiswaDto.setRt(mahasiswa.getRt());
            mahasiswaDto.setRw(mahasiswa.getRw());
            mahasiswaDto.setNamaDusun(mahasiswa.getNamaDusun());
            mahasiswaDto.setKodepos(mahasiswa.getKodepos());
            mahasiswaDto.setJenisTinggal(mahasiswa.getJenisTinggal());
            mahasiswaDto.setAlatTransportasi(mahasiswa.getAlatTransportasi());
            mahasiswaDto.setTeleponRumah(mahasiswa.getTeleponRumah());
            mahasiswaDto.setTeleponSeluler(mahasiswa.getTeleponSeluler());
            mahasiswaDto.setEmailPribadi(mahasiswa.getEmailPribadi());
            mahasiswaDto.setEmailTazkia(mahasiswa.getEmailTazkia());
            mahasiswaDto.setStatusAktif(mahasiswa.getStatusAktif());
            mahasiswaDto.setIdUser(mahasiswa.getUser());

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

        if (mahasiswa.getWali() == null){
            mahasiswaDto.setId(mahasiswa.getId());
            mahasiswaDto.setAngkatan(mahasiswa.getAngkatan());
            mahasiswaDto.setIdProdi(mahasiswa.getIdProdi());
            mahasiswaDto.setIdKonsentrasi(mahasiswa.getIdKonsentrasi());
            mahasiswaDto.setNim(mahasiswa.getNim());
            mahasiswaDto.setNama(mahasiswa.getNama());
            mahasiswaDto.setStatusMatrikulasi(mahasiswa.getStatusMatrikulasi());
            mahasiswaDto.setIdProgram(mahasiswa.getIdProgram());
            mahasiswaDto.setJenisKelamin(mahasiswa.getJenisKelamin());
            mahasiswaDto.setReligion(mahasiswa.getIdAgama());
            mahasiswaDto.setTempat(mahasiswa.getTempatLahir());
            mahasiswaDto.setTanggalLahir(mahasiswa.getTanggalLahir());
            mahasiswaDto.setIdKelurahan(mahasiswa.getIdKelurahan());
            mahasiswaDto.setIdKecamatan(mahasiswa.getIdKecamatan());
            mahasiswaDto.setIdKotaKabupaten(mahasiswa.getIdKotaKabupaten());
            mahasiswaDto.setIdProvinsi(mahasiswa.getIdProvinsi());
            mahasiswaDto.setIdNegara(mahasiswa.getIdNegara());
            mahasiswaDto.setKewarganegaraan(mahasiswa.getKewarganegaraan());
            mahasiswaDto.setNik(mahasiswa.getNik());
            mahasiswaDto.setNisn(mahasiswa.getNisn());
            mahasiswaDto.setNamaJalan(mahasiswa.getNamaJalan());
            mahasiswaDto.setRt(mahasiswa.getRt());
            mahasiswaDto.setRw(mahasiswa.getRw());
            mahasiswaDto.setNamaDusun(mahasiswa.getNamaDusun());
            mahasiswaDto.setKodepos(mahasiswa.getKodepos());
            mahasiswaDto.setJenisTinggal(mahasiswa.getJenisTinggal());
            mahasiswaDto.setAlatTransportasi(mahasiswa.getAlatTransportasi());
            mahasiswaDto.setTeleponRumah(mahasiswa.getTeleponRumah());
            mahasiswaDto.setTeleponSeluler(mahasiswa.getTeleponSeluler());
            mahasiswaDto.setEmailPribadi(mahasiswa.getEmailPribadi());
            mahasiswaDto.setEmailTazkia(mahasiswa.getEmailTazkia());
            mahasiswaDto.setStatusAktif(mahasiswa.getStatusAktif());
            mahasiswaDto.setIdUser(mahasiswa.getUser());

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
                model.addAttribute("mahasiswa", mahasiswaDto);

    }

    @PostMapping("/mahasiswa/form")
    public String prosesForm(@RequestParam Mahasiswa mahasiswa, @ModelAttribute @Valid MahasiswaDto mahasiswaDto, BindingResult errors){

        if(errors.hasErrors()){
            return "form";
        }

        BeanUtils.copyProperties(mahasiswaDto,mahasiswa);
        mahasiswa.setTempatLahir(mahasiswaDto.getTempat());
        mahasiswa.setIdAgama(mahasiswaDto.getReligion());
        mahasiswaDao.save(mahasiswa);

        if (mahasiswa.getUser() != null) {
            User user = userDao.findById(mahasiswa.getUser().getId()).get();
            Role role = roleDao.findById(user.getRole().getId()).get();
            user.setId(mahasiswaDto.getIdUser().getId());
            user.setUsername(mahasiswa.getNim());
            user.setRole(role);
            user.setActive(Boolean.TRUE);
            userDao.save(user);
        }

        if (mahasiswaDto.getIbu() != null){
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
        }
        if (mahasiswaDto.getAyah() != null){
            Ayah ayah = ayahDao.findById(mahasiswaDto.getAyah()).get();
            BeanUtils.copyProperties(mahasiswaDto, ayah);
            ayah.setId(mahasiswaDto.getAyah());
            ayah.setTanggalLahir(mahasiswaDto.getTanggalLahirAyah());
            ayah.setTempatLahir(mahasiswaDto.getTempatLahirAyah());
            ayah.setStatusHidup(mahasiswaDto.getHidup());
            ayahDao.save(ayah);
        }
        mahasiswaService.prosesAyah(mahasiswaDto,mahasiswa);
        mahasiswaService.prosesIbu(mahasiswaDto,mahasiswa);
        if (mahasiswaDto.getNamaWali() != null) {
            mahasiswaService.prosesWali(mahasiswaDto, mahasiswa);
        }
        return "redirect:list";
    }

    @PostMapping("/mahasiswa/aktif")
    public String aktifkanAkun(Mahasiswa mahasiswa){
        mahasiswa.setStatus(StatusRecord.AKTIF);
        mahasiswaDao.save(mahasiswa);
        return "redirect:list";
    }

    @PostMapping("/mahasiswa/nonaktif")
    public String nonaktifkanAkun(Mahasiswa mahasiswa){
        mahasiswa.setStatus(StatusRecord.NONAKTIF);
        mahasiswaDao.save(mahasiswa);
        return "redirect:list";
    }


}
