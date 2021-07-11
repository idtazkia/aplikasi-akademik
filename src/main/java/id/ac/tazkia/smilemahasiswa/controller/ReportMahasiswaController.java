package id.ac.tazkia.smilemahasiswa.controller;

import id.ac.tazkia.smilemahasiswa.dao.*;
import id.ac.tazkia.smilemahasiswa.dto.report.DataKhsDto;
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
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
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
    public String khs(Model model, Authentication authentication,@RequestParam(required = false) TahunAkademik tahunAkademik){

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
                if (tahunAkademik.getTanggalSelesaiNilai().compareTo(LocalDate.now()) > 0){
                    return "redirect:edom?tahunAkademik="+tahunAkademik.getId();
                }else {
                    return "redirect:edom?tahunAkademik="+tahunAkademik.getId();
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
        EdomQuestion edomQuestion = edomQuestionDao.findByStatusAndNomorAndTahunAkademik(StatusRecord.AKTIF,1,tahunAkademik);
        EdomQuestion edomQuestion2 = edomQuestionDao.findByStatusAndNomorAndTahunAkademik(StatusRecord.AKTIF,2,tahunAkademik);
        EdomQuestion edomQuestion3 = edomQuestionDao.findByStatusAndNomorAndTahunAkademik(StatusRecord.AKTIF,3,tahunAkademik);
        EdomQuestion edomQuestion4 = edomQuestionDao.findByStatusAndNomorAndTahunAkademik(StatusRecord.AKTIF,4,tahunAkademik);
        EdomQuestion edomQuestion5 = edomQuestionDao.findByStatusAndNomorAndTahunAkademik(StatusRecord.AKTIF,5,tahunAkademik);

        model.addAttribute("detail", krsDetail);
        model.addAttribute("question1", edomQuestion);
        model.addAttribute("question2", edomQuestion2);
        model.addAttribute("question3", edomQuestion3);
        model.addAttribute("question4", edomQuestion4);
        model.addAttribute("question5", edomQuestion5);
        model.addAttribute("tahun", tahunAkademik);
    }

    @PostMapping("/report/edom")
    public String prosesForm(Authentication authentication, HttpServletRequest request,@RequestParam TahunAkademik tahunAkademik,
                             RedirectAttributes attributes) {
        User user = currentUserService.currentUser(authentication);

        Mahasiswa mahasiswa = mahasiswaDao.findByUser(user);

        List<KrsDetail> krsDetail = krsDetailDao.findByMahasiswaAndKrsTahunAkademikAndStatusAndStatusEdom(mahasiswa,tahunAkademik,StatusRecord.AKTIF,StatusRecord.UNDONE);


        for(KrsDetail daftarEdom : krsDetail) {
            String pertanyaan1 = request.getParameter(daftarEdom.getId() + "1");
            String pertanyaan2 = request.getParameter(daftarEdom.getId() + "2");
            String pertanyaan3 = request.getParameter(daftarEdom.getId() + "3");
            String pertanyaan4 = request.getParameter(daftarEdom.getId() + "4");
            String pertanyaan5 = request.getParameter(daftarEdom.getId() + "5");

            if (pertanyaan1 == null){
                daftarEdom.setE1(Integer.valueOf("3"));
                EdomQuestion edomQuestion = edomQuestionDao.findByStatusAndNomorAndTahunAkademik(StatusRecord.AKTIF,1,tahunAkademik);
                EdomMahasiswa edomMahasiswa = new EdomMahasiswa();
                edomMahasiswa.setEdomQuestion(edomQuestion);
                edomMahasiswa.setJadwal(daftarEdom.getJadwal());
                edomMahasiswa.setKrsDetail(daftarEdom);
                edomMahasiswa.setNilai(3);
                edomMahasiswa.setTahunAkademik(tahunAkademik);
                edomMahasiswaDao.save(edomMahasiswa);
            }else {
                daftarEdom.setE1(Integer.valueOf(pertanyaan1));
                EdomQuestion edomQuestion = edomQuestionDao.findByStatusAndNomorAndTahunAkademik(StatusRecord.AKTIF,1,tahunAkademik);
                EdomMahasiswa edomMahasiswa = new EdomMahasiswa();
                edomMahasiswa.setEdomQuestion(edomQuestion);
                edomMahasiswa.setJadwal(daftarEdom.getJadwal());
                edomMahasiswa.setKrsDetail(daftarEdom);
                edomMahasiswa.setNilai(Integer.valueOf(pertanyaan1));
                edomMahasiswa.setTahunAkademik(tahunAkademik);
                edomMahasiswaDao.save(edomMahasiswa);
            }

            if (pertanyaan2 == null){
                daftarEdom.setE2(Integer.valueOf("3"));
                EdomQuestion edomQuestion = edomQuestionDao.findByStatusAndNomorAndTahunAkademik(StatusRecord.AKTIF,2,tahunAkademik);
                EdomMahasiswa edomMahasiswa2 = new EdomMahasiswa();
                edomMahasiswa2.setEdomQuestion(edomQuestion);
                edomMahasiswa2.setJadwal(daftarEdom.getJadwal());
                edomMahasiswa2.setKrsDetail(daftarEdom);
                edomMahasiswa2.setNilai(3);
                edomMahasiswa2.setTahunAkademik(tahunAkademik);
                edomMahasiswaDao.save(edomMahasiswa2);
            }else {
                daftarEdom.setE2(Integer.valueOf(pertanyaan2));
                EdomQuestion edomQuestion = edomQuestionDao.findByStatusAndNomorAndTahunAkademik(StatusRecord.AKTIF,2,tahunAkademik);
                EdomMahasiswa edomMahasiswa2 = new EdomMahasiswa();
                edomMahasiswa2.setEdomQuestion(edomQuestion);
                edomMahasiswa2.setJadwal(daftarEdom.getJadwal());
                edomMahasiswa2.setKrsDetail(daftarEdom);
                edomMahasiswa2.setNilai(Integer.valueOf(pertanyaan2));
                edomMahasiswa2.setTahunAkademik(tahunAkademik);
                edomMahasiswaDao.save(edomMahasiswa2);
            }

            if (pertanyaan3 == null){
                daftarEdom.setE3(Integer.valueOf("3"));
                EdomQuestion edomQuestion = edomQuestionDao.findByStatusAndNomorAndTahunAkademik(StatusRecord.AKTIF,3,tahunAkademik);
                EdomMahasiswa edomMahasiswa3 = new EdomMahasiswa();
                edomMahasiswa3.setEdomQuestion(edomQuestion);
                edomMahasiswa3.setJadwal(daftarEdom.getJadwal());
                edomMahasiswa3.setKrsDetail(daftarEdom);
                edomMahasiswa3.setNilai(3);
                edomMahasiswa3.setTahunAkademik(tahunAkademik);
                edomMahasiswaDao.save(edomMahasiswa3);
            }else {
                daftarEdom.setE3(Integer.valueOf(pertanyaan3));
                EdomQuestion edomQuestion = edomQuestionDao.findByStatusAndNomorAndTahunAkademik(StatusRecord.AKTIF,3,tahunAkademik);
                EdomMahasiswa edomMahasiswa3 = new EdomMahasiswa();
                edomMahasiswa3.setEdomQuestion(edomQuestion);
                edomMahasiswa3.setJadwal(daftarEdom.getJadwal());
                edomMahasiswa3.setKrsDetail(daftarEdom);
                edomMahasiswa3.setNilai(Integer.valueOf(pertanyaan3));
                edomMahasiswa3.setTahunAkademik(tahunAkademik);
                edomMahasiswaDao.save(edomMahasiswa3);
            }

            if (pertanyaan4 == null){
                daftarEdom.setE4(Integer.valueOf("3"));
                EdomQuestion edomQuestion = edomQuestionDao.findByStatusAndNomorAndTahunAkademik(StatusRecord.AKTIF,4,tahunAkademik);
                EdomMahasiswa edomMahasiswa4 = new EdomMahasiswa();
                edomMahasiswa4.setEdomQuestion(edomQuestion);
                edomMahasiswa4.setJadwal(daftarEdom.getJadwal());
                edomMahasiswa4.setKrsDetail(daftarEdom);
                edomMahasiswa4.setNilai(3);
                edomMahasiswa4.setTahunAkademik(tahunAkademik);
                edomMahasiswaDao.save(edomMahasiswa4);
            }else {
                daftarEdom.setE4(Integer.valueOf(pertanyaan4));
                EdomQuestion edomQuestion = edomQuestionDao.findByStatusAndNomorAndTahunAkademik(StatusRecord.AKTIF,4,tahunAkademik);
                EdomMahasiswa edomMahasiswa4 = new EdomMahasiswa();
                edomMahasiswa4.setEdomQuestion(edomQuestion);
                edomMahasiswa4.setJadwal(daftarEdom.getJadwal());
                edomMahasiswa4.setKrsDetail(daftarEdom);
                edomMahasiswa4.setNilai(Integer.valueOf(pertanyaan4));
                edomMahasiswa4.setTahunAkademik(tahunAkademik);
                edomMahasiswaDao.save(edomMahasiswa4);
            }

            if (pertanyaan5 == null){
                daftarEdom.setE5(Integer.valueOf("3"));
                EdomQuestion edomQuestion = edomQuestionDao.findByStatusAndNomorAndTahunAkademik(StatusRecord.AKTIF,5,tahunAkademik);
                EdomMahasiswa edomMahasiswa5 = new EdomMahasiswa();
                edomMahasiswa5.setEdomQuestion(edomQuestion);
                edomMahasiswa5.setJadwal(daftarEdom.getJadwal());
                edomMahasiswa5.setKrsDetail(daftarEdom);
                edomMahasiswa5.setNilai(3);
                edomMahasiswa5.setTahunAkademik(tahunAkademik);
                edomMahasiswaDao.save(edomMahasiswa5);
            }else {
                daftarEdom.setE5(Integer.valueOf(pertanyaan5));
                EdomQuestion edomQuestion = edomQuestionDao.findByStatusAndNomorAndTahunAkademik(StatusRecord.AKTIF,5,tahunAkademik);
                EdomMahasiswa edomMahasiswa5 = new EdomMahasiswa();
                edomMahasiswa5.setEdomQuestion(edomQuestion);
                edomMahasiswa5.setJadwal(daftarEdom.getJadwal());
                edomMahasiswa5.setKrsDetail(daftarEdom);
                edomMahasiswa5.setNilai(Integer.valueOf(pertanyaan5));
                edomMahasiswa5.setTahunAkademik(tahunAkademik);
                edomMahasiswaDao.save(edomMahasiswa5);
            }
            daftarEdom.setStatusEdom(StatusRecord.DONE);
            krsDetailDao.save(daftarEdom);


        }

        return "redirect:khs";

    }

    @GetMapping("/report/transcript")
    public void checkTranscript(Authentication authentication,Model model){

        User user = currentUserService.currentUser(authentication);
        Mahasiswa mahasiswa = mahasiswaDao.findByUser(user);
        if (mahasiswa.getKurikulum() != null){
            model.addAttribute("matakuliah", matakuliahKurikulumDao.findByStatusNotInAndKurikulumAndKurikulumProdi(Arrays.asList(StatusRecord.HAPUS),mahasiswa.getKurikulum(),mahasiswa.getIdProdi()));
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

                String keteranganTagihan = "Tagihan " + nilaiJenisTagihan.getJenisTagihan().getNama()
                        + " a.n. " + mahasiswa.getNama();

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

        return "redirect:transkript";
    }



}
