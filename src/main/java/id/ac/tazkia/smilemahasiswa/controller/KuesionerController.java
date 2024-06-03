package id.ac.tazkia.smilemahasiswa.controller;

import id.ac.tazkia.smilemahasiswa.constant.JenisTest;
import id.ac.tazkia.smilemahasiswa.constant.StatusUjian;
import id.ac.tazkia.smilemahasiswa.dao.*;
import id.ac.tazkia.smilemahasiswa.dto.kusioner.KuesionerDto;
import id.ac.tazkia.smilemahasiswa.dto.prediksitest.TestDto;
import id.ac.tazkia.smilemahasiswa.entity.*;
import id.ac.tazkia.smilemahasiswa.service.CurrentUserService;
import id.ac.tazkia.smilemahasiswa.service.TagihanService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.validation.Valid;
import java.io.File;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

@Controller
public class KuesionerController {

    @Autowired
    private CurrentUserService currentUserService;

    @Autowired
    private KuesionerDao kuesionerDao;

    @Autowired
    private KuesionerPertanyaanDao kuesionerPertanyaanDao;

    @Autowired
    private KuesionerPilihanGandaDao kuesionerPilihanGandaDao;

    @Autowired
    private MahasiswaDao mahasiswaDao;

    @Autowired
    private PrediksiTestDao prediksiTestDao;

    @Autowired
    private TagihanService tagihanService;

    @Autowired
    private EnableFitureDao enableFitureDao;

    @Autowired
    private  TahunAkademikDao tahunAkademikDao;

    @Value("${upload.prediksiTest}")
    private String uploadPrediksiTest;

    @GetMapping("/kuesioner")
    public String listKuesioner(Model model, @PageableDefault Pageable pageable){
        model.addAttribute("list", kuesionerDao.findByStatus(StatusRecord.AKTIF, pageable));
        return "kuesioner/list";
    }

    @GetMapping("/kuesioner/form")
    public String formKuesioner(Model model, @RequestParam Kuesioner kuesioner){
        model.addAttribute("kuesioner", kuesioner);
        model.addAttribute("listPertanyaan", kuesionerPertanyaanDao.findByStatusAndKuesionerOrderByUrutanAsc(StatusRecord.AKTIF, kuesioner));
        model.addAttribute("urutan", kuesionerPertanyaanDao.countByStatusAndKuesioner(StatusRecord.AKTIF, kuesioner));
        return "kuesioner/form";
    }

    @GetMapping("/kuesioner/deskripsi")
    public String formDeskripsi(Model model, @RequestParam(required = false) Kuesioner id){
        if (id == null){
            model.addAttribute("kuesioner", new Kuesioner());
        }else {
            model.addAttribute("kuesioner", id);
        }
        return "kuesioner/deskripsi";
    }

    @PostMapping("/kuesioner/deskripsi/save")
    public String saveDeskripsi(@Valid Kuesioner kuesioner, Authentication authentication,
                                RedirectAttributes attributes){
        try{
            User user = currentUserService.currentUser(authentication);
            kuesioner.setCreatedBy(user.getUsername());
            kuesioner.setCreatedDate(LocalDateTime.now());
            kuesionerDao.save(kuesioner);
            attributes.addFlashAttribute("success", "Berhasil Disimpan");
            return "redirect:/kuesioner";
        }catch (Exception e){
            attributes.addFlashAttribute("error", "Error : " + e.getMessage());
            return "redirect:/kuesioner";
        }
    }

    @GetMapping("/kuesioner/deskripsi/delete")
    public String deleteDeskripsi(@RequestParam Kuesioner kuesioner, RedirectAttributes attributes){
        try{
            kuesioner.setStatus(StatusRecord.HAPUS);
            kuesionerDao.save(kuesioner);
            attributes.addFlashAttribute("success", "Berhasil Dihapus");
            return "redirect:/kuesioner";
        }catch (Exception e){
            attributes.addFlashAttribute("error", "Error : " + e.getMessage());
            return "redirect:/kuesioner";
        }
    }

    @PostMapping("/kuesioner/pertanyaan/save")
    public String savePertanyaan(@Valid KuesionerDto kuesionerDto, RedirectAttributes attributes){
        try{
            KuesionerPertanyaan kuesionerPertanyaan = new KuesionerPertanyaan();
            kuesionerPertanyaan.setKuesioner(kuesionerDto.getKuesioner());
            kuesionerPertanyaan.setPertanyaan(kuesionerDto.getPertanyaan());
            kuesionerPertanyaan.setJenisPertanyaan(kuesionerDto.getJenisPertanyaan());
            kuesionerPertanyaan.setWajibIsi(kuesionerDto.getWajibIsi());
            kuesionerPertanyaan.setCreatedDate(LocalDateTime.now());
            kuesionerPertanyaan.setUrutan(kuesionerDto.getUrutan());
            if (kuesionerDto.getJenisPertanyaan().equals("pilihanGanda")){
                kuesionerPertanyaanDao.save(kuesionerPertanyaan);
                ArrayList<String> pilihanGanda = kuesionerDto.getPilihanGanda();
                int row = 1;
                for (String data : pilihanGanda){
                    KuesionerPilihanGanda kuesionerPilihanGanda = new KuesionerPilihanGanda();
                    kuesionerPilihanGanda.setKuesionerPertanyaan(kuesionerPertanyaan);
                    kuesionerPilihanGanda.setUrutan(row++);
                    kuesionerPilihanGanda.setOpsi(data);
                    kuesionerPilihanGandaDao.save(kuesionerPilihanGanda);
                }
            }else {
                kuesionerPertanyaan.setSkalaLinearAwal(kuesionerDto.getSkalaLinearAwal());
                kuesionerPertanyaan.setSkalaLinearAkhir(kuesionerDto.getSkalaLinearAkhir());
                kuesionerPertanyaan.setOpsiSkalaLinearAwal(kuesionerDto.getOpsiSkalaLinearAwal());
                kuesionerPertanyaan.setOpsiSkalaLinearAkhir(kuesionerDto.getOpsiSkalaLinearAkhir());
                kuesionerPertanyaanDao.save(kuesionerPertanyaan);
            }
            attributes.addFlashAttribute("success", "Berhasil disimpan");
            return "redirect:/kuesioner/form?kuesioner=" + kuesionerDto.getKuesioner().getId();
        }catch (Exception e){
            attributes.addFlashAttribute("error", "Error : " + e.getMessage());
            return "redirect:/kuesioner/form?kuesioner=" + kuesionerDto.getKuesioner().getId();
        }
    }

