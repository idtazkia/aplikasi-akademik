package id.ac.tazkia.smilemahasiswa.controller;

import id.ac.tazkia.smilemahasiswa.dao.*;
import id.ac.tazkia.smilemahasiswa.dto.report.DataKhsDto;
import id.ac.tazkia.smilemahasiswa.dto.report.DetailEdom;
import id.ac.tazkia.smilemahasiswa.dto.report.EdomDto;
import id.ac.tazkia.smilemahasiswa.dto.report.TugasDto;
import id.ac.tazkia.smilemahasiswa.dto.study.Kartu;
import id.ac.tazkia.smilemahasiswa.dto.user.PrasyaratDto;
import id.ac.tazkia.smilemahasiswa.entity.*;
import id.ac.tazkia.smilemahasiswa.service.CurrentUserService;
import id.ac.tazkia.smilemahasiswa.service.TagihanService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.servlet.http.HttpServletRequest;
import java.math.BigDecimal;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

@Controller
public class ReportMahasiswaController {

    @Autowired
    private KrsDao krsDao;

    @Autowired
    private KrsDetailDao krsDetailDao;

    @Autowired
    private CurrentUserService currentUserService;

    @Autowired
    private TahunAkademikDao tahunAkademikDao;

    @Autowired
    private MahasiswaDao mahasiswaDao;

    @Autowired
    private NilaiTugasDao nilaiTugasDao;

    @Autowired
    private PraKrsSpDao praKrsSpDao;

    @Autowired
    private EdomQuestionDao edomQuestionDao;

    @Autowired
    private EdomMahasiswaDao edomMahasiswaDao;

    @Autowired
    private MatakuliahKurikulumDao matakuliahKurikulumDao;

    @Autowired
    private PrasyaratDao prasyaratDao;

    @Autowired
    private NilaiJenisTagihanDao nilaiJenisTagihanDao;

    @Autowired
    JadwalDao jadwalDao;

    @Autowired
    DosenDao dosenDao;

    @Autowired
    private JenisTagihanDao jenisTagihanDao;

    @Autowired
    private BiayaSksSpDao biayaSksDao;

    @Autowired
    private TagihanDao tagihanDao;

    @Autowired
    private TagihanService tagihanService;

    @GetMapping("/api/prasyarat")
    @ResponseBody
    public Kartu cariPrasyarat(@RequestParam(required = false) String search, Authentication authentication) {
        User user = currentUserService.currentUser(authentication);

        Mahasiswa mahasiswa = mahasiswaDao.findByUser(user);

        if (search.equals("kosong")) {
            Kartu kosong = new Kartu();
            kosong.setMatakuliah("");
            kosong.setIdUjian("KOSONG");
            return kosong;
        } else{

            MatakuliahKurikulum matakuliahKurikulum = matakuliahKurikulumDao.findById(search).get();
            PrasyaratDto prasyarat = prasyaratDao.cariPrasyarat(matakuliahKurikulum);

            if (prasyarat != null) {
                List<PrasyaratDto> pras = prasyaratDao.validasiPras(mahasiswa, prasyarat.getGrade(), prasyarat.getMatakuliah(), prasyarat.getEnglish(), prasyarat.getKode());
                System.out.println(pras);
                if (pras == null || pras.isEmpty()) {
                    Kartu gagal = new Kartu();
                    gagal.setMatakuliah(prasyarat.getMatakuliah());
                    gagal.setIdUjian("GAGAL");
                    return gagal;
                } else {
                    Kartu lulus = new Kartu();
                    lulus.setMatakuliah(prasyarat.getMatakuliah());
                    lulus.setIdUjian("LULUS");
                    return lulus;
                }
            } else {
                Kartu kartu = new Kartu();
                kartu.setMatakuliah("Tidak Ada Prasyarat");
                kartu.setIdUjian("LULUS");
                return kartu;
            }
    }

    }


