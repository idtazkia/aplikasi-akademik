package id.ac.tazkia.smilemahasiswa.controller;

import id.ac.tazkia.smilemahasiswa.dao.*;
import id.ac.tazkia.smilemahasiswa.dto.select2.BaseRequest;
import id.ac.tazkia.smilemahasiswa.dto.user.MahasiswaDto;
import id.ac.tazkia.smilemahasiswa.entity.*;
import id.ac.tazkia.smilemahasiswa.service.CurrentUserService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.RandomStringUtils;
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

import javax.validation.Valid;
import java.io.File;
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
    private JadwalDao jadwalDao;

    @Autowired
    private KelasDao kelasDao;

    @Autowired
    private KelasMahasiswaDao kelasMahasiswaDao;

    @Autowired
    private TagihanDao tagihanDao;

    @Autowired
    private RequestCicilanDao requestCicilanDao;

    @Autowired
    private RequestPenangguhanDao requestPenangguhanDao;

    @Autowired
    private RequestPeringananDao requestPeringananDao;

    @Autowired
    private PembayaranDao pembayaranDao;

    @Autowired
    private VirtualAccountDao virtualAccountDao;

    @Autowired
    private BankDao bankDao;

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

//    @Autowired
//    private AsuransiMahasiswaDao asuransiMahasiswaDao;

    /*@Autowired
    private AsuransiDao asuransiDao;*/


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

    @GetMapping("/api/provinsi")
    @ResponseBody
    public List<BaseRequest> provinsiList(@RequestParam String search){
        List<BaseRequest> wilayah = wilayahDao.provinsi(search);
        return wilayah;
    }

    @GetMapping("/api/kabupaten")
    @ResponseBody
    public List<BaseRequest> kabupatenList(@RequestParam String id,@RequestParam String search){
        List<BaseRequest> wilayah = wilayahDao.kabupaten(id,search);
        return wilayah;
    }

    @GetMapping("/api/kecamatan")
    @ResponseBody
    public List<BaseRequest> kecamatanList(@RequestParam String id,@RequestParam String search){
        List<BaseRequest> wilayah = wilayahDao.kecamatan(id,search);
        return wilayah;
    }

    @GetMapping("/api/desa")
    @ResponseBody
    public List<BaseRequest> desaList(@RequestParam String id, @RequestParam String search){
        List<BaseRequest> wilayah = wilayahDao.desa(id,search);
        return wilayah;
    }