    @PostMapping("/kuesioner/pertanyaan/edit")
    public String editPertanyaan(@Valid KuesionerDto kuesionerDto, RedirectAttributes attributes){
        try{
            KuesionerPertanyaan kuesionerPertanyaan = new KuesionerPertanyaan();
            kuesionerPertanyaan.setId(kuesionerDto.getId());
            kuesionerPertanyaan.setKuesioner(kuesionerDto.getKuesioner());
            kuesionerPertanyaan.setPertanyaan(kuesionerDto.getPertanyaan());
            kuesionerPertanyaan.setJenisPertanyaan(kuesionerDto.getJenisPertanyaan());
            kuesionerPertanyaan.setWajibIsi(kuesionerDto.getWajibIsi());
            kuesionerPertanyaan.setCreatedDate(LocalDateTime.now());
            kuesionerPertanyaan.setUrutan(kuesionerDto.getUrutan());
            if (kuesionerDto.getJenisPertanyaan().equals("pilihanGanda")){
                kuesionerPertanyaanDao.save(kuesionerPertanyaan);
                ArrayList<String> pilihanGanda = kuesionerDto.getPilihanGanda();
                ArrayList<String> idPilihanGanda = kuesionerDto.getIdPilihanGanda();
                int row = 1;

                for (int i = 0; i < pilihanGanda.size(); i++) {
                    String dataPilihanGanda = pilihanGanda.get(i);
                    String dataId = i < idPilihanGanda.size() ? idPilihanGanda.get(i) : null;

                    if (dataId == null || dataId.isEmpty()) {
                        KuesionerPilihanGanda newKuesionerPilihanGanda = new KuesionerPilihanGanda();
                        newKuesionerPilihanGanda.setKuesionerPertanyaan(kuesionerPertanyaan);
                        newKuesionerPilihanGanda.setUrutan(row++);
                        newKuesionerPilihanGanda.setOpsi(dataPilihanGanda);
                        kuesionerPilihanGandaDao.save(newKuesionerPilihanGanda);
                    } else {
                        KuesionerPilihanGanda kuesionerPilihanGanda = kuesionerPilihanGandaDao.findById(dataId).orElse(new KuesionerPilihanGanda());
                        kuesionerPilihanGanda.setKuesionerPertanyaan(kuesionerPertanyaan);
                        kuesionerPilihanGanda.setUrutan(row++);
                        kuesionerPilihanGanda.setOpsi(dataPilihanGanda);
                        kuesionerPilihanGandaDao.save(kuesionerPilihanGanda);
                    }
                }
            } else {
                kuesionerPertanyaan.setSkalaLinearAwal(kuesionerDto.getSkalaLinearAwal());
                kuesionerPertanyaan.setSkalaLinearAkhir(kuesionerDto.getSkalaLinearAkhir());
                kuesionerPertanyaan.setOpsiSkalaLinearAwal(kuesionerDto.getOpsiSkalaLinearAwal());
                kuesionerPertanyaan.setOpsiSkalaLinearAkhir(kuesionerDto.getOpsiSkalaLinearAkhir());
                kuesionerPertanyaanDao.save(kuesionerPertanyaan);
            }
            attributes.addFlashAttribute("success", "Berhasil diedit");
            return "redirect:/kuesioner/form?kuesioner=" + kuesionerDto.getKuesioner().getId();
        }catch (Exception e){
            attributes.addFlashAttribute("error", "Error : " + e.getMessage());
            return "redirect:/kuesioner/form?kuesioner=" + kuesionerDto.getKuesioner().getId();
        }
    }

    @GetMapping("/kuesioner/pertanyaan/delete")
    public String deletePertanyaan(@RequestParam KuesionerPertanyaan id, RedirectAttributes attributes){
        try{
            id.setStatus(StatusRecord.HAPUS);
            kuesionerPertanyaanDao.save(id);
            attributes.addFlashAttribute("success", "Berhasil Dihapus");
            return "redirect:/kuesioner/form?kuesioner=" + id.getKuesioner().getId();
        }catch (Exception e){
            attributes.addFlashAttribute("error", "Error : " + e.getMessage());
            return "redirect:/kuesioner/form?kuesioner=" + id.getKuesioner().getId();
        }
    }

    @GetMapping("/kuesioner/pertanyaan/edit")
    public String editPertanyaan(Model model, @RequestParam KuesionerPertanyaan id){
        KuesionerDto kuesionerDto = new KuesionerDto();
        BeanUtils.copyProperties(id, kuesionerDto);
        model.addAttribute("kuesionerPertanyaan", kuesionerDto);
        return "kuesioner/edit";
    }

    @ResponseBody
    @GetMapping("/list-pilihanganda")
    public List<KuesionerPilihanGanda> listPilihanGanda(@RequestParam KuesionerPertanyaan id){
        List<KuesionerPilihanGanda> kuesionerPilihanGandas = kuesionerPilihanGandaDao
                .findByStatusAndKuesionerPertanyaanOrderByUrutanAsc(StatusRecord.AKTIF, id);
        return kuesionerPilihanGandas;
    }

    @ResponseBody
    @GetMapping("/delete/pilihanGanda")
    public KuesionerPilihanGanda deletePilihanGanda(@RequestParam KuesionerPilihanGanda id){
        id.setStatus(StatusRecord.HAPUS);
        kuesionerPilihanGandaDao.save(id);

        return id;
    }

