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
import id.ac.tazkia.smilemahasiswa.service.KhsService;
import id.ac.tazkia.smilemahasiswa.service.TagihanService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.util.StringUtils;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

@Controller
@Slf4j
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
    KelasMahasiswaDao kelasMahasiswaDao;

    @Autowired
    private IbuDao ibuDao;

    @Autowired
    private AyahDao ayahDao;

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

    @Autowired
    private GradeDao gradeDao;

    @Autowired
    private RequestKonversiDao requestKonversiDao;

    @Autowired
    private KhsService khsService;

    @Autowired
    private ProdiDao prodiDao;

    @Autowired
    private KaryawanDao karyawanDao;

    @Autowired
    private RequestKonversiTenggatDao requestKonversiTenggatDao;

    @Value("${upload.request.konversi}")
    private String uploadRequestKonversi;

    @ModelAttribute("prodi")
    public Iterable<Prodi> prodi() {
        return prodiDao.findByStatus(StatusRecord.AKTIF);
    }

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
        } else {

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
    public String khs(Model model, Authentication authentication, @RequestParam(required = false) TahunAkademik tahunAkademik) throws ParseException {

        User user = currentUserService.currentUser(authentication);

        Mahasiswa mahasiswa = mahasiswaDao.findByUser(user);

        List<TugasDto> tugasDtos = new ArrayList<>();
        List<TugasDto> utsDtos = new ArrayList<>();
        List<TugasDto> uasDtos = new ArrayList<>();
        model.addAttribute("tahun", tahunAkademikDao.findByStatusNotInOrderByTahunDesc(Arrays.asList(StatusRecord.HAPUS)));
        if (tahunAkademik != null) {
            List<KrsDetail> validasiEdom = krsDetailDao.findByMahasiswaAndKrsTahunAkademikAndStatusAndStatusEdom(mahasiswa, tahunAkademik, StatusRecord.AKTIF, StatusRecord.UNDONE);

            model.addAttribute("selectedTahun", tahunAkademik);

            List<DataKhsDto> krsDetail = krsDetailDao.getKhs(tahunAkademik, mahasiswa);
            for (DataKhsDto data : krsDetail) {
                List<TugasDto> nilaiTugas = nilaiTugasDao.findScore(data.getId(), KategoriTugas.TUGAS);
                List<TugasDto> nilaiUts = nilaiTugasDao.findScore(data.getId(), KategoriTugas.UTS);
                List<TugasDto> nilaiUas = nilaiTugasDao.findScore(data.getId(), KategoriTugas.UAS);
                tugasDtos.addAll(nilaiTugas);
                utsDtos.addAll(nilaiUts);
                uasDtos.addAll(nilaiUas);
            }

            if (validasiEdom.isEmpty() || validasiEdom == null) {
                if (!krsDetail.isEmpty()) {
                    model.addAttribute("tugas", tugasDtos);
                    model.addAttribute("uts", utsDtos);
                    model.addAttribute("uas", uasDtos);
                    model.addAttribute("khs", krsDetail);
                    model.addAttribute("ipk", krsDetailDao.ipk(mahasiswa));
                    model.addAttribute("ip", krsDetailDao.ip(mahasiswa, tahunAkademik));
                }
                return "report/khs";
            } else {
                SimpleDateFormat sdformat = new SimpleDateFormat("yyyy-MM-dd");
                Date dateNow = sdformat.parse(LocalDate.now().toString());
                Date scoreEnd = sdformat.parse(tahunAkademik.getTanggalSelesaiNilai().toString());
                if (dateNow.compareTo(scoreEnd) > 0) {
                    return "redirect:edom?tahunAkademik=" + tahunAkademik.getId();
                } else {
                    model.addAttribute("hidden", "Data KHS masih terkunci");
                    return "report/khs";
                }
            }
        }

        return "report/khs";


    }

    @Transactional
    @Scheduled(cron = "0 00 22 * * *", zone = "Asia/Jakarta")
    public void setStatusTenggat() {

        RequestKonversiTenggat requestKonversiTenggat = requestKonversiTenggatDao.findByStatus(StatusRecord.AKTIF);
        if (LocalDate.now().isAfter(requestKonversiTenggat.getTanggalAkhir())) {
            requestKonversiDao.updateStatusTenggat();

        }

    }

    @GetMapping("/report/edom")
    public void edom(Authentication authentication, Model model, @RequestParam(required = false) TahunAkademik tahunAkademik) {
        User user = currentUserService.currentUser(authentication);

        Mahasiswa mahasiswa = mahasiswaDao.findByUser(user);
        model.addAttribute("mahasiswa", mahasiswa);

        List<EdomDto> krsDetail = krsDetailDao.cariEdom(mahasiswa, tahunAkademik, StatusRecord.AKTIF, StatusRecord.UNDONE);

        List<DetailEdom> detailEdoms = krsDetailDao.getListEdom(mahasiswa, tahunAkademik);
        List<EdomQuestion> listQuestion = edomQuestionDao.findByStatusAndTahunAkademikOrderByNomorAsc(StatusRecord.AKTIF, tahunAkademik);

        model.addAttribute("detail", detailEdoms);
        model.addAttribute("question", listQuestion);
        model.addAttribute("tahun", tahunAkademik);
    }

    @PostMapping("/report/edom")
    public String prosesForm(Authentication authentication, HttpServletRequest request, @RequestParam TahunAkademik tahunAkademik,
                             RedirectAttributes attributes) {
        User user = currentUserService.currentUser(authentication);

        Mahasiswa mahasiswa = mahasiswaDao.findByUser(user);

        List<DetailEdom> detailEdoms = krsDetailDao.getListEdom(mahasiswa, tahunAkademik);
        List<EdomQuestion> listQuestion = edomQuestionDao.findByStatusAndTahunAkademikOrderByNomorAsc(StatusRecord.AKTIF, tahunAkademik);

        for (DetailEdom edom : detailEdoms) {
            for (EdomQuestion question : listQuestion) {
                String pertanyaan1 = request.getParameter(edom.getKrs() + "-" + edom.getDosen() + "-" + question.getNomor());
                KrsDetail krsDetail = krsDetailDao.findById(edom.getKrs()).get();
                if (pertanyaan1 == null) {
                    EdomMahasiswa edomMahasiswa = new EdomMahasiswa();
                    edomMahasiswa.setEdomQuestion(question);
                    edomMahasiswa.setJadwal(jadwalDao.findById(edom.getJadwal()).get());
                    edomMahasiswa.setKrsDetail(krsDetail);
                    edomMahasiswa.setNilai(3);
                    edomMahasiswa.setTahunAkademik(tahunAkademik);
                    edomMahasiswa.setDosen(dosenDao.findById(edom.getDosen()).get());
                    edomMahasiswaDao.save(edomMahasiswa);
                } else {
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

        return "redirect:khs?tahunAkademik=" + tahunAkademik.getId();

    }

    @GetMapping("/report/transcript")
    public String checkTranscript(Authentication authentication, Model model) {

        User user = currentUserService.currentUser(authentication);
        Mahasiswa mahasiswa = mahasiswaDao.findByUser(user);
        if (mahasiswa.getKurikulum() != null) {
            model.addAttribute("matakuliah", matakuliahKurikulumDao.listMatkulSelection(mahasiswa));
        }
        TahunAkademik tahun = tahunAkademikDao.findByStatus(StatusRecord.PRAAKTIF);
        TahunAkademik tahunAktif = tahunAkademikDao.findByStatus(StatusRecord.AKTIF);


        model.addAttribute("tahun", tahun);
        model.addAttribute("ceklis", praKrsSpDao.cariKrsSp(mahasiswa, tahun));
        model.addAttribute("total", praKrsSpDao.countSp(mahasiswa, tahun));
        System.out.println(praKrsSpDao.countSp(mahasiswa, tahun));
        model.addAttribute("transkrip", krsDetailDao.transkrip(mahasiswa));
        model.addAttribute("semesterTranskript", krsDao.semesterTranskriptMahasiswa(mahasiswa.getId()));
        model.addAttribute("transkriptTampil", krsDetailDao.transkriptTampil(mahasiswa.getId()));
        model.addAttribute("hapusSp", praKrsSpDao.findByMahasiswaAndStatusAndStatusApproveAndTahunAkademik(mahasiswa, StatusRecord.AKTIF, StatusApprove.WAITING, tahun));

        if (LocalDate.now().isAfter(tahunAktif.getTanggalSelesaiNilai())) {
            return "redirect:edom?tahunAkademik=" + tahunAktif.getId();
        } else {
            return "report/transcript";
        }

    }

    @GetMapping("/report/transcriptParent")
    public void laporanTranskripOrangTua(Authentication authentication, Model model) {

        User user = currentUserService.currentUser(authentication);
        Mahasiswa mahasiswa = null;
        Ibu ibu = ibuDao.findByUser(user);
        if (ibu != null) {
            mahasiswa = mahasiswaDao.findByIbu(ibu);
        } else {
            Ayah ayah = ayahDao.findByUser(user);
            if (ayah != null) {
                mahasiswa = mahasiswaDao.findByAyah(ayah);
            } else {

            }
        }

        String URL = "https://api.whatsapp.com/send?phone=" + mahasiswa.getTeleponSeluler();
        String urlProdi = "https://smile.tazkia.ac.id/mahasiswa/form?mahasiswa=" + mahasiswa.getNim();
        KelasMahasiswa kelasMahasiswa = kelasMahasiswaDao.findByMahasiswaAndStatus(mahasiswa, StatusRecord.AKTIF);

        model.addAttribute("mhs", mahasiswa);
        model.addAttribute("kelas", kelasMahasiswa);
        model.addAttribute("url", URL);
        model.addAttribute("urlProdi", urlProdi);
        model.addAttribute("history", krsDetailDao.historyMahasiswa(mahasiswa));
        model.addAttribute("ipk", krsDetailDao.ipk(mahasiswa));
        model.addAttribute("sksTotal", krsDetailDao.totalSks(mahasiswa));
        model.addAttribute("semester", krsDetailDao.semesterHistory(mahasiswa));
        model.addAttribute("khsHistory", krsDetailDao.khsHistoty(mahasiswa));
        model.addAttribute("transkrip", krsDetailDao.transkrip(mahasiswa));
        model.addAttribute("semesterTranskript", krsDao.semesterTranskript(mahasiswa.getId()));
        model.addAttribute("transkriptTampil", krsDetailDao.transkriptTampil(mahasiswa.getId()));

    }

    @PostMapping("/report/akselerasi")
    public String akselerasi(@RequestParam String idMatkul, @RequestParam(required = false) String idMatkul2, Authentication authentication) {
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
                tagihan.setNilaiTagihanAsli(total);
                tagihan.setAkumulasiPembayaran(BigDecimal.ZERO);
                tagihan.setTanggalPembuatan(LocalDate.now());
                tagihan.setTanggalJatuhTempo(tahunAkademik.getTanggalMulaiUas().minusDays(2));
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
                        tagihan.setNilaiTagihanAsli(total);
                        tagihan.setAkumulasiPembayaran(BigDecimal.ZERO);
                        tagihan.setTanggalPembuatan(LocalDate.now());
                        tagihan.setTanggalJatuhTempo(tahunAkademik.getTanggalSelesaiUas().minusDays(2));
                        tagihan.setTanggalPenangguhan(LocalDate.now().plusYears(1));
                        tagihan.setTahunAkademik(tahunAkademik);
                        tagihan.setStatusTagihan(StatusTagihan.AKTIF);
                        tagihan.setStatus(StatusRecord.AKTIF);
                        tagihanDao.save(tagihan);
                        tagihanService.requestCreateTagihan(tagihan);
                    } else {
                        if (cekSp.isEmpty() || cekSp == null) {
                            Tagihan tagihan = new Tagihan();
                            tagihan.setMahasiswa(mahasiswa);
                            tagihan.setNilaiJenisTagihan(nilaiJenisTagihan);
                            tagihan.setKeterangan(keteranganTagihan);
                            tagihan.setNilaiTagihan(total);
                            tagihan.setNilaiTagihanAsli(total);
                            tagihan.setAkumulasiPembayaran(BigDecimal.ZERO);
                            tagihan.setTanggalPembuatan(LocalDate.now());
                            tagihan.setTanggalJatuhTempo(tahunAkademik.getTanggalSelesaiUas().minusDays(2));
                            tagihan.setTanggalPenangguhan(LocalDate.now().plusYears(1));
                            tagihan.setTahunAkademik(tahunAkademik);
                            tagihan.setStatusTagihan(StatusTagihan.AKTIF);
                            tagihan.setStatus(StatusRecord.AKTIF);
                            tagihanDao.save(tagihan);
                            tagihanService.requestCreateTagihan(tagihan);
                        } else {
                            if (total.compareTo(t.getNilaiTagihan()) > 0) {
                                Tagihan tagihan = new Tagihan();
                                tagihan.setMahasiswa(mahasiswa);
                                tagihan.setNilaiJenisTagihan(nilaiJenisTagihan);
                                tagihan.setKeterangan(keteranganTagihan);
                                tagihan.setNilaiTagihan(new BigDecimal(total.intValue() - t.getNilaiTagihan().intValue()));
                                tagihan.setNilaiTagihanAsli(new BigDecimal(total.intValue() - t.getNilaiTagihan().intValue()));
                                tagihan.setAkumulasiPembayaran(BigDecimal.ZERO);
                                tagihan.setTanggalPembuatan(LocalDate.now());
                                tagihan.setTanggalJatuhTempo(tahunAkademik.getTanggalSelesaiUas().minusDays(2));
                                tagihan.setTanggalPenangguhan(LocalDate.now().plusYears(1));
                                tagihan.setTahunAkademik(tahunAkademik);
                                tagihan.setStatusTagihan(StatusTagihan.AKTIF);
                                tagihan.setStatus(StatusRecord.AKTIF);
                                tagihanDao.save(tagihan);
                                tagihanService.requestCreateTagihan(tagihan);
                            }

                        }
                    }
                } else {
                    t.setNilaiTagihan(t.getNilaiTagihan().add(total));
                    t.setNilaiTagihanAsli(t.getNilaiTagihan().add(total));
                    tagihanDao.save(t);
                    tagihanService.editTagihan(t, t.getNomor());
                }


            }
        } else {
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
            if (nilaiJenisTagihan == null) {

                BiayaSksSp bs = biayaSksDao.findByStatus(StatusRecord.AKTIF);
                BigDecimal total = bs.getBiaya().multiply(new BigDecimal(mk1.getJumlahSks() + mk2.getJumlahSks()));

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
                tagihan.setNilaiTagihanAsli(total);
                tagihan.setAkumulasiPembayaran(BigDecimal.ZERO);
                tagihan.setTanggalPembuatan(LocalDate.now());
                tagihan.setTanggalJatuhTempo(tahunAkademik.getTanggalSelesaiUas().minusDays(2));
                tagihan.setTanggalPenangguhan(LocalDate.now().plusYears(1));
                tagihan.setTahunAkademik(tahunAkademik);
                tagihan.setStatusTagihan(StatusTagihan.AKTIF);
                tagihan.setStatus(StatusRecord.AKTIF);
                tagihanDao.save(tagihan);
                tagihanService.requestCreateTagihan(tagihan);

            } else {

                BiayaSksSp bs = biayaSksDao.findByStatus(StatusRecord.AKTIF);
                BigDecimal total = bs.getBiaya().multiply(new BigDecimal(mk1.getJumlahSks() + mk2.getJumlahSks()));

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
                        tagihan.setNilaiTagihanAsli(new BigDecimal(total.intValue() - t.getNilaiTagihan().intValue()));
                        tagihan.setAkumulasiPembayaran(BigDecimal.ZERO);
                        tagihan.setTanggalPembuatan(LocalDate.now());
                        tagihan.setTanggalJatuhTempo(tahunAkademik.getTanggalSelesaiUas().minusDays(2));
                        tagihan.setTanggalPenangguhan(LocalDate.now().plusYears(1));
                        tagihan.setTahunAkademik(tahunAkademik);
                        tagihan.setStatusTagihan(StatusTagihan.AKTIF);
                        tagihan.setStatus(StatusRecord.AKTIF);
                        tagihanDao.save(tagihan);
                        tagihanService.requestCreateTagihan(tagihan);
                    }
                } else {
                    Tagihan tagihan = new Tagihan();
                    tagihan.setMahasiswa(mahasiswa);
                    tagihan.setNilaiJenisTagihan(nilaiJenisTagihan);
                    tagihan.setKeterangan(keteranganTagihan);
                    tagihan.setNilaiTagihan(total);
                    tagihan.setNilaiTagihanAsli(total);
                    tagihan.setAkumulasiPembayaran(BigDecimal.ZERO);
                    tagihan.setTanggalPembuatan(LocalDate.now());
                    tagihan.setTanggalJatuhTempo(tahunAkademik.getTanggalSelesaiUas().minusDays(2));
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
    public String deleteSp(Authentication authentication) {

        User user = currentUserService.currentUser(authentication);
        Mahasiswa mahasiswa = mahasiswaDao.findByUser(user);

        JenisTagihan jt = jenisTagihanDao.findByKodeAndStatus("23", StatusRecord.AKTIF);
        TahunAkademik tahunAkademik = tahunAkademikDao.findByStatusAndJenis(StatusRecord.PRAAKTIF, StatusRecord.PENDEK);

        List<PraKrsSp> listSp = praKrsSpDao.findByMahasiswaAndStatusAndStatusApproveAndTahunAkademik(mahasiswa, StatusRecord.AKTIF, StatusApprove.WAITING, tahunAkademik);
        for (PraKrsSp deleteSp : listSp) {
            deleteSp.setStatus(StatusRecord.HAPUS);
            deleteSp.setUserDelete(user);
            deleteSp.setStatusApprove(StatusApprove.HAPUS);
            praKrsSpDao.save(deleteSp);
        }
        NilaiJenisTagihan nilaiJenisTagihan = nilaiJenisTagihanDao.findByProdiAndAngkatanAndTahunAkademikAndProgramAndStatusAndJenisTagihan(mahasiswa.getIdProdi(), mahasiswa.getAngkatan(), tahunAkademik, mahasiswa.getIdProgram(), StatusRecord.AKTIF, jt);
        Tagihan t = tagihanDao.findByStatusAndTahunAkademikAndMahasiswaAndNilaiJenisTagihanAndLunas(StatusRecord.AKTIF, tahunAkademik, mahasiswa, nilaiJenisTagihan, false);

        if (t != null) {
            t.setStatus(StatusRecord.HAPUS);
            t.setStatusTagihan(StatusTagihan.HAPUS);
            tagihanDao.save(t);
            tagihanService.hapusTagihan(t);
        }

        return "redirect:../transcript";
    }

    @GetMapping("/studiesActivity/khs/konversi")
    public String requestKonversi(Model model, @RequestParam KrsDetail krsDetail) {
        model.addAttribute("krs", krsDetail);
        model.addAttribute("file", requestKonversiDao.findByKrsDetailAndStatus(krsDetail, StatusRecord.AKTIF));

        return "studiesActivity/khs/konversi";
    }

    @GetMapping("/studiesActivity/khs/konversiApproval")
    public String requestKonversiApproval(Model model, @PageableDefault(size = 100) Pageable pageable, String search, @RequestParam(required = false) Prodi prodi, @RequestParam(required = false) Mahasiswa mahasiswa) {
//        model.addAttribute("report","active");
        model.addAttribute("selectedProdi", prodi);
        model.addAttribute("selectedNim", search);

        for (RequestKonversi requestKonversi1 : requestKonversiDao.findByKrsDetailMahasiswaIdProdiAndApprovalAndStatusAndFinalization(prodi, StatusRecord.APPROVED, StatusRecord.AKTIF, StatusRecord.N, pageable)) {

            model.addAttribute("requestKonversi", khsService.getRequestKonversiById(requestKonversi1.getId()));

        }


        if (prodi != null) {
            if (StringUtils.hasText(search)) {
                model.addAttribute("search", search);
                model.addAttribute("listRequestApproved", requestKonversiDao.findByKrsDetailMahasiswaIdProdiAndApprovalAndStatusAndFinalizationAndKrsDetailMahasiswaNimContainingIgnoreCaseOrderByCreatedTimeAsc(prodi, StatusRecord.APPROVED, StatusRecord.AKTIF, StatusRecord.N, search, pageable));
                model.addAttribute("listRequestApprovedGroupMahasiswa", requestKonversiDao.findByKrsDetailMahasiswaIdProdiAndApprovalAndStatusAndFinalizationAndKrsDetailMahasiswaIdAndKrsDetailMahasiswaNimContainingIgnoreCaseOrderByCreatedTimeAsc(prodi, StatusRecord.APPROVED, StatusRecord.AKTIF, StatusRecord.N, mahasiswa, search, pageable));
                model.addAttribute("listRequestPrestasi", requestKonversiDao.findByKrsDetailMahasiswaIdProdiAndApprovalAndStatusAndJenisNilaiAndKrsDetailMahasiswaNimContainingIgnoreCaseOrderByCreatedTimeAsc(prodi, StatusRecord.WAITING, StatusRecord.AKTIF, StatusRecord.PRESTASI, search, pageable));
                model.addAttribute("listRequestTahfizh", requestKonversiDao.findByKrsDetailMahasiswaIdProdiAndApprovalAndStatusAndJenisNilaiAndKrsDetailMahasiswaNimContainingIgnoreCaseOrderByCreatedTimeAsc(prodi, StatusRecord.WAITING, StatusRecord.AKTIF, StatusRecord.TAHFIZH, search, pageable));
                model.addAttribute("listFinal", requestKonversiDao.findByKrsDetailMahasiswaIdProdiAndStatusAndKrsDetailMahasiswaNimContainingIgnoreCaseOrderByCreatedTimeAsc(prodi, StatusRecord.AKTIF, search, pageable));

            } else {
                model.addAttribute("listRequestApprovedGroup", requestKonversiDao.listApprovalGroup(prodi, pageable));
                model.addAttribute("listApprovalGroupDetail", requestKonversiDao.listApprovalGroupDetail(prodi, pageable));
                model.addAttribute("listRequestApproved", requestKonversiDao.findByKrsDetailMahasiswaIdProdiAndApprovalAndStatusAndFinalization(prodi, StatusRecord.APPROVED, StatusRecord.AKTIF, StatusRecord.N, pageable));
                model.addAttribute("listRequestApprovedGroupMahasiswa", requestKonversiDao.findByKrsDetailMahasiswaIdProdiAndApprovalAndStatusAndFinalizationAndKrsDetailMahasiswaId(prodi, StatusRecord.APPROVED, StatusRecord.AKTIF, StatusRecord.N, mahasiswa, pageable));
                model.addAttribute("listRequestPrestasi", requestKonversiDao.findByKrsDetailMahasiswaIdProdiAndApprovalAndStatusAndJenisNilai(prodi, StatusRecord.WAITING, StatusRecord.AKTIF, StatusRecord.PRESTASI, pageable));
                model.addAttribute("listRequestTahfizh", requestKonversiDao.findByKrsDetailMahasiswaIdProdiAndApprovalAndStatusAndJenisNilai(prodi, StatusRecord.WAITING, StatusRecord.AKTIF, StatusRecord.TAHFIZH, pageable));
                model.addAttribute("listFinal", requestKonversiDao.findByKrsDetailMahasiswaIdProdiAndStatus(prodi, StatusRecord.AKTIF, pageable));


            }
        }

        return "studiesActivity/khs/konversiApproval";
    }

    @PostMapping("/studiesActivity/khs/requestKonversi")
    public String requestKonversi(@Valid @ModelAttribute RequestKonversi requestKonversi, @RequestParam KrsDetail krsDetail, @RequestParam("file") MultipartFile file,
                                  Authentication authentication, RedirectAttributes redirectAttributes, BindingResult errors) throws IOException {


        try {
            if (errors.hasErrors()) {
                log.debug("Error upload supported documents : {}", errors.toString());
            }

            String lokasiUpload = uploadRequestKonversi + File.separator;
            log.info("Lokasi Upload : {}", lokasiUpload);
            new File(lokasiUpload).mkdirs();

            System.out.println("Original Filename: " + file.getOriginalFilename());
            System.out.println("Content Type: " + file.getContentType());
            System.out.println("File Size: " + file.getSize());

            khsService.uploadFileKonversi(requestKonversi, krsDetail, file, lokasiUpload);

            redirectAttributes.addFlashAttribute("success", "Save data successfull..");
            return "redirect:../../studiesActivity/khs/konversi?krsDetail=" + krsDetail.getId() + "&jadwal=" + krsDetail.getJadwal().getId();
        } catch (Exception e) {
            log.error("Error : ", e.getMessage());
            redirectAttributes.addFlashAttribute("gagal", e.getMessage());
            return "redirect:../../studiesActivity/khs/konversi?krsDetail=" + krsDetail.getId() + "&jadwal=" + krsDetail.getJadwal().getId();
        }

    }

    @PostMapping("/studiesActivity/khs/konversiApproval/save")
    public String updateNilai(@RequestParam List<String> grade, @RequestParam List<RequestKonversi> requestKonversi,
                              Authentication authentication, RedirectAttributes redirectAttributes) throws IOException {

        for (RequestKonversi requestKonversi1 : requestKonversi) {
            for (String grade2 : grade) {
                System.out.println("GRADE " + grade2.substring(2, grade2.length()));
                System.out.println("RQ " + requestKonversi1.getId());
                System.out.println("GRADE 2 " + grade2.substring(0, 1));
                if (requestKonversi1.getId().equals(grade2.substring(2, grade2.length()))) {
                    try {
                        khsService.updateNilai(authentication, requestKonversi1.getKrsDetail(), grade2.substring(0, 1));
//                   redirectAttributes.addFlashAttribute("success", "Save data successfull..");
                    } catch (Exception e) {
                        log.error("Error : ", e.getMessage());
                        redirectAttributes.addFlashAttribute("gagal", e.getMessage());
//                    return "redirect:../../khs/konversiApproval";
                    }
                }
            }
        }
        return "redirect:../../khs/konversiApproval";

    }

    @GetMapping("/khs/request/{requestKonversi}/")
    public ResponseEntity<byte[]> tampilkanBukti(@PathVariable RequestKonversi requestKonversi) throws Exception {
        String lokasiUpload = uploadRequestKonversi + File.separator + requestKonversi.getFilename();
        log.debug("Lokasi file bukti : {}", lokasiUpload);

        try {
            HttpHeaders headers = new HttpHeaders();
            if (requestKonversi.getFilename().toLowerCase().endsWith("jpeg") || requestKonversi.getFilename().toLowerCase().endsWith("jpg")) {
                headers.setContentType(MediaType.IMAGE_JPEG);
            } else if (requestKonversi.getFilename().toLowerCase().endsWith("png")) {
                headers.setContentType(MediaType.IMAGE_PNG);
            } else if (requestKonversi.getFilename().toLowerCase().endsWith("pdf")) {
                headers.setContentType(MediaType.APPLICATION_PDF);
            } else if (requestKonversi.getFilename().toLowerCase().endsWith("xlsx") || requestKonversi.getFilename().toLowerCase().endsWith("xls") || requestKonversi.getFilename().toLowerCase().endsWith("csv")) {
                headers.setContentType(new MediaType("application", "force-download"));
                headers.set(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + requestKonversi.getNamaFile());
            } else {
                headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
            }
            byte[] data = Files.readAllBytes(Paths.get(lokasiUpload));
            return new ResponseEntity<byte[]>(data, headers, HttpStatus.OK);
        } catch (Exception err) {
            log.warn(err.getMessage(), err);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/khs/request/delete")
    public String evidenceDelete(@RequestParam String requestKonversi, Authentication authentication, @RequestParam(required = false) KrsDetail krsDetail, RedirectAttributes redirectAttributes) {
        try {
            khsService.deleteRequestKonversi(requestKonversi, authentication, krsDetail);

            System.out.println("ID KRS DETAIIL" + krsDetail.getId());
            System.out.println("ID JADWAL" + krsDetail.getJadwal().getId());

            redirectAttributes.addFlashAttribute("success", "Delete data successfull..");
            return "redirect:../../studiesActivity/khs/konversi?krsDetail=" + krsDetail.getId() + "&jadwal=" + krsDetail.getJadwal().getId();
        } catch (Exception e) {

            log.error("Error : ", e.getMessage());
            redirectAttributes.addFlashAttribute("gagal", e.getMessage());
            return "redirect:../../studiesActivity/khs/konversi?krsDetail=" + krsDetail.getId() + "&jadwal=" + krsDetail.getJadwal().getId();
        }
    }

    @GetMapping("/khs/request/approved")
    public String requestApproved(@RequestParam String requestKonversi, Authentication authentication, @RequestParam(required = false) KrsDetail krsDetail, RedirectAttributes redirectAttributes) {
        try {
            khsService.approveRequestKonversi(authentication, requestKonversi, krsDetail);

            System.out.println("ID KRS DETAIIL" + krsDetail.getId());
            System.out.println("ID JADWAL" + krsDetail.getJadwal().getId());

            redirectAttributes.addFlashAttribute("success", "Delete data successfull..");
            return "redirect:../../studiesActivity/khs/konversiApproval";
        } catch (Exception e) {

            log.error("Error : ", e.getMessage());
            redirectAttributes.addFlashAttribute("gagal", e.getMessage());
            return "redirect:../../studiesActivity/khs/konversiApproval";
        }
    }

    @GetMapping("/khs/request/rejected")
    public String requestRejected(@RequestParam String requestKonversi, Authentication authentication, @RequestParam(required = false) KrsDetail krsDetail, RedirectAttributes redirectAttributes) {
        try {
            khsService.rejectRequestKonversi(authentication, requestKonversi, krsDetail);

            System.out.println("ID KRS DETAIIL" + krsDetail.getId());
            System.out.println("ID JADWAL" + krsDetail.getJadwal().getId());

            redirectAttributes.addFlashAttribute("success", "Delete data successfull..");
            return "redirect:../../studiesActivity/khs/konversiApproval";
        } catch (Exception e) {

            log.error("Error : ", e.getMessage());
            redirectAttributes.addFlashAttribute("gagal", e.getMessage());
            return "redirect:../../studiesActivity/khs/konversiApproval";
        }
    }


}