    @GetMapping("/report/khs")
    public String khs(Model model, Authentication authentication,@RequestParam(required = false) TahunAkademik tahunAkademik) throws ParseException {

        User user = currentUserService.currentUser(authentication);

        Mahasiswa mahasiswa = mahasiswaDao.findByUser(user);

        List<TugasDto> tugasDtos = new ArrayList<>();
        model.addAttribute("tahun" , tahunAkademikDao.findByStatusNotInOrderByTahunDesc(Arrays.asList(StatusRecord.HAPUS)));
        if (tahunAkademik != null) {
            List<KrsDetail> validasiEdom = krsDetailDao.findByMahasiswaAndKrsTahunAkademikAndStatusAndStatusEdom(mahasiswa,tahunAkademik,StatusRecord.AKTIF, StatusRecord.UNDONE);

            model.addAttribute("selectedTahun" , tahunAkademik);

            List<DataKhsDto> krsDetail = krsDetailDao.getKhs(tahunAkademik,mahasiswa);
            for (DataKhsDto data : krsDetail) {
                List<TugasDto> nilaiTugas = nilaiTugasDao.findTaskScore(data.getId());
                tugasDtos.addAll(nilaiTugas);
            }

            if (validasiEdom.isEmpty() || validasiEdom == null){
                if (!krsDetail.isEmpty()){
                    model.addAttribute("tugas",tugasDtos);
                    model.addAttribute("khs",krsDetail);
                    model.addAttribute("ipk", krsDetailDao.ipk(mahasiswa));
                    model.addAttribute("ip", krsDetailDao.ip(mahasiswa,tahunAkademik));
                }
                return "report/khs";
            }else {
                SimpleDateFormat sdformat = new SimpleDateFormat("yyyy-MM-dd");
                Date dateNow = sdformat.parse(LocalDate.now().toString());
                Date scoreEnd = sdformat.parse(tahunAkademik.getTanggalSelesaiNilai().toString());
                if (dateNow.compareTo(scoreEnd) > 0){
                    return "redirect:edom?tahunAkademik="+tahunAkademik.getId();
                }else {
                    model.addAttribute("hidden", "Data KHS masih terkunci");
                    return "report/khs";
                }
            }
        }

        return "report/khs";


    }

    @GetMapping("/report/edom")
    public void edom(Authentication authentication, Model model,@RequestParam(required = false) TahunAkademik tahunAkademik) {
        User user = currentUserService.currentUser(authentication);

        Mahasiswa mahasiswa = mahasiswaDao.findByUser(user);
        model.addAttribute("mahasiswa", mahasiswa);

        List<EdomDto> krsDetail = krsDetailDao.cariEdom(mahasiswa, tahunAkademik, StatusRecord.AKTIF, StatusRecord.UNDONE);

        List<DetailEdom> detailEdoms = krsDetailDao.getListEdom(mahasiswa,tahunAkademik);
        List<EdomQuestion> listQuestion = edomQuestionDao.findByStatusAndTahunAkademikOrderByNomorAsc(StatusRecord.AKTIF, tahunAkademik);

        model.addAttribute("detail", detailEdoms);
        model.addAttribute("question", listQuestion);
        model.addAttribute("tahun", tahunAkademik);
    }