//    @GetMapping("/get-asuransi")
//    @ResponseBody
//    public List<AsuransiMahasiswa> getAsuransiMahsiswa(@RequestParam String id){
//        List<AsuransiMahasiswa> asuransiMahasiswaList = asuransiMahasiswaDao.findByMahasiswaAndStatus(mahasiswaDao.findById(id).get(), StatusRecord.AKTIF);
//        return asuransiMahasiswaList;
//    }


    @GetMapping("/dashboard")
    public String dashboardUtama(Model model, Authentication authentication, Device device){
        User user = currentUserService.currentUser(authentication);
        Mahasiswa mahasiswa = mahasiswaDao.findByUser(user);

        if (mahasiswa == null){
            return "redirect:admin";
        }

        if (user.getRole().getId().equals("mahasiswanunggak")){
            return "redirect:mahasiswa";
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

        if (tahun != null) {
            List<Object[]> listSp = praKrsSpDao.listSp(mahasiswa.getId(), tahun.getId());
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
        }

        model.addAttribute("memo", memoKeuanganDao.findByTahunAkademikAndAngkatanAndStatusOrderByCreateTime(tahunAkademikDao.findByStatus(StatusRecord.AKTIF), mahasiswa.getAngkatan(), StatusRecord.AKTIF));

        if (device.isMobile()) {
            model.addAttribute("mobile", "mobile device");
        }else{
            model.addAttribute("normal", "normal devide");
        }

        return "dashboard";
    }

   /* @ResponseBody
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
    }*/

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
        model.addAttribute("listProdi",prodiDao.findByStatusNotIn(Arrays.asList(StatusRecord.HAPUS)));
        model.addAttribute("listKurikulum",kurikulumDao.findByStatusNotIn(Arrays.asList(StatusRecord.HAPUS)));


        model.addAttribute("agama",agamaDao.findByStatus(StatusRecord.AKTIF));

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
        if (mahasiswa.getIdProvinsi() != null){
            BaseRequest provinsi = wilayahDao.getProvinsi(mahasiswa.getIdProvinsi());
            if (provinsi != null) {
                mahasiswaDto.setIdProvinsi(provinsi.getId());
                mahasiswaDto.setProvinsi(provinsi.getNama());

                if (mahasiswa.getIdKotaKabupaten() != null) {
                    BaseRequest kabupaten = wilayahDao.getKabupaten(provinsi.getId(), mahasiswa.getIdKotaKabupaten());
                    if (kabupaten != null) {
                        mahasiswaDto.setIdKotaKabupaten(kabupaten.getId());
                        mahasiswaDto.setKotaKabupaten(kabupaten.getNama());

                        if (mahasiswa.getIdKecamatan() != null) {
                            BaseRequest kecamatan = wilayahDao.getKecamatan(kabupaten.getId(), mahasiswa.getIdKecamatan());
                            if (kecamatan != null) {
                                mahasiswaDto.setIdKecamatan(kecamatan.getId());
                                mahasiswaDto.setKecamatan(kecamatan.getNama());

                                if (mahasiswa.getIdKelurahan() != null){
                                    BaseRequest kelurahan = wilayahDao.getDesa(kecamatan.getId(),mahasiswa.getIdKelurahan());
                                    if (kelurahan!= null) {
                                        mahasiswaDto.setIdKelurahan(kelurahan.getId());
                                        mahasiswaDto.setKelurahan(kelurahan.getNama());
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }


        if (mahasiswa.getFileIjazah() != null){
            mahasiswaDto.setFileIjazah(mahasiswa.getFileIjazah());
        }
        if (mahasiswa.getFileKtp() != null){
            mahasiswaDto.setFileKtp(mahasiswa.getFileKtp());
        }
        if (mahasiswa.getFoto() != null){
            mahasiswaDto.setFoto(mahasiswa.getFoto());
        }
        mahasiswaDto.setIbu(mahasiswa.getIbu().getId());
        mahasiswaDto.setNamaIbuKandung(mahasiswa.getIbu().getNamaIbuKandung());
        mahasiswaDto.setKebutuhanKhususIbu(mahasiswa.getIbu().getKebutuhanKhusus());
        mahasiswaDto.setTempatLahirIbu(mahasiswa.getIbu().getTempatLahir());
        mahasiswaDto.setTanggalLahirIbu(mahasiswa.getIbu().getTanggalLahir());
        mahasiswaDto.setIdJenjangPendidikanIbu(mahasiswa.getIbu().getIdJenjangPendidikan());
        mahasiswaDto.setIdPekerjaanIbu(mahasiswa.getIbu().getIdPekerjaan());
        mahasiswaDto.setPenghasilanIbu(mahasiswa.getIbu().getPenghasilan());
        mahasiswaDto.setNikIbu(mahasiswa.getAyah().getNik());
        mahasiswaDto.setEmailIbu(mahasiswa.getIbu().getEmailIbu());
        mahasiswaDto.setNomorIbu(mahasiswa.getIbu().getNomorIbu());
        mahasiswaDto.setAgamaIbu(mahasiswa.getIbu().getAgama());
        mahasiswaDto.setStatusHidupIbu(mahasiswa.getIbu().getStatusHidup());

        mahasiswaDto.setAyah(mahasiswa.getAyah().getId());
        mahasiswaDto.setNikAyah(mahasiswa.getAyah().getNik());
        mahasiswaDto.setEmailAyah(mahasiswa.getAyah().getEmailAyah());
        mahasiswaDto.setNomorAyah(mahasiswa.getAyah().getNomorAyah());
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
        System.out.println(foto.getSize());
        System.out.println(ijazah.getSize());
        if (file.getSize() > 0){
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

        if (ijazah.getSize() > 0){
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

        if (foto.getSize() > 0){
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
        mahasiswa.setIdProvinsi(mahasiswaDto.getProvinsi());
        mahasiswa.setIdKotaKabupaten(mahasiswaDto.getKotaKabupaten());
        mahasiswa.setIdKecamatan(mahasiswaDto.getKecamatan());
        mahasiswa.setIdKelurahan(mahasiswaDto.getKelurahan());
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
            return  "redirect:../user/profile?type=profile";
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

    @GetMapping("/activation/krsdetail")
    public void activeKrs(){}

    @PostMapping("/activation/krsdetail")
    public void setKrsDetail(@RequestParam(required = false) String nim, @RequestParam(required = false) String jadwal, @RequestParam(required = false) String kelas){
        Jadwal j = jadwalDao.findById(jadwal).get();

        if (nim != null){
            Mahasiswa mahasiswa = mahasiswaDao.findByNim(nim);
            Krs krs = krsDao.findByTahunAkademikAndMahasiswaAndStatus(j.getTahunAkademik(),mahasiswa,StatusRecord.AKTIF);
            if (krs != null){
                KrsDetail krsDetail = krsDetailDao.findByMahasiswaAndJadwalAndStatus(mahasiswa,j,StatusRecord.AKTIF);
                if (krsDetail == null){
                    KrsDetail kd = new KrsDetail();
                    kd.setJadwal(j);
                    kd.setKrs(krs);
                    kd.setMahasiswa(mahasiswa);
                    kd.setMatakuliahKurikulum(j.getMatakuliahKurikulum());
                    kd.setNilaiPresensi(BigDecimal.ZERO);
                    kd.setNilaiTugas(BigDecimal.ZERO);
                    kd.setNilaiUas(BigDecimal.ZERO);
                    kd.setNilaiUts(BigDecimal.ZERO);
                    kd.setFinalisasi("N");
                    kd.setJumlahMangkir(0);
                    kd.setJumlahKehadiran(0);
                    kd.setKodeUts(RandomStringUtils.randomAlphanumeric(5));
                    kd.setKodeUas(RandomStringUtils.randomAlphanumeric(5));
                    kd.setJumlahTerlambat(0);
                    kd.setJumlahIzin(0);
                    kd.setJumlahSakit(0);
                    kd.setStatusEdom(StatusRecord.UNDONE);
                    kd.setTahunAkademik(j.getTahunAkademik());
                    krsDetailDao.save(kd);
                }
            }
        }else {
            Kelas k = kelasDao.findById(kelas).get();
            Iterable<KelasMahasiswa> kelasMahasiswa = kelasMahasiswaDao.findByKelasAndStatus(k, StatusRecord.AKTIF);
            for (KelasMahasiswa km : kelasMahasiswa){
                Krs krs = krsDao.findByTahunAkademikAndMahasiswaAndStatus(j.getTahunAkademik(),km.getMahasiswa(),StatusRecord.AKTIF);
                if (krs != null){
                    KrsDetail krsDetail = krsDetailDao.findByMahasiswaAndJadwalAndStatus(km.getMahasiswa(),j,StatusRecord.AKTIF);
                    if (krsDetail == null){
                        KrsDetail kd = new KrsDetail();
                        kd.setJadwal(j);
                        kd.setKrs(krs);
                        kd.setMahasiswa(km.getMahasiswa());
                        kd.setMatakuliahKurikulum(j.getMatakuliahKurikulum());
                        kd.setNilaiPresensi(BigDecimal.ZERO);
                        kd.setNilaiTugas(BigDecimal.ZERO);
                        kd.setNilaiUas(BigDecimal.ZERO);
                        kd.setNilaiUts(BigDecimal.ZERO);
                        kd.setFinalisasi("N");
                        kd.setJumlahMangkir(0);
                        kd.setJumlahKehadiran(0);
                        kd.setKodeUts(RandomStringUtils.randomAlphanumeric(5));
                        kd.setKodeUas(RandomStringUtils.randomAlphanumeric(5));
                        kd.setJumlahTerlambat(0);
                        kd.setJumlahIzin(0);
                        kd.setJumlahSakit(0);
                        kd.setStatusEdom(StatusRecord.UNDONE);
                        kd.setTahunAkademik(j.getTahunAkademik());
                        krsDetailDao.save(kd);
                    }
                }
            }
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

    @GetMapping("/mahasiswa")
    public String mahasiswaNunggak(Model model, Authentication authentication, @PageableDefault(size = 10) Pageable page){
        User user = currentUserService.currentUser(authentication);
        Mahasiswa mhs = mahasiswaDao.findByUser(user);

        Tagihan tagihan = tagihanDao.findByStatusAndStatusTagihanAndMahasiswa(StatusRecord.AKTIF, StatusTagihan.DICICIL, mhs);

        model.addAttribute("cekJumlahCicilan", requestCicilanDao.jumlahCicilan(tagihan));
        model.addAttribute("cekJumlahPembayaran", pembayaranDao.countAllByTagihan(tagihan));
        model.addAttribute("pembayaran", pembayaranDao.cekPembayaran(tagihan.getId()));
        model.addAttribute("bank", bankDao.findByStatus(StatusRecord.AKTIF));
        model.addAttribute("virtualAccount", virtualAccountDao.listVa(tagihan.getId()));
        model.addAttribute("request", requestCicilanDao.findByStatusNotInAndTagihanOrderByTanggalJatuhTempo(Arrays.asList(StatusRecord.HAPUS), tagihan, page));

        return "tunggakan";
    }


}