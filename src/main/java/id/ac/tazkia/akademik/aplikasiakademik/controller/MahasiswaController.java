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
    private MahasiswaDetailKeluargaDao mahasiswaDetailKeluargaDao;

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
            model.addAttribute("list", mahasiswaDetailKeluargaDao.findByMahasiswaStatusNotInAndMahasiswaNamaContainingIgnoreCaseOrMahasiswaNimContainingIgnoreCaseOrderByMahasiswaNama(StatusRecord.HAPUS, search,search, page));
        } else {
            model.addAttribute("list",mahasiswaDetailKeluargaDao.findByMahasiswaStatusNotIn(StatusRecord.HAPUS,page));

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

            mahasiswaDto.setWali(mahasiswaDetailKeluarga.getWali().getId());
            mahasiswaDto.setNamaWali(mahasiswaDetailKeluarga.getWali().getNamaWali());
            mahasiswaDto.setKebutuhanKhususWali(mahasiswaDetailKeluarga.getWali().getKebutuhanKhusus());
            mahasiswaDto.setTempatLahirWali(mahasiswaDetailKeluarga.getWali().getTempatLahir());
            mahasiswaDto.setTanggalLahirWali(mahasiswaDetailKeluarga.getWali().getTanggalLahir());
            mahasiswaDto.setIdJenjangPendidikanWali(mahasiswaDetailKeluarga.getWali().getIdJenjangPendidikan());
            mahasiswaDto.setIdPekerjaanWali(mahasiswaDetailKeluarga.getWali().getIdPekerjaan());
            mahasiswaDto.setIdPenghasilanWali(mahasiswaDetailKeluarga.getWali().getIdPenghasilan());
            mahasiswaDto.setAgamaWali(mahasiswaDetailKeluarga.getWali().getAgama());
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