    @PostMapping("/report/edom")
    public String prosesForm(Authentication authentication, HttpServletRequest request,@RequestParam TahunAkademik tahunAkademik,
                             RedirectAttributes attributes) {
        User user = currentUserService.currentUser(authentication);

        Mahasiswa mahasiswa = mahasiswaDao.findByUser(user);

        List<DetailEdom> detailEdoms = krsDetailDao.getListEdom(mahasiswa,tahunAkademik);
        List<EdomQuestion> listQuestion = edomQuestionDao.findByStatusAndTahunAkademikOrderByNomorAsc(StatusRecord.AKTIF, tahunAkademik);

        for (DetailEdom edom : detailEdoms){
            for (EdomQuestion question : listQuestion){
                String pertanyaan1 = request.getParameter(edom.getKrs()+"-"+edom.getDosen()+"-"+question.getNomor());
                KrsDetail krsDetail = krsDetailDao.findById(edom.getKrs()).get();
                if (pertanyaan1 == null){
                    EdomMahasiswa edomMahasiswa = new EdomMahasiswa();
                    edomMahasiswa.setEdomQuestion(question);
                    edomMahasiswa.setJadwal(jadwalDao.findById(edom.getJadwal()).get());
                    edomMahasiswa.setKrsDetail(krsDetail);
                    edomMahasiswa.setNilai(3);
                    edomMahasiswa.setTahunAkademik(tahunAkademik);
                    edomMahasiswa.setDosen(dosenDao.findById(edom.getDosen()).get());
                    edomMahasiswaDao.save(edomMahasiswa);
                }else {
                    EdomMahasiswa edomMahasiswa = new EdomMahasiswa();
                    edomMahasiswa.setEdomQuestion(question);
                    edomMahasiswa.setJadwal(jadwalDao.findById(edom.getJadwal()).get());
                    edomMahasiswa.setKrsDetail(krsDetail);
                    edomMahasiswa.setNilai(Integer.valueOf(pertanyaan1));
                    edomMahasiswa.setTahunAkademik(tahunAkademik);
                    edomMahasiswa.setDosen(dosenDao.findById(edom.getDosen()).get());
                    edomMahasiswaDao.save(edomMahasiswa);
                }
                if (krsDetail.getStatusEdom() == StatusRecord.UNDONE) {
                    krsDetail.setStatusEdom(StatusRecord.DONE);
                }
            }
        }

        return "redirect:khs";

    }

    @GetMapping("/report/transcript")
    public void checkTranscript(Authentication authentication,Model model){

        User user = currentUserService.currentUser(authentication);
        Mahasiswa mahasiswa = mahasiswaDao.findByUser(user);
        if (mahasiswa.getKurikulum() != null){
            model.addAttribute("matakuliah", matakuliahKurikulumDao.listMatkulSelection(mahasiswa));
        }
        TahunAkademik tahun = tahunAkademikDao.findByStatus(StatusRecord.PRAAKTIF);
        model.addAttribute("tahun", tahun);
        model.addAttribute("ceklis", praKrsSpDao.cariKrsSp(mahasiswa, tahun));
        model.addAttribute("total", praKrsSpDao.countSp(mahasiswa, tahun));
        System.out.println(praKrsSpDao.countSp(mahasiswa,tahun));
        //tampilsemua
        model.addAttribute("transkrip", krsDetailDao.transkrip(mahasiswa));

        model.addAttribute("semesterTranskript", krsDao.semesterTranskript(mahasiswa.getId()));
        model.addAttribute("transkriptTampil", krsDetailDao.transkriptTampil(mahasiswa.getId()));
        // delete request sp
        model.addAttribute("hapusSp", praKrsSpDao.findByMahasiswaAndStatusAndStatusApproveAndTahunAkademik(mahasiswa, StatusRecord.AKTIF, StatusApprove.WAITING, tahun));


    }

    @PostMapping("/report/akselerasi")
    public String akselerasi(@RequestParam String idMatkul,@RequestParam(required = false) String idMatkul2, Authentication authentication){
        User user = currentUserService.currentUser(authentication);
        Mahasiswa mahasiswa = mahasiswaDao.findByUser(user);
        JenisTagihan jt = jenisTagihanDao.findByKodeAndStatus("23", StatusRecord.AKTIF);

        if (idMatkul2 == "kosong" || idMatkul2.trim().equals("kosong")) {

            TahunAkademik tahunAkademik = tahunAkademikDao.findByStatusAndJenis(StatusRecord.PRAAKTIF, StatusRecord.PENDEK);
            MatakuliahKurikulum matakuliahKurikulum = matakuliahKurikulumDao.findById(idMatkul).get();
            System.out.println(matakuliahKurikulum);

            PraKrsSp pks1 = new PraKrsSp();
            pks1.setMahasiswa(mahasiswa);
            pks1.setMatakuliahKurikulum(matakuliahKurikulum);
            pks1.setNomorTelepon(mahasiswa.getTeleponSeluler());
            pks1.setStatus(StatusRecord.AKTIF);
            pks1.setTahunAkademik(tahunAkademik);
            praKrsSpDao.save(pks1);

            NilaiJenisTagihan nilaiJenisTagihan = nilaiJenisTagihanDao.findByProdiAndAngkatanAndTahunAkademikAndProgramAndStatusAndJenisTagihan(mahasiswa.getIdProdi(),
                    mahasiswa.getAngkatan(), tahunAkademik, mahasiswa.getIdProgram(), StatusRecord.AKTIF, jt);
            if (nilaiJenisTagihan == null) {

                BiayaSksSp bs = biayaSksDao.findByStatus(StatusRecord.AKTIF);
                BigDecimal total = bs.getBiaya().multiply(new BigDecimal(matakuliahKurikulum.getJumlahSks()));

                NilaiJenisTagihan nilaiTagihan = new NilaiJenisTagihan();
                nilaiTagihan.setJenisTagihan(jt);
                nilaiTagihan.setNilai(bs.getBiaya());
                nilaiTagihan.setTahunAkademik(tahunAkademik);
                nilaiTagihan.setProdi(mahasiswa.getIdProdi());
                nilaiTagihan.setProgram(mahasiswa.getIdProgram());
                nilaiTagihan.setAngkatan(mahasiswa.getAngkatan());
                nilaiTagihan.setStatus(StatusRecord.AKTIF);
                nilaiJenisTagihanDao.save(nilaiTagihan);

                String keteranganTagihan = "Tagihan " + nilaiTagihan.getJenisTagihan().getNama()
                        + " a.n. " + mahasiswa.getNama();

                Tagihan tagihan = new Tagihan();
                tagihan.setMahasiswa(mahasiswa);
                tagihan.setNilaiJenisTagihan(nilaiTagihan);
                tagihan.setKeterangan(keteranganTagihan);
                tagihan.setNilaiTagihan(total);
                tagihan.setAkumulasiPembayaran(BigDecimal.ZERO);
                tagihan.setTanggalPembuatan(LocalDate.now());
                tagihan.setTanggalJatuhTempo(LocalDate.now().plusYears(1));
                tagihan.setTanggalPenangguhan(LocalDate.now().plusYears(1));
                tagihan.setTahunAkademik(tahunAkademik);
                tagihan.setStatusTagihan(StatusTagihan.AKTIF);
                tagihan.setStatus(StatusRecord.AKTIF);
                tagihanDao.save(tagihan);
                tagihanService.requestCreateTagihan(tagihan);

            } else {

                BiayaSksSp bs = biayaSksDao.findByStatus(StatusRecord.AKTIF);
                BigDecimal total = bs.getBiaya().multiply(new BigDecimal(matakuliahKurikulum.getJumlahSks()));
                List<PraKrsSp> cekSp = praKrsSpDao.findByMahasiswaAndStatusAndStatusApproveAndTahunAkademik(mahasiswa, StatusRecord.HAPUS, StatusApprove.HAPUS, tahunAkademik);
                System.out.println("sp kosong : " + cekSp);

                String keteranganTagihan = "Tagihan " + nilaiJenisTagihan.getJenisTagihan().getNama()
                        + " a.n. " + mahasiswa.getNama();

                Tagihan t = tagihanDao.findByStatusAndTahunAkademikAndMahasiswaAndNilaiJenisTagihanAndLunas(StatusRecord.AKTIF, tahunAkademik, mahasiswa, nilaiJenisTagihan, false);
                if (t == null) {
                    t = tagihanDao.findByStatusAndTahunAkademikAndMahasiswaAndNilaiJenisTagihanAndLunas(StatusRecord.AKTIF, tahunAkademik, mahasiswa, nilaiJenisTagihan, true);
                    if (t == null) {
                        Tagihan tagihan = new Tagihan();
                        tagihan.setMahasiswa(mahasiswa);
                        tagihan.setNilaiJenisTagihan(nilaiJenisTagihan);
                        tagihan.setKeterangan(keteranganTagihan);
                        tagihan.setNilaiTagihan(total);
                        tagihan.setAkumulasiPembayaran(BigDecimal.ZERO);
                        tagihan.setTanggalPembuatan(LocalDate.now());
                        tagihan.setTanggalJatuhTempo(LocalDate.now().plusYears(1));
                        tagihan.setTanggalPenangguhan(LocalDate.now().plusYears(1));
                        tagihan.setTahunAkademik(tahunAkademik);
                        tagihan.setStatusTagihan(StatusTagihan.AKTIF);
                        tagihan.setStatus(StatusRecord.AKTIF);
                        tagihanDao.save(tagihan);
                        tagihanService.requestCreateTagihan(tagihan);
                    }else{
                        if (cekSp.isEmpty() || cekSp == null) {
                            Tagihan tagihan = new Tagihan();
                            tagihan.setMahasiswa(mahasiswa);
                            tagihan.setNilaiJenisTagihan(nilaiJenisTagihan);
                            tagihan.setKeterangan(keteranganTagihan);
                            tagihan.setNilaiTagihan(total);
                            tagihan.setAkumulasiPembayaran(BigDecimal.ZERO);
                            tagihan.setTanggalPembuatan(LocalDate.now());
                            tagihan.setTanggalJatuhTempo(LocalDate.now().plusYears(1));
                            tagihan.setTanggalPenangguhan(LocalDate.now().plusYears(1));
                            tagihan.setTahunAkademik(tahunAkademik);
                            tagihan.setStatusTagihan(StatusTagihan.AKTIF);
                            tagihan.setStatus(StatusRecord.AKTIF);
                            tagihanDao.save(tagihan);
                            tagihanService.requestCreateTagihan(tagihan);
                        }else{
                            if (total.compareTo(t.getNilaiTagihan()) > 0) {
                                Tagihan tagihan = new Tagihan();
                                tagihan.setMahasiswa(mahasiswa);
                                tagihan.setNilaiJenisTagihan(nilaiJenisTagihan);
                                tagihan.setKeterangan(keteranganTagihan);
                                tagihan.setNilaiTagihan(new BigDecimal(total.intValue() - t.getNilaiTagihan().intValue()));
                                tagihan.setAkumulasiPembayaran(BigDecimal.ZERO);
                                tagihan.setTanggalPembuatan(LocalDate.now());
                                tagihan.setTanggalJatuhTempo(LocalDate.now().plusYears(1));
                                tagihan.setTanggalPenangguhan(LocalDate.now().plusYears(1));
                                tagihan.setTahunAkademik(tahunAkademik);
                                tagihan.setStatusTagihan(StatusTagihan.AKTIF);
                                tagihan.setStatus(StatusRecord.AKTIF);
                                tagihanDao.save(tagihan);
                                tagihanService.requestCreateTagihan(tagihan);
                            }

                        }
                    }
                }else{
                    t.setNilaiTagihan(t.getNilaiTagihan().add(total));
                    tagihanDao.save(t);
                    tagihanService.editTagihan(t);
                }


            }
        }else {
            TahunAkademik tahunAkademik = tahunAkademikDao.findByStatusAndJenis(StatusRecord.PRAAKTIF, StatusRecord.PENDEK);
            MatakuliahKurikulum mk1 = matakuliahKurikulumDao.findById(idMatkul).get();
            MatakuliahKurikulum mk2 = matakuliahKurikulumDao.findById(idMatkul2).get();

            PraKrsSp pks1 = new PraKrsSp();
            pks1.setMahasiswa(mahasiswa);
            pks1.setMatakuliahKurikulum(mk1);
            pks1.setNomorTelepon(mahasiswa.getTeleponSeluler());
            pks1.setStatus(StatusRecord.AKTIF);
            pks1.setTahunAkademik(tahunAkademik);
            praKrsSpDao.save(pks1);

            PraKrsSp pks2 = new PraKrsSp();
            pks2.setMahasiswa(mahasiswa);
            pks2.setMatakuliahKurikulum(mk2);
            pks2.setNomorTelepon(mahasiswa.getTeleponSeluler());
            pks2.setStatus(StatusRecord.AKTIF);
            pks2.setTahunAkademik(tahunAkademik);
            praKrsSpDao.save(pks2);


            NilaiJenisTagihan nilaiJenisTagihan = nilaiJenisTagihanDao.findByProdiAndAngkatanAndTahunAkademikAndProgramAndStatusAndJenisTagihan(mahasiswa.getIdProdi(),
                    mahasiswa.getAngkatan(), tahunAkademik, mahasiswa.getIdProgram(), StatusRecord.AKTIF, jt);
            if (nilaiJenisTagihan == null){

                BiayaSksSp bs = biayaSksDao.findByStatus(StatusRecord.AKTIF);
                BigDecimal total = bs.getBiaya().multiply(new BigDecimal(mk1.getJumlahSks()+mk2.getJumlahSks()));

                NilaiJenisTagihan nilaiTagihan = new NilaiJenisTagihan();
                nilaiTagihan.setJenisTagihan(jt);
                nilaiTagihan.setNilai(bs.getBiaya());
                nilaiTagihan.setTahunAkademik(tahunAkademik);
                nilaiTagihan.setProdi(mahasiswa.getIdProdi());
                nilaiTagihan.setProgram(mahasiswa.getIdProgram());
                nilaiTagihan.setAngkatan(mahasiswa.getAngkatan());
                nilaiTagihan.setStatus(StatusRecord.AKTIF);
                nilaiJenisTagihanDao.save(nilaiTagihan);

                String keteranganTagihan = "Tagihan " + nilaiTagihan.getJenisTagihan().getNama()
                        + " a.n. " + mahasiswa.getNama();

                Tagihan tagihan = new Tagihan();
                tagihan.setMahasiswa(mahasiswa);
                tagihan.setNilaiJenisTagihan(nilaiTagihan);
                tagihan.setKeterangan(keteranganTagihan);
                tagihan.setNilaiTagihan(total);
                tagihan.setAkumulasiPembayaran(BigDecimal.ZERO);
                tagihan.setTanggalPembuatan(LocalDate.now());
                tagihan.setTanggalJatuhTempo(LocalDate.now().plusYears(1));
                tagihan.setTanggalPenangguhan(LocalDate.now().plusYears(1));
                tagihan.setTahunAkademik(tahunAkademik);
                tagihan.setStatusTagihan(StatusTagihan.AKTIF);
                tagihan.setStatus(StatusRecord.AKTIF);
                tagihanDao.save(tagihan);
                tagihanService.requestCreateTagihan(tagihan);

            } else{

                BiayaSksSp bs = biayaSksDao.findByStatus(StatusRecord.AKTIF);
                BigDecimal total = bs.getBiaya().multiply(new BigDecimal(mk1.getJumlahSks()+mk2.getJumlahSks()));

                String keteranganTagihan = "Tagihan " + nilaiJenisTagihan.getJenisTagihan().getNama()
                        + " a.n. " + mahasiswa.getNama();

                Tagihan t = tagihanDao.findByStatusAndTahunAkademikAndMahasiswaAndNilaiJenisTagihanAndLunas(StatusRecord.AKTIF, tahunAkademik, mahasiswa, nilaiJenisTagihan, true);
                if (t != null) {
                    if (total.compareTo(t.getNilaiTagihan()) > 0) {
                        Tagihan tagihan = new Tagihan();
                        tagihan.setMahasiswa(mahasiswa);
                        tagihan.setNilaiJenisTagihan(nilaiJenisTagihan);
                        tagihan.setKeterangan(keteranganTagihan);
                        tagihan.setNilaiTagihan(new BigDecimal(total.intValue() - t.getNilaiTagihan().intValue()));
                        tagihan.setAkumulasiPembayaran(BigDecimal.ZERO);
                        tagihan.setTanggalPembuatan(LocalDate.now());
                        tagihan.setTanggalJatuhTempo(LocalDate.now().plusYears(1));
                        tagihan.setTanggalPenangguhan(LocalDate.now().plusYears(1));
                        tagihan.setTahunAkademik(tahunAkademik);
                        tagihan.setStatusTagihan(StatusTagihan.AKTIF);
                        tagihan.setStatus(StatusRecord.AKTIF);
                        tagihanDao.save(tagihan);
                        tagihanService.requestCreateTagihan(tagihan);
                    }
                }else{
                    Tagihan tagihan = new Tagihan();
                    tagihan.setMahasiswa(mahasiswa);
                    tagihan.setNilaiJenisTagihan(nilaiJenisTagihan);
                    tagihan.setKeterangan(keteranganTagihan);
                    tagihan.setNilaiTagihan(total);
                    tagihan.setAkumulasiPembayaran(BigDecimal.ZERO);
                    tagihan.setTanggalPembuatan(LocalDate.now());
                    tagihan.setTanggalJatuhTempo(LocalDate.now().plusYears(1));
                    tagihan.setTanggalPenangguhan(LocalDate.now().plusYears(1));
                    tagihan.setTahunAkademik(tahunAkademik);
                    tagihan.setStatusTagihan(StatusTagihan.AKTIF);
                    tagihan.setStatus(StatusRecord.AKTIF);
                    tagihanDao.save(tagihan);
                    tagihanService.requestCreateTagihan(tagihan);
                }
            }
        }

        return "redirect:transcript";
    }

