package id.ac.tazkia.akademik.aplikasiakademik.controller;

import id.ac.tazkia.akademik.aplikasiakademik.dao.*;
import id.ac.tazkia.akademik.aplikasiakademik.entity.*;
import id.ac.tazkia.akademik.aplikasiakademik.service.CurrentUserService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Controller
@RequestMapping("/finance")
public class KeuanganController  {

    @Autowired
    private KomponenBiayaDao komponenBiayaDao;

    @Autowired
    private ProdiDao prodiDao;

    @Autowired
    private TahunAkademikDao tahunAkademikDao;

    @Autowired
    private NilaiKomponenBiayaDao nilaiKomponenBiayaDao;

    @Autowired
    private MahasiswaDao mahasiswaDao;

    @Autowired
    private ProgramDao programDao;

    @Autowired
    private TagihanMahasiwaDao tagihanMahasiwaDao;

    @Autowired
    private DiskonDao diskonDao;

    @Autowired
    private JenisDiskonDao jenisDiskonDao;

    @Autowired
    private RencanaPembayaranDao rencanaPembayaranDao;

    @Autowired
    private RencanaDetailDao rencanaDetailDao;

    @Autowired
    private CurrentUserService currentUserService;

    @Autowired
    private HistoriRencanaDao historiRencanaDao;

    @ModelAttribute("prodi")
    public Iterable<Prodi> prodi() {
        return prodiDao.findByStatusNotIn(StatusRecord.HAPUS);
    }

    @ModelAttribute("jenisDiskon")
    public Iterable<JenisDiskon> jenisDiskon() {
        return jenisDiskonDao.findAll();
    }

    @ModelAttribute("program")
    public Iterable<Program> program() {
        return programDao.findByStatusNotIn(StatusRecord.HAPUS);
    }

    @ModelAttribute("tahun")
    public Iterable<TahunAkademik> tahun() {
        return tahunAkademikDao.findByStatusNotIn(StatusRecord.HAPUS);
    }

    @ModelAttribute("komponen")
    public Iterable<KomponenBiaya> komponen() {
        return komponenBiayaDao.findByStatusNotIn(StatusRecord.HAPUS);
    }

    @ModelAttribute("angkatan")
    public Iterable<Mahasiswa> angkatan() {
        return mahasiswaDao.cariAngkatan();
    }





    @GetMapping("/komponenbiaya/list")
    public void listKomponen(Model model, Pageable page, String search){

        if (StringUtils.hasText(search)) {
            model.addAttribute("search", search);
            model.addAttribute("list", komponenBiayaDao.findByStatusNotInAndNamaContainingIgnoreCaseOrderByNamaAsc(StatusRecord.HAPUS, search, page));
        } else {
            model.addAttribute("list", komponenBiayaDao.findByStatusNotInOrderByNamaAsc(StatusRecord.HAPUS,page));

        }

    }

    @GetMapping("/komponenbiaya/form")
    public void formKomponen(Model model,@RequestParam(required = false) String id){
        model.addAttribute("komponen", new KomponenBiaya());

        if (id != null && !id.isEmpty()) {
            KomponenBiaya komponenBiaya = komponenBiayaDao.findById(id).get();
            if (komponenBiaya != null) {
                model.addAttribute("komponen", komponenBiaya);
                if (komponenBiaya.getStatus() == null){
                    komponenBiaya.setStatus(StatusRecord.NONAKTIF);
                }
            }
        }
    }

    @PostMapping("/komponenbiaya/form")
    public String prosesForm(@ModelAttribute @Valid KomponenBiaya komponenBiaya){
        if (komponenBiaya.getStatus() == null){
            komponenBiaya.setStatus(StatusRecord.NONAKTIF);
        }
        komponenBiayaDao.save(komponenBiaya);
        return "redirect:list";
    }

    @GetMapping("/komponenbiaya/nilai/list")
    public void listkomponenNilai(@RequestParam(required = false) TahunAkademik tahunAkademik,
                                  @RequestParam(required = false) Prodi prodi, @RequestParam(required = false) Program program,
                                  @RequestParam(required = false) String angkatan, Model model, Pageable page){

        if (tahunAkademik != null && prodi != null && program != null && angkatan != null){
        model.addAttribute("tahunAkademik", tahunAkademik);
            model.addAttribute("selectedProdi", prodi);
            model.addAttribute("selectedProgram", program);
            model.addAttribute("selectedAngkatan", angkatan);

            model.addAttribute("nilai", nilaiKomponenBiayaDao.findByStatusNotInAndTahunAkademikAndIdAngkatanMahasiswaAndProdiAndProgram(StatusRecord.HAPUS, tahunAkademik, angkatan, prodi, program, page));
            }

    }

