package id.ac.tazkia.smilemahasiswa.controller;

import id.ac.tazkia.smilemahasiswa.dao.*;
import id.ac.tazkia.smilemahasiswa.dto.AsuransiDto;
import id.ac.tazkia.smilemahasiswa.dto.response.BaseResponse;
import id.ac.tazkia.smilemahasiswa.dto.user.MahasiswaDto;
import id.ac.tazkia.smilemahasiswa.dto.user.ProfileDto;
import id.ac.tazkia.smilemahasiswa.entity.*;
import id.ac.tazkia.smilemahasiswa.service.CurrentUserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.mobile.device.Device;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.script.Bindings;
import javax.swing.text.html.HTML;
import javax.validation.Valid;
import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

@Controller @Slf4j
public class DashboardController {

    @Autowired
    private UserDao userDao;

    @Value("${upload.ktp}")
    private String uploadKtp;

    @Value("${upload.ijazah}")
    private String uploadIjazah;

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

    @Autowired
    private TagihanDao tagihanDao;

    @Autowired
    private RequestCicilanDao requestCicilanDao;

    @Autowired
    private RequestPenangguhanDao requestPenangguhanDao;

    @Autowired
    private RequestPeringananDao requestPeringananDao;

    @Autowired
    private CutiDao cutiDao;

    @Autowired
    private WilayahDao wilayahDao;

    @Autowired
    private ProdiDao prodiDao;

    @Autowired
    private KurikulumDao kurikulumDao;

    @Autowired
    private PraKrsSpDao praKrsSpDao;

    @Autowired
    private BiayaSksSpDao biayaSksDao;

    @Autowired
    private RefundSpDao refundSpDao;

    @Autowired
    private DaftarUlangDao daftarUlangDao;

    @Autowired
    private MemoKeuanganDao memoKeuanganDao;

    @Autowired
    private AsuransiMahasiswaDao asuransiMahasiswaDao;

    @Autowired
    private AsuransiDao asuransiDao;


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

    @GetMapping("/api/kabupaten")
    @ResponseBody
    public List<Wilayah> kabupatenList(@RequestParam String id){
        List<Wilayah> wilayah = wilayahDao.kabupaten(id);
        return wilayah;
    }

    @GetMapping("/api/kecamatan")
    @ResponseBody
    public List<Wilayah> kecamatanList(@RequestParam String id){
        List<Wilayah> wilayah = wilayahDao.kecamatan(id);
        return wilayah;
    }

    @GetMapping("/get-asuransi")
    @ResponseBody
    public List<AsuransiMahasiswa> getAsuransiMahsiswa(@RequestParam String id){
        List<AsuransiMahasiswa> asuransiMahasiswaList = asuransiMahasiswaDao.findByMahasiswaAndStatus(mahasiswaDao.findById(id).get(), StatusRecord.AKTIF);
        return asuransiMahasiswaList;
    }

    @GetMapping("/api/desa")
    @ResponseBody
    public List<Wilayah> desaList(@RequestParam String id){
        List<Wilayah> wilayah = wilayahDao.desa(id);
        return wilayah;
    }

