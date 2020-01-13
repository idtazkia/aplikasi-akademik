package id.ac.tazkia.akademik.aplikasiakademik.controller;

import id.ac.tazkia.akademik.aplikasiakademik.dao.*;
import id.ac.tazkia.akademik.aplikasiakademik.dto.Khs;
import id.ac.tazkia.akademik.aplikasiakademik.dto.KhsDto;
import id.ac.tazkia.akademik.aplikasiakademik.entity.*;
import id.ac.tazkia.akademik.aplikasiakademik.service.CurrentUserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.servlet.http.HttpServletRequest;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.*;

@Controller
public class KhsMahasiswaController {
    private static final Logger LOGGER = LoggerFactory.getLogger(KhsMahasiswaController.class);

    @Autowired
    private KrsDao krsDao;
    @Autowired
    private KrsDetailDao krsDetailDao;
    @Autowired
    private MahasiswaDao mahasiswaDao;
    @Autowired
    private UserDao userDao;
    @Autowired
    private TahunAkademikDao tahunAkademikDao;
    @Autowired
    private GradeDao gradeDao;
    @Autowired
    private CurrentUserService currentUserService;
    @Autowired
    private MatakuliahKurikulumDao matakuliahKurikulumDao;

    @ModelAttribute("listTahunAkademik")
    public Iterable<TahunAkademik> daftarKonfig() {
        return tahunAkademikDao.findByStatusNotInOrderByNamaTahunAkademikDesc(StatusRecord.HAPUS);
    }

    @GetMapping("/menumahasiswa/khs/list")
    public String daftarKhs(Model model, Authentication authentication, @PageableDefault(size = 10) Pageable page, @RequestParam(required = false) TahunAkademik tahunAkademik){
        model.addAttribute("gradeA", gradeDao.findById("1").get());
        model.addAttribute("grademinA", gradeDao.findById("2").get());
        model.addAttribute("gradeplusB", gradeDao.findById("3").get());
        model.addAttribute("gradeB", gradeDao.findById("4").get());
        model.addAttribute("grademinB", gradeDao.findById("5").get());
        model.addAttribute("gradeplusC", gradeDao.findById("6").get());
        model.addAttribute("gradeC", gradeDao.findById("7").get());
        model.addAttribute("gradeD", gradeDao.findById("8").get());
        model.addAttribute("gradeE", gradeDao.findById("9").get());

        LOGGER.debug("Authentication class : {}", authentication.getClass().getName());

        if (authentication == null) {
            LOGGER.warn("Current user is null");
        }

        User user = currentUserService.currentUser(authentication);

        Mahasiswa mahasiswa = mahasiswaDao.findByUser(user);

        List<KrsDetail> validasiEdom = krsDetailDao.findByMahasiswaAndKrsTahunAkademikAndStatusAndStatusEdom(mahasiswa,tahunAkademikDao.findByStatus(StatusRecord.AKTIF),StatusRecord.AKTIF,StatusRecord.UNDONE);


        model.addAttribute("mahasiswa",mahasiswa);


        if (tahunAkademik != null) {
//            List<KhsDto> khsDtos = new ArrayList<>();
//            model.addAttribute("search", tahunAkademik);
//            Page<KrsDetail> krsDetail = krsDetailDao.findByMahasiswaAndKrsTahunAkademik(mahasiswa,tahunAkademik,page);
//            for (KrsDetail kd: krsDetail) {
//                KhsDto khsDto = new KhsDto();
//                khsDto.setKode(kd.getMatakuliahKurikulum().getMatakuliah().getKodeMatakuliah());
//                khsDto.setId(kd);
//                khsDto.setMapel(kd.getMatakuliahKurikulum().getMatakuliah().getNamaMatakuliahEnglish());
//                khsDto.setPresensi(kd.getNilaiPresensi());
//                khsDto.setTugas(kd.getNilaiTugas());
//                khsDto.setUas(kd.getNilaiUas());
//                khsDto.setUts(kd.getNilaiUts());
//                khsDto.setSks(kd.getMatakuliahKurikulum().getJumlahSks());
//
//                BigDecimal tugas = khsDto.getTugas().multiply(kd.getJadwal().getBobotTugas().divide(new BigDecimal(100)));
//                BigDecimal uas = khsDto.getUas().multiply(kd.getJadwal().getBobotUas().divide(new BigDecimal(100)));
//                BigDecimal uts =khsDto.getUts().multiply(kd.getJadwal().getBobotUts().divide(new BigDecimal(100)));
//                BigDecimal presensi = khsDto.getPresensi().multiply(kd.getJadwal().getBobotPresensi().divide(new BigDecimal(100)));
////
//                khsDto.setTotal(tugas.add(uas).add(uts).add(presensi));
//                khsDtos.add(khsDto);
//            }
//            model.addAttribute("khs",khsDtos);
        } else {
            List<Khs> krsDetail = krsDetailDao.getKhs(tahunAkademikDao.findByStatus(StatusRecord.AKTIF),mahasiswa);
            List<KhsDto> khsDtos = new ArrayList<>();

            for (Khs kd : krsDetail){
                KhsDto khsDto = new KhsDto();
                KrsDetail matakuliahKurikulum = krsDetailDao.findById(kd.getId()).get();
                if (kd.getNilaiAkhir() == null){
                    khsDto.setId("E");
                    khsDto.setBobot(BigDecimal.ZERO);
                    khsDto.setTotal(BigDecimal.ZERO);
                    khsDto.setTotalBobot(BigDecimal.ZERO);

                }
                if (kd.getNilaiAkhir() != null) {
                    khsDto.setId(kd.getGrade());
                    khsDto.setBobot(kd.getBobot());
                    khsDto.setTotal(kd.getNilaiAkhir());
                    khsDto.setTotalBobot(kd.getNilaiAkhir());
                }
                khsDto.setSks(matakuliahKurikulum.getMatakuliahKurikulum().getJumlahSks());
                khsDto.setKode(matakuliahKurikulum.getMatakuliahKurikulum().getMatakuliah().getKodeMatakuliah());
                khsDto.setMapel(matakuliahKurikulum.getMatakuliahKurikulum().getMatakuliah().getNamaMatakuliah());
                khsDto.setTugas(kd.getTugas());
                khsDto.setUas(kd.getUas());
                khsDto.setUts(kd.getUts());
                khsDto.setPresensi(kd.getPresensi());
                khsDtos.add(khsDto);
            }

            model.addAttribute("khs",khsDtos);
        }

        if (validasiEdom.isEmpty() || validasiEdom == null){
            return "menumahasiswa/khs/list";
        }else {
            return "redirect:edom";
        }
    }