    @GetMapping("/komponenbiaya/nilai/form")
    public void formkomponenNilai(Model model,@RequestParam(required = false) String id,
                                  @RequestParam(required = false) TahunAkademik tahunAkademik,
                                  @RequestParam(required = false) Prodi prodi, @RequestParam(required = false) Program program,
                                  @RequestParam(required = false) String angkatan){
        model.addAttribute("nilaiKomponen", new NilaiKomponenBiaya());
        if (id != null && !id.isEmpty()) {
            NilaiKomponenBiaya nilaiKomponenBiaya = nilaiKomponenBiayaDao.findById(id).get();
            if (nilaiKomponenBiaya != null) {
                model.addAttribute("nilaiKomponen", nilaiKomponenBiaya);
                if (nilaiKomponenBiaya.getStatus() == null){
                    nilaiKomponenBiaya.setStatus(StatusRecord.NONAKTIF);
                }
                if (nilaiKomponenBiaya.getDikaliSks() == null){
                    nilaiKomponenBiaya.setDikaliSks(StatusRecord.N);
                }
            }
        }

        if (tahunAkademik != null){
            model.addAttribute("selectedTahun", tahunAkademik);
            model.addAttribute("selectedProdi", prodi);
            model.addAttribute("selectedProgram", program);
            model.addAttribute("selectedAngkatan", angkatan);
        }
    }

    @PostMapping("/komponenbiaya/nilai/delete")
    public String deleteKomponen(@RequestParam(name = "id", value = "id") NilaiKomponenBiaya nilaiKomponenBiaya){
        nilaiKomponenBiaya.setStatus(StatusRecord.HAPUS);
        nilaiKomponenBiayaDao.save(nilaiKomponenBiaya);
        return "redirect:list?tahunAkademik="+nilaiKomponenBiaya.getTahunAkademik().getId()+"&prodi="+nilaiKomponenBiaya.getProdi().getId()+"&program="+nilaiKomponenBiaya.getProgram().getId()+"&angkatan="+nilaiKomponenBiaya.getIdAngkatanMahasiswa();

    }

    @PostMapping("/komponenbiaya/nilai/form")
    public String proseskomponenNilai(@ModelAttribute @Valid NilaiKomponenBiaya nilaiKomponenBiaya){
        if (nilaiKomponenBiaya.getId() == null || nilaiKomponenBiaya.getId().isEmpty()) {
            if (nilaiKomponenBiaya.getStatus() == null) {
                nilaiKomponenBiaya.setStatus(StatusRecord.NONAKTIF);
            }

            if (nilaiKomponenBiaya.getDikaliSks() == null) {
                nilaiKomponenBiaya.setDikaliSks(StatusRecord.N);
            }
            nilaiKomponenBiayaDao.save(nilaiKomponenBiaya);
        }

        if (nilaiKomponenBiaya.getId() != null || !nilaiKomponenBiaya.getId().isEmpty()){
            NilaiKomponenBiaya nilai = nilaiKomponenBiayaDao.findById(nilaiKomponenBiaya.getId()).get();
            BeanUtils.copyProperties(nilaiKomponenBiaya,nilai);
            nilai.setId(nilai.getId());
            nilaiKomponenBiayaDao.save(nilai);
        }

        return "redirect:list?tahunAkademik="+nilaiKomponenBiaya.getTahunAkademik().getId()+"&prodi="+nilaiKomponenBiaya.getProdi().getId()+"&program="+nilaiKomponenBiaya.getProgram().getId()+"&angkatan="+nilaiKomponenBiaya.getIdAngkatanMahasiswa();

    }