    @GetMapping("/kuesioner/toefl/view")
    public String viewKuesionerToefl(Model model, Authentication authentication){
        User user = currentUserService.currentUser(authentication);
        Mahasiswa mahasiswa = mahasiswaDao.findByUser(user);
        model.addAttribute("mahasiswa", mahasiswa);

        EnableFiture enableFiture = enableFitureDao.findByMahasiswaAndFiturAndEnable(mahasiswa, StatusRecord.PREDIKSI_TEST, true);

        Integer totalTest = prediksiTestDao.countTotalUjian(mahasiswa);
        if (totalTest < 1){
            return "kuesioner/toefl/view";
        }else {
            PrediksiTest validate = prediksiTestDao.findByStatusNotInAndStatusUjianInAndMahasiswa(Arrays.asList(StatusRecord.HAPUS), Arrays.asList(StatusUjian.WAITING,StatusUjian.LULUS,StatusUjian.DINILAI), mahasiswa);
            if (validate != null) {
                return "redirect:list";
            }else {
                if (enableFiture != null){
                    return "kuesioner/toefl/view";
                }else {
                    return "redirect:list";
                }
            }
        }
    }

    @GetMapping("/kuesioner/toefl/list")
    public String listKuesionerToefl(Model model, Authentication authentication){
        User user = currentUserService.currentUser(authentication);
        Mahasiswa mahasiswa = mahasiswaDao.findByUser(user);
        List<PrediksiTest> prediksiTest = prediksiTestDao.findByMahasiswaAndStatusNotInOrderByUjianKeAsc(mahasiswa, Arrays.asList(StatusRecord.HAPUS));

        model.addAttribute("mahasiswa", mahasiswa);
        PrediksiTest validate = prediksiTestDao.findByStatusNotInAndStatusUjianInAndMahasiswa(Arrays.asList(StatusRecord.HAPUS), Arrays.asList(StatusUjian.WAITING,StatusUjian.LULUS, StatusUjian.DINILAI), mahasiswa);
        EnableFiture enableFiture = enableFitureDao.findByMahasiswaAndFiturAndEnable(mahasiswa, StatusRecord.PREDIKSI_TEST, true);

        if (enableFiture != null){
            model.addAttribute("enableFitur", enableFiture);
        }

        if (validate != null) {
            model.addAttribute("validate", validate);
        }
        model.addAttribute("listPrediksi", prediksiTestDao.findByMahasiswaAndStatusNotInOrderByUjianKeAsc(mahasiswa, Arrays.asList(StatusRecord.HAPUS)));

        if (prediksiTest == null){
            return "redirect:view";
        }else {
            return "kuesioner/toefl/list";
        }
    }

    @GetMapping("/kuesioner/toefl/admin/list")
    public void listAdminToefl(Model model, @RequestParam(required = false) String search, @RequestParam(required = false) String status,
                               Authentication authentication, Pageable pageable){
        model.addAttribute("status", status);
        if (StringUtils.hasText(search)){
            model.addAttribute("search", search);
            if (status.equals("WAITING")){
                model.addAttribute("listPrediksi", prediksiTestDao.findByNamaOrNimAndStatusUjian(search,status,pageable));
            }else {
                model.addAttribute("listPrediksi", prediksiTestDao.findByNamaOrNimAndStatusUjianNotIn(search,status,pageable));
            }
        }else {
            if (status.equals("WAITING")){
                model.addAttribute("listPrediksi", prediksiTestDao.findByStatusNotInAndStatusUjian(Arrays.asList(StatusRecord.HAPUS),StatusUjian.valueOf(status),pageable));
            }else {
                model.addAttribute("listPrediksi", prediksiTestDao.findByDistincMahasiswa(pageable));
            }
        }
    }

    @PostMapping("/kuesioner/toefl/admin/approve")
    public String approveTest(@RequestParam String id){
        PrediksiTest prediksiTest = prediksiTestDao.findById(id).get();
        prediksiTest.setStatus(StatusRecord.AKTIF);
        prediksiTestDao.save(prediksiTest);
        return "redirect:list?status=WAITING";
    }

    @PostMapping("/kuesioner/toefl/admin/reject")
    public String RejectTest(@Valid TestDto testDto){
        PrediksiTest prediksiTest = prediksiTestDao.findById(testDto.getId()).get();
        prediksiTest.setStatus(StatusRecord.AKTIF);
        prediksiTest.setStatusUjian(StatusUjian.REJECTED);
        prediksiTest.setUjianKe(2);
        prediksiTestDao.save(prediksiTest);
        tagihanService.createTagihanTestInggris(prediksiTest.getMahasiswa(),tahunAkademikDao.findByStatus(StatusRecord.AKTIF), LocalDate.now().plusYears(1) , "toefl");
        return "redirect:list?status=WAITING";
    }