    @GetMapping("/menumahasiswa/khs/edom")
    public void  formEdom(Authentication authentication, Model model){
        User user = currentUserService.currentUser(authentication);

        Mahasiswa mahasiswa = mahasiswaDao.findByUser(user);
        model.addAttribute("mahasiswa",mahasiswa);

        List<KrsDetail> krsDetail = krsDetailDao.findByMahasiswaAndKrsTahunAkademikAndStatusAndStatusEdom(mahasiswa,tahunAkademikDao.findByStatus(StatusRecord.AKTIF),StatusRecord.AKTIF,StatusRecord.UNDONE);

        model.addAttribute("detail",krsDetail);

    }

    @PostMapping("/menumahasiswa/khs/edom")
    public String prosesForm(Authentication authentication, HttpServletRequest request, RedirectAttributes attributes) {
        User user = currentUserService.currentUser(authentication);

        Mahasiswa mahasiswa = mahasiswaDao.findByUser(user);

        Map<KrsDetail, String> errorMessage = new HashMap<>();
        List<KrsDetail> krsDetail = krsDetailDao.findByMahasiswaAndKrsTahunAkademikAndStatusAndStatusEdom(mahasiswa,tahunAkademikDao.findByStatus(StatusRecord.AKTIF),StatusRecord.AKTIF,StatusRecord.UNDONE);


        Map<String, BigInteger> mapNilaiKpi = new HashMap<>();
        for(KrsDetail daftarEdom : krsDetail) {
            String pertanyaan1 = request.getParameter(daftarEdom.getId() + "1");
            String pertanyaan2 = request.getParameter(daftarEdom.getId() + "2");
            String pertanyaan3 = request.getParameter(daftarEdom.getId() + "3");
            String pertanyaan4 = request.getParameter(daftarEdom.getId() + "4");
            String pertanyaan5 = request.getParameter(daftarEdom.getId() + "5");

            if (pertanyaan1 == null){
                daftarEdom.setE1(Integer.valueOf("3"));
            }else {
                daftarEdom.setE1(Integer.valueOf(pertanyaan1));
            }

            if (pertanyaan2 == null){
                daftarEdom.setE2(Integer.valueOf("3"));
            }else {
                daftarEdom.setE2(Integer.valueOf(pertanyaan2));
            }

            if (pertanyaan3 == null){
                daftarEdom.setE3(Integer.valueOf("3"));
            }else {
                daftarEdom.setE3(Integer.valueOf(pertanyaan3));
            }

            if (pertanyaan4 == null){
                daftarEdom.setE4(Integer.valueOf("3"));
            }else {
                daftarEdom.setE4(Integer.valueOf(pertanyaan4));
            }

            if (pertanyaan5 == null){
                daftarEdom.setE5(Integer.valueOf("3"));
            }else {
                daftarEdom.setE5(Integer.valueOf(pertanyaan5));
            }
            daftarEdom.setStatusEdom(StatusRecord.DONE);
            krsDetailDao.save(daftarEdom);



        }

        return "redirect:list";

    }

}