    @PostMapping("/komponenbiaya/nilai/generate")
    public String generateTagihan(@RequestParam(required = false) TahunAkademik tahunAkademik,
                                  @RequestParam(required = false) Prodi prodi, @RequestParam(required = false) Program program,
                                  @RequestParam(required = false) String angkatan,
                                  @RequestParam(required = false) String[] data){
        List<Mahasiswa> mahasiswa = mahasiswaDao.findByStatusAndAngkatanAndIdProdiAndIdProgram(StatusRecord.AKTIF,angkatan,prodi,program);

        for (Mahasiswa mhsw : mahasiswa) {
            for (String nilai : data) {
                NilaiKomponenBiaya nilaiKomponenBiaya = nilaiKomponenBiayaDao.findById(nilai).get();

                TagihanMahasiswa tagihanMahasiswa = new TagihanMahasiswa();
                tagihanMahasiswa.setLunas(StatusRecord.BELUM_LUNAS);
                tagihanMahasiswa.setTanggalTagih(LocalDate.now());
                tagihanMahasiswa.setBatasWaktu(LocalDate.now().plusMonths(1));
                tagihanMahasiswa.setMahasiswa(mhsw);
                tagihanMahasiswa.setTahunAkademik(tahunAkademik);
                tagihanMahasiswa.setQty(1);
                tagihanMahasiswa.setKomponenBiaya(nilaiKomponenBiaya.getKomponenBiaya());
                tagihanMahasiswa.setAmount(nilaiKomponenBiaya.getAmount());
                tagihanMahasiwaDao.save(tagihanMahasiswa);

                RencanaPembayaran rencanaPembayaran = new RencanaPembayaran();
                rencanaPembayaran.setAmount(nilaiKomponenBiaya.getAmount());
                rencanaPembayaran.setTanggalJatuhTempo(LocalDate.now().plusMonths(1));
                rencanaPembayaran.setTagihanMahasiswa(tagihanMahasiswa);
                rencanaPembayaran.setLunas(StatusRecord.BELUM_LUNAS);
                rencanaPembayaranDao.save(rencanaPembayaran);

                RencanaDetail rencanaDetail = new RencanaDetail();
                rencanaDetail.setStatusBayar(StatusRecord.AKTIF);
                rencanaDetail.setRencanaPembayaran(rencanaPembayaran);
                rencanaDetail.setLunas(StatusRecord.BELUM_LUNAS);
                rencanaDetail.setAmount(rencanaPembayaran.getAmount());
                rencanaDetail.setPembayaranKe("1");
                rencanaDetail.setTagihanMahasiswa(tagihanMahasiswa);
                rencanaDetailDao.save(rencanaDetail);

            }
        }

        return "redirect:list?tahunAkademik="+tahunAkademik.getId()+"&prodi="+prodi.getId()+"&program="+program.getId()+"&angkatan="+angkatan;
    }

    @GetMapping("/tagihan/mahasiswa/list")
    public void listTagihanMahasiswa(Model model,
                                  @RequestParam(required = false) TahunAkademik tahunAkademik,
                                  @RequestParam(required = false) String nim){

        if (tahunAkademik != null && !nim.isEmpty() || nim == null) {
            Mahasiswa mahasiswa = mahasiswaDao.findByNim(nim);
            List<TagihanMahasiswa> tagihanMahasiswa = tagihanMahasiwaDao.findByMahasiswaAndTahunAkademik(mahasiswa, tahunAkademik);
            for (TagihanMahasiswa tagihan : tagihanMahasiswa) {

            }

            model.addAttribute("tahunAkademik", tahunAkademik);
            model.addAttribute("mahasiswa", mahasiswa);
            model.addAttribute("nim", nim);

        }
    }


    @GetMapping("/tagihan/mahasiswa/diskon/form")
    public void diskonForm(){}

    @PostMapping("/tagihan/mahasiswa/tambahdiskon")
    public String tambihDiskonMahasiswa(@ModelAttribute @Valid Diskon diskon, @RequestParam Mahasiswa mahasiswa, @RequestParam TahunAkademik tahunAkademik){
        List<TagihanMahasiswa> tagihanMahasiswa = tagihanMahasiwaDao.findByMahasiswaAndTahunAkademikAndLunas(mahasiswa,tahunAkademik,"BELUM LUNAS");
        BigDecimal amount = diskon.getAmount().divide(new BigDecimal(tagihanMahasiswa.size()));
        for (TagihanMahasiswa tagihan : tagihanMahasiswa){
            Diskon disc = new Diskon();
            disc.setTagihanMahasiswa(tagihan);
            disc.setAmount(amount);
            disc.setJenisDiskon(diskon.getJenisDiskon());
            diskonDao.save(disc);
        }

        return"redirect:list?tahunAkademik="+tahunAkademik.getId()+"&nim="+mahasiswa.getNim();
    }

    @PostMapping("/tagihan/mahasiswa/penangguhan")
    public String pengagguhanBiaya(@RequestParam Mahasiswa mahasiswa, @RequestParam TahunAkademik tahunAkademik,
                                   @RequestParam BigDecimal amount, @RequestParam KomponenBiaya komponen, Authentication auth){
        User user = currentUserService.currentUser(auth);

        return"redirect:list?tahunAkademik="+tahunAkademik.getId()+"&nim="+mahasiswa.getNim();
    }
}