    @GetMapping("/dashboard")
    public String dashboardUtama(Model model, Authentication authentication, Device device){
        User user = currentUserService.currentUser(authentication);
        Mahasiswa mahasiswa = mahasiswaDao.findByUser(user);

        if (mahasiswa == null){
            return "redirect:admin";
        }

        TahunAkademik ta = tahunAkademikDao.findByStatus(StatusRecord.AKTIF);

        Krs k = krsDao.findByTahunAkademikAndMahasiswaAndStatus(ta,mahasiswa,StatusRecord.AKTIF);

        model.addAttribute("krsdetail", krsDetailDao.findByMahasiswaAndKrsAndStatus(mahasiswa, k, StatusRecord.AKTIF));
        model.addAttribute("persentase", krsDetailDao.persentaseKehadiran(mahasiswa,ta));

        // tampilan tagihan
        List<Tagihan> cekTagihan = tagihanDao.findByStatusNotInAndLunasAndMahasiswaAndTahunAkademik(Arrays.asList(StatusRecord.HAPUS), false, mahasiswa, ta);
        model.addAttribute("tagihan", cekTagihan);
        if(cekTagihan.isEmpty()){
            model.addAttribute("message", "message");
        }

        // request cicilan
        List<Object[]> cekCicilan = requestCicilanDao.cekCicilanPerMahasiswa(ta, mahasiswa);
        if (cekCicilan.isEmpty()){
            model.addAttribute("cekCicilan", "cekCicilan");
        }else {
            model.addAttribute("cicilan", cekCicilan);
        }

        // request penangguhan
        List<Object[]> cekPenangguhan = requestPenangguhanDao.cekPenangguhanPerMahasiswa(mahasiswa.getId(), ta.getId());
        if (cekPenangguhan.isEmpty()){
            model.addAttribute("cekPenangguhan", "cekPenangguhan");
        }else {
            model.addAttribute("penangguhan", cekPenangguhan);
        }

        // request peringanan
        List<RequestPeringanan> cekPeringanan = requestPeringananDao.findByTagihanTahunAkademikAndTagihanMahasiswa(ta, mahasiswa);
        if (cekPeringanan.isEmpty()) {
            model.addAttribute("cekPeringanan", "request peringanan");
        }else{
            model.addAttribute("peringanan", cekPeringanan);
        }

        // tampilan pra krs sp
        model.addAttribute("mhs", mahasiswa);
        BiayaSksSp bs = biayaSksDao.findByStatus(StatusRecord.AKTIF);
        TahunAkademik tahun = tahunAkademikDao.findByStatusAndJenis(StatusRecord.PRAAKTIF, StatusRecord.PENDEK);
        if (tahun == null) {
            tahun = tahunAkademikDao.findByStatusAndJenis(StatusRecord.AKTIF, StatusRecord.PENDEK);
        }
        List<Object[]> listSp = praKrsSpDao.listSp(mahasiswa.getId());
        model.addAttribute("tahunAktif", tahun);
        model.addAttribute("listSp", listSp);
        if (listSp.isEmpty()) {
            model.addAttribute("message", "message");
        }
        model.addAttribute("jumlah", bs.getBiaya());
        for (Object[] list : listSp){
            Mahasiswa mhs = mahasiswaDao.findById(list[5].toString()).get();
            TahunAkademik tahunAkademik = tahunAkademikDao.findById(list[6].toString()).get();
            Tagihan tagihan = tagihanDao.tagihanSp(mhs.getId(), tahunAkademik.getId());
            if (tagihan == null){
                model.addAttribute("cekTagihan", "cekTagihan");
            }
        }

        model.addAttribute("memo", memoKeuanganDao.findByTahunAkademikAndAngkatanAndStatusOrderByCreateTime(tahunAkademikDao.findByStatus(StatusRecord.AKTIF), mahasiswa.getAngkatan(), StatusRecord.AKTIF));

        if (device.isMobile()) {
            model.addAttribute("mobile", "mobile device");
        }else{
            model.addAttribute("normal", "normal devide");
        }

        return "dashboard";
    }

    @ResponseBody
    @ResponseStatus(HttpStatus.OK)
    @PostMapping(value = "/asuransi-post", produces = MediaType.APPLICATION_JSON_VALUE)
    public BaseResponse simpanJenjang(@RequestBody AsuransiDto asuransiDto){
        AsuransiMahasiswa cekAsuransi =
                asuransiMahasiswaDao.findByMahasiswaAndAsuransiAndJenisAsuransiAndStatus(mahasiswaDao.findById(asuransiDto.getMahasiswa()).get(),asuransiDao.findById(asuransiDto.getAsuransi()).get(),asuransiDto.getJenis(),StatusRecord.AKTIF);
        if (cekAsuransi == null) {
            AsuransiMahasiswa asuransiMahasiswa = new AsuransiMahasiswa();
            asuransiMahasiswa.setAsuransi(asuransiDao.findById(asuransiDto.getAsuransi()).get());
            asuransiMahasiswa.setMahasiswa(mahasiswaDao.findById(asuransiDto.getMahasiswa()).get());
            asuransiMahasiswa.setJenisAsuransi(asuransiDto.getJenis());
            asuransiMahasiswa.setDurasiTahun(asuransiDto.getDurasi());
            asuransiMahasiswaDao.save(asuransiMahasiswa);
            String message = "Data Asuransi "+ asuransiMahasiswa.getMahasiswa().getNama() + " " + asuransiMahasiswa.getJenisAsuransi() + " telah ditambahkan";

            return new BaseResponse(message,
                    HttpStatus.CREATED.toString());
        }else {

            String message = "Data Asuransi Sudah Ada";

            return new BaseResponse(message,
                    String.valueOf(HttpStatus.ALREADY_REPORTED.value()));
        }

    }