    @PostMapping("/report/sp/delete")
    public String deleteSp(Authentication authentication){

        User user = currentUserService.currentUser(authentication);
        Mahasiswa mahasiswa = mahasiswaDao.findByUser(user);

        JenisTagihan jt = jenisTagihanDao.findByKodeAndStatus("23", StatusRecord.AKTIF);
        TahunAkademik tahunAkademik = tahunAkademikDao.findByStatusAndJenis(StatusRecord.PRAAKTIF, StatusRecord.PENDEK);

        List<PraKrsSp> listSp = praKrsSpDao.findByMahasiswaAndStatusAndStatusApproveAndTahunAkademik(mahasiswa, StatusRecord.AKTIF, StatusApprove.WAITING, tahunAkademik);
        for (PraKrsSp deleteSp : listSp){
            deleteSp.setStatus(StatusRecord.HAPUS);
            deleteSp.setUserDelete(user);
            deleteSp.setStatusApprove(StatusApprove.HAPUS);
            praKrsSpDao.save(deleteSp);
        }
        NilaiJenisTagihan nilaiJenisTagihan = nilaiJenisTagihanDao.findByProdiAndAngkatanAndTahunAkademikAndProgramAndStatusAndJenisTagihan(mahasiswa.getIdProdi(), mahasiswa.getAngkatan(), tahunAkademik, mahasiswa.getIdProgram(), StatusRecord.AKTIF, jt);
        Tagihan t = tagihanDao.findByStatusAndTahunAkademikAndMahasiswaAndNilaiJenisTagihanAndLunas(StatusRecord.AKTIF, tahunAkademik, mahasiswa, nilaiJenisTagihan, false);

        if (t != null){
            t.setStatus(StatusRecord.HAPUS);
            t.setStatusTagihan(StatusTagihan.HAPUS);
            tagihanDao.save(t);
            tagihanService.hapusTagihan(t);
        }

        return "redirect:../transcript";
    }



}