    @PostMapping("/kuesioner/toefl/submit")
    public String submitToefl(@Valid TestDto testDto, Authentication authentication, MultipartFile file) throws  Exception{
        User user = currentUserService.currentUser(authentication);
        Mahasiswa mahasiswa = mahasiswaDao.findByUser(user);
        mahasiswa.setTeleponSeluler(testDto.getNo());
        mahasiswa.setEmailTazkia(testDto.getEmail());
        mahasiswaDao.save(mahasiswa);

        Integer ujianKe = 0;

        PrediksiTest prediksiTest = new PrediksiTest();
        if (prediksiTestDao.countTotalUjian(mahasiswa) > 0){
            ujianKe = prediksiTestDao.findMaxUjian(mahasiswa) + 1;
            prediksiTest.setStatus(StatusRecord.AKTIF);
        }else {
            ujianKe = 1;
        }

        prediksiTest.setMahasiswa(mahasiswa);
        prediksiTest.setJenisTest(JenisTest.TOEFL);
        prediksiTest.setUjianKe(ujianKe);

        if (Integer.valueOf(mahasiswa.getAngkatan()) >= 2020){
            prediksiTest.setStatus(StatusRecord.AKTIF);
        }

        if (file.getSize() > 0) {
            String namaAsliFile = file.getOriginalFilename();

            String extension = "";

            int i = namaAsliFile.lastIndexOf('.');
            int p = Math.max(namaAsliFile.lastIndexOf('/'), namaAsliFile.lastIndexOf('\\'));

            if (i > p) {
                extension = namaAsliFile.substring(i + 1);
            }

            String idFile = UUID.randomUUID().toString();
            String lokasiUpload = uploadPrediksiTest + File.separator + mahasiswa.getNim();
            new File(lokasiUpload).mkdirs();
            File tujuan = new File(lokasiUpload + File.separator + idFile + "." + extension);
            file.transferTo(tujuan);

            prediksiTest.setFileBukti(idFile + "." + extension);
        }
        prediksiTestDao.save(prediksiTest);
        return "redirect:list";
    }

    @PostMapping("/kuesioner/toefl/admin/nilai")
    public String penilaianToefl(@Valid TestDto testDto){
        PrediksiTest prediksiTest = prediksiTestDao.findById(testDto.getId()).get();
        prediksiTest.setNilaiReading(testDto.getNilaiReading());
        prediksiTest.setNilaiListening(testDto.getNilaiListening());
        prediksiTest.setNilaiStructure(testDto.getNilaiStructure());
        BigDecimal nilaiAkhir = prediksiTest.getNilaiReading()
                .add(prediksiTest.getNilaiListening())
                .add(prediksiTest.getNilaiStructure())
                .divide(BigDecimal.valueOf(3), 1, RoundingMode.HALF_UP);
        prediksiTest.setNilai(nilaiAkhir.multiply(BigDecimal.TEN));
        prediksiTest.setStatusUjian(StatusUjian.DINILAI);
        prediksiTestDao.save(prediksiTest);

        return "redirect:list?status=WAITING";
    }

    @GetMapping("/get-detail-prediksi/{id}")
    @ResponseBody
    public PrediksiTest detailTahfidzAdmin(@PathVariable String id) {
        PrediksiTest prediksiTest = prediksiTestDao.findById(id).get();
        return prediksiTest;
    }

    @GetMapping("/get-list-prediksi/{id}")
    @ResponseBody
    public List<PrediksiTest> listPrediksi(@PathVariable String id) {
        Mahasiswa mahasiswa = mahasiswaDao.findById(id).get();
        return prediksiTestDao.findByMahasiswaAndStatusNotInOrderByUjianKeAsc(mahasiswa, Arrays.asList(StatusRecord.HAPUS));
    }


}