    @ResponseBody
    @ResponseStatus(HttpStatus.OK)
    @PostMapping(value = "/asuransi-update/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    public BaseResponse updateJenjang(@PathVariable String id, @RequestBody AsuransiDto request){
        AsuransiMahasiswa asuransiMahasiswa = asuransiMahasiswaDao.findById(id).get();
        asuransiMahasiswa.setAsuransi(asuransiDao.findById(request.getAsuransi()).get());
        asuransiMahasiswa.setJenisAsuransi(request.getJenis());
        asuransiMahasiswa.setDurasiTahun(request.getDurasi());
        asuransiMahasiswaDao.save(asuransiMahasiswa);

        String message = "Data Asuransi telah diubah ";

        return new BaseResponse(message, HttpStatus.OK.toString());
    }

    @PostMapping(value = "/asuransi-delete/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    @ResponseStatus(HttpStatus.OK)
    public BaseResponse hapusAsuransi(@PathVariable String id){
        AsuransiMahasiswa asuransiMahasiswa = asuransiMahasiswaDao.findById(id).get();
        asuransiMahasiswa.setStatus(StatusRecord.HAPUS);
        asuransiMahasiswaDao.save(asuransiMahasiswa);

        return new BaseResponse("Asuransi " +asuransiMahasiswa.getMahasiswa().getNama() + " " + asuransiMahasiswa.getJenisAsuransi() + " Berhasil Dihapus", HttpStatus.OK.toString());
    }

    @ResponseBody
    @GetMapping(value = "/asuransi-detail/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    public AsuransiDto detailAsuransi(@PathVariable String id){
        AsuransiMahasiswa asuransiMahasiswa = asuransiMahasiswaDao.findById(id).get();
        AsuransiDto asuransiDto = new AsuransiDto();
        asuransiDto.setAsuransi(asuransiMahasiswa.getAsuransi().getId());
        asuransiDto.setDurasi(asuransiMahasiswa.getDurasiTahun());
        asuransiDto.setJenis(asuransiMahasiswa.getJenisAsuransi());
        asuransiDto.setMahasiswa(asuransiMahasiswa.getMahasiswa().getId());

        return asuransiDto;
    }

    @GetMapping("/")
    public String formAwal(){
        return "redirect:/dashboard";
    }

    @GetMapping("/login")
    public void login(){

    }


    @GetMapping("/user/profile")
    public void UserProfile(Model model,@RequestParam(required = false)String type, Authentication authentication){
        User user = currentUserService.currentUser(authentication);
        Mahasiswa mahasiswa = mahasiswaDao.findByUser(user);
        model.addAttribute("mhsw", mahasiswa);
        model.addAttribute("type", type);
        model.addAttribute("transportasi", transportasiDao.findAll());
        model.addAttribute("kebutuhan",kebutuhanKhususDao.findAll());
        model.addAttribute("provinsi", wilayahDao.provinsi());
        model.addAttribute("listProdi",prodiDao.findByStatusNotIn(Arrays.asList(StatusRecord.HAPUS)));
        model.addAttribute("listKurikulum",kurikulumDao.findByStatusNotIn(Arrays.asList(StatusRecord.HAPUS)));

        model.addAttribute("agama",agamaDao.findByStatus(StatusRecord.AKTIF));
        model.addAttribute("listAsuransi", asuransiDao.findByStatus(StatusRecord.AKTIF));

        model.addAttribute("mhsw",mahasiswa);
        MahasiswaDto mahasiswaDto = new MahasiswaDto();

        mahasiswaDto.setId(mahasiswa.getId());
        mahasiswaDto.setAbsen(mahasiswa.getIdAbsen());
        mahasiswaDto.setAngkatan(mahasiswa.getAngkatan());
        mahasiswaDto.setIdProdi(mahasiswa.getIdProdi());
        mahasiswaDto.setIdKonsentrasi(mahasiswa.getIdKonsentrasi());
        mahasiswaDto.setNim(mahasiswa.getNim());
        mahasiswaDto.setNama(mahasiswa.getNama());
        mahasiswaDto.setStatusMatrikulasi(mahasiswa.getStatusMatrikulasi());
        mahasiswaDto.setUkuranBaju(mahasiswa.getUkuranBaju());
        mahasiswaDto.setKps(mahasiswa.getKps());
        mahasiswaDto.setNomorKps(mahasiswa.getNomorKps());
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
        mahasiswaDto.setRfid(mahasiswa.getRfid());

        model.addAttribute("mahasiswa", mahasiswaDto);


    }

    @PostMapping("/user/profile")
    public String prosesUser(@ModelAttribute @Valid MahasiswaDto mahasiswaDto, BindingResult result,
                             MultipartFile file, MultipartFile ijazah,MultipartFile foto,@RequestParam(required = false) String type,
                             Authentication authentication) throws Exception {
        Mahasiswa mahasiswa = mahasiswaDao.findByUser(mahasiswaDto.getIdUser());

        if (!file.isEmpty() || file != null){
            String namaFile = file.getName();
            String jenisFile = file.getContentType();
            String namaAsliFile = file.getOriginalFilename();
            Long ukuran = file.getSize();

            String extension = "";

            int i = namaAsliFile.lastIndexOf('.');
            int p = Math.max(namaAsliFile.lastIndexOf('/'), namaAsliFile.lastIndexOf('\\'));

            if (i > p){
                extension = namaAsliFile.substring(i + 1);
            }

            String idFile = UUID.randomUUID().toString();
            String lokasiUpload = uploadKtp + File.separator + mahasiswa.getNim();
            new File(lokasiUpload).mkdirs();
            File tujuan = new File(lokasiUpload + File.separator + idFile + "." + extension);
            file.transferTo(tujuan);

            mahasiswa.setFileKtp(idFile + "." + extension);
        }else {
            mahasiswa.setFileKtp(mahasiswa.getFileKtp());
        }

        if (!ijazah.isEmpty() || ijazah != null){
            String namaFile = ijazah.getName();
            String namaFileAsli = ijazah.getOriginalFilename();

            String extinsion = "";
            int i = namaFileAsli.lastIndexOf('.');
            int p = Math.max(namaFileAsli.lastIndexOf('/'), namaFileAsli.lastIndexOf('\\'));

            if (i > p){
                extinsion = namaFileAsli.substring(i + 1);
            }

            String idFile = UUID.randomUUID().toString();
            String lokasiUpload = uploadKtp + File.separator + mahasiswa.getNim();
            new File(lokasiUpload).mkdirs();
            File tujuan = new File(lokasiUpload + File.separator + idFile + "." + extinsion);
            ijazah.transferTo(tujuan);

            mahasiswa.setFileIjazah(idFile + "." + extinsion);
        }else {
            mahasiswa.setFileIjazah(mahasiswa.getFileIjazah());
        }

        if (!foto.isEmpty() || foto != null){
            String namaFile = foto.getName();
            String namaFileAsli = foto.getOriginalFilename();

            String extinsion = "";
            int i = namaFileAsli.lastIndexOf('.');
            int p = Math.max(namaFileAsli.lastIndexOf('/'), namaFileAsli.lastIndexOf('\\'));

            if (i > p){
                extinsion = namaFileAsli.substring(i + 1);
            }

            String idFile = UUID.randomUUID().toString();
            String lokasiUpload = uploadKtp + File.separator + mahasiswa.getNim();
            new File(lokasiUpload).mkdirs();
            File tujuan = new File(lokasiUpload + File.separator + idFile + "." + extinsion);
            foto.transferTo(tujuan);

            mahasiswa.setFoto(idFile + "." + extinsion);
        }else {
            mahasiswa.setFoto(mahasiswa.getFoto());
        }


        mahasiswa.setStatus(mahasiswa.getStatus());
        mahasiswa.setIdAgama(mahasiswaDto.getAgama());
        mahasiswa.setEmailPribadi(mahasiswaDto.getEmailPribadi());
        mahasiswa.setEmailTazkia(mahasiswaDto.getEmailTazkia());
        mahasiswa.setUkuranBaju(mahasiswaDto.getUkuranBaju());
        mahasiswa.setTeleponSeluler(mahasiswaDto.getTeleponSeluler());
        mahasiswa.setTeleponRumah(mahasiswaDto.getTeleponRumah());
        mahasiswa.setKewarganegaraan(mahasiswaDto.getKewarganegaraan());
        mahasiswa.setTanggalLahir(mahasiswaDto.getTanggalLahir());
        mahasiswa.setNik(mahasiswaDto.getNik());
        mahasiswa.setAlatTransportasi(mahasiswaDto.getAlatTransportasi());
        mahasiswa.setTempatLahir(mahasiswaDto.getTempat());
        mahasiswa.setIdNegara(mahasiswaDto.getIdNegara());
        mahasiswa.setNamaJalan(mahasiswaDto.getNamaJalan());
        mahasiswa.setIdProvinsi(mahasiswaDto.getIdProvinsi());
        mahasiswa.setIdKotaKabupaten(mahasiswaDto.getIdKotaKabupaten());
        mahasiswa.setIdKecamatan(mahasiswaDto.getIdKecamatan());
        mahasiswa.setIdKelurahan(mahasiswaDto.getIdKelurahan());
        mahasiswa.setRt(mahasiswaDto.getRt());
        if (mahasiswa.getStatusAktif() == null){
            mahasiswa.setStatusAktif("AKTIF");
        } else {
            mahasiswa.setStatusAktif(mahasiswa.getStatusAktif());
        }
        mahasiswa.setRw(mahasiswaDto.getRw());
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
        ibu.setEmailIbu(mahasiswaDto.getEmailIbu());
        ibu.setNomorIbu(mahasiswaDto.getNomorIbu());
        ibuDao.save(ibu);

        Ayah ayah = ayahDao.findById(mahasiswaDto.getAyah()).get();
        BeanUtils.copyProperties(mahasiswaDto, ayah);
        ayah.setId(mahasiswaDto.getAyah());
        ayah.setTanggalLahir(mahasiswaDto.getTanggalLahirAyah());
        ayah.setTempatLahir(mahasiswaDto.getTempatLahirAyah());
        ayah.setStatusHidup(mahasiswaDto.getHidup());
        ayah.setNik(mahasiswaDto.getNikAyah());
        ayah.setEmailAyah(mahasiswaDto.getEmailAyah());
        ayah.setNomorAyah(mahasiswaDto.getNomorAyah());
        ayahDao.save(ayah);

        if (type.equals("profile")){
            return "user/profile?type=profile";
        }else {

            TahunAkademik tahunAkademik = tahunAkademikDao.findByStatus(StatusRecord.AKTIF);
            DaftarUlang daftarUlang = daftarUlangDao.findByStatusAndMahasiswaAndTahunAkademik(StatusRecord.AKTIF, mahasiswa, tahunAkademik);
            if (daftarUlang == null) {
                DaftarUlang du = new DaftarUlang();
                du.setMahasiswa(mahasiswa);
                du.setTahunAkademik(tahunAkademik);
                du.setStatus(StatusRecord.AKTIF);
                daftarUlangDao.save(du);
            }
            return "redirect:study/comingsoon";

        }
    }

    @GetMapping("/admin")
    public String adminDashboard(Model model, Authentication authentication, @PageableDefault(size = 10)Pageable pageable){
        TahunAkademik tahunAkademik = tahunAkademikDao.findByStatus(StatusRecord.AKTIF);

        model.addAttribute("jmlDosen",dosenDao.countDosenByStatus(StatusRecord.AKTIF));
        model.addAttribute("jmlMhsA" ,krsDao.countKrsByTahunAkademikAndMahasiswaStatus(tahunAkademik,StatusRecord.AKTIF));
        model.addAttribute("jmlL",krsDao.countKrsByTahunAkademikAndMahasiswaJenisKelamin(tahunAkademik,JenisKelamin.PRIA));
        model.addAttribute("jmlP",krsDao.countKrsByTahunAkademikAndMahasiswaJenisKelamin(tahunAkademik,JenisKelamin.WANITA));

        User user = currentUserService.currentUser(authentication);
        Karyawan karyawan = karyawanDao.findByIdUser(user);
        Dosen dosen = dosenDao.findByKaryawan(karyawan);

        model.addAttribute("listCutiMahasiswa",cutiDao.findByStatusOrderByStatusPengajuaanDesc(StatusRecord.AKTIF, pageable));
        model.addAttribute("listCutiDosenWali", cutiDao.listCutiDosenWali(user));
        model.addAttribute("listCutiKps", cutiDao.listCutiKps(user));

        Iterable<JadwalDosen> jadwal = jadwalDosenDao.findByJadwalStatusNotInAndJadwalTahunAkademikAndDosenAndJadwalHariNotNullAndJadwalKelasNotNullOrderByJadwalHariAscJadwalJamMulaiAsc(Arrays.asList(StatusRecord.HAPUS), tahunAkademik,dosen);
        model.addAttribute("jadwal", jadwal);



        return "dashboardadmin";
    }

    @PostMapping("/admin/updateCuti")
    public String updateStatus(@RequestParam Cuti cuti, Authentication authentication){
        User user = currentUserService.currentUser(authentication);
        String idMahasiswa = cuti.getMahasiswa().getNim();
        Mahasiswa mahasiswa = mahasiswaDao.findByNim(idMahasiswa);

        cuti.setStatusPengajuaan(StatusApprove.APPROVED);
        cuti.setDosenWaliApproved(StatusApprove.APPROVED);
        cutiDao.save(cuti);
        return "redirect:/admin";
    }

    @PostMapping("/admin/approved")
    public String approvedStatus(@RequestParam Cuti cuti, Authentication authentication){
        User user = currentUserService.currentUser(authentication);
        String idMahasiswa = cuti.getMahasiswa().getNim();
        Mahasiswa mahasiswa = mahasiswaDao.findByNim(idMahasiswa);

        mahasiswa.setStatusAktif("CUTI");
        cuti.setStatusPengajuaan(StatusApprove.APPROVED);
        cuti.setKpsApproved(StatusApprove.APPROVED);
        cutiDao.save(cuti);
        return "redirect:/admin";
    }

    @GetMapping("/upload/{foto}/mahasiswa/")
    public ResponseEntity<byte[]> turnitin(@PathVariable Mahasiswa foto, Model model) throws Exception {
        String lokasiFile = uploadKtp + File.separator + foto.getNim()
                + File.separator + foto.getFoto();

        try {
            HttpHeaders headers = new HttpHeaders();
            if (foto.getFoto().toLowerCase().endsWith("jpeg") || foto.getFoto().toLowerCase().endsWith("jpg")) {
                headers.setContentType(MediaType.IMAGE_JPEG);
            } else if (foto.getFoto().toLowerCase().endsWith("png")) {
                headers.setContentType(MediaType.IMAGE_PNG);
            } else if (foto.getFoto().toLowerCase().endsWith("pdf")) {
                headers.setContentType(MediaType.APPLICATION_PDF);
            } else {
                headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
            }
            byte[] data = Files.readAllBytes(Paths.get(lokasiFile));
            return new ResponseEntity<byte[]>(data, headers, HttpStatus.OK);
        } catch (Exception err) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();


        }

    }

}