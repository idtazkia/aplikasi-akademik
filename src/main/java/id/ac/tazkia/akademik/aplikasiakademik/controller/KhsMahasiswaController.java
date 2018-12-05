package id.ac.tazkia.akademik.aplikasiakademik.controller;

import id.ac.tazkia.akademik.aplikasiakademik.dao.*;
import id.ac.tazkia.akademik.aplikasiakademik.dto.KhsDto;
import id.ac.tazkia.akademik.aplikasiakademik.entity.*;
import id.ac.tazkia.akademik.aplikasiakademik.service.CurrentUserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestParam;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

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

    @ModelAttribute("listTahunAkademik")
    public Iterable<TahunAkademik> daftarKonfig() {
        return tahunAkademikDao.findByStatusNotInOrderByNamaTahunAkademikDesc(StatusRecord.HAPUS);
    }

    @GetMapping("/menumahasiswa/khs/list")
    public void daftarKhs(Model model, Authentication authentication, @PageableDefault(size = 10) Pageable page, @RequestParam(required = false) TahunAkademik tahunAkademik){
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
        model.addAttribute("mahasiswa",mahasiswa);

        Krs krs = krsDao.findByTahunAkademikStatusAndMahasiswa(StatusRecord.AKTIF,mahasiswa);

        if (tahunAkademik != null) {
            List<KhsDto> khsDtos = new ArrayList<>();
            model.addAttribute("search", tahunAkademik);
            Page<KrsDetail> krsDetail = krsDetailDao.findByMahasiswaAndKrsTahunAkademik(mahasiswa,tahunAkademik,page);
            for (KrsDetail kd: krsDetail) {
                KhsDto khsDto = new KhsDto();
                khsDto.setKode(kd.getMatakuliahKurikulum().getMatakuliah().getKodeMatakuliah());
                khsDto.setId(kd);
                khsDto.setMapel(kd.getMatakuliahKurikulum().getMatakuliah().getNamaMatakuliahEnglish());
                khsDto.setPresensi(kd.getNilaiPresensi());
                khsDto.setTugas(kd.getNilaiTugas());
                khsDto.setUas(kd.getNilaiUas());
                khsDto.setUts(kd.getNilaiUts());
                khsDto.setSks(kd.getMatakuliahKurikulum().getJumlahSks());

                BigDecimal tugas = khsDto.getTugas().multiply(kd.getJadwal().getBobotTugas().divide(new BigDecimal(100)));
                BigDecimal uas = khsDto.getUas().multiply(kd.getJadwal().getBobotUas().divide(new BigDecimal(100)));
                BigDecimal uts =khsDto.getUts().multiply(kd.getJadwal().getBobotUts().divide(new BigDecimal(100)));
                BigDecimal presensi = khsDto.getPresensi().multiply(kd.getJadwal().getBobotPresensi().divide(new BigDecimal(100)));
//
                khsDto.setTotal(tugas.add(uas).add(uts).add(presensi));
                khsDtos.add(khsDto);
            }
            model.addAttribute("khs",khsDtos);
        } else {
            List<KhsDto> khsDtos = new ArrayList<>();
            Page<KrsDetail> krsDetail = krsDetailDao.findByKrsAndMahasiswa(krs,mahasiswa,page);
            for (KrsDetail kd: krsDetail) {
                KhsDto khsDto = new KhsDto();
                khsDto.setKode(kd.getMatakuliahKurikulum().getMatakuliah().getKodeMatakuliah());
                khsDto.setId(kd);
                khsDto.setMapel(kd.getMatakuliahKurikulum().getMatakuliah().getNamaMatakuliahEnglish());
                khsDto.setPresensi(kd.getNilaiPresensi());
                khsDto.setUas(kd.getNilaiUas());
                khsDto.setTugas(kd.getNilaiTugas());
                khsDto.setSks(kd.getMatakuliahKurikulum().getJumlahSks());
                khsDto.setUts(kd.getNilaiUts());

                BigDecimal tugas = khsDto.getTugas().multiply(kd.getJadwal().getBobotTugas().divide(new BigDecimal(100)));
                BigDecimal uas = khsDto.getUas().multiply(kd.getJadwal().getBobotUas().divide(new BigDecimal(100)));
                BigDecimal uts =khsDto.getUts().multiply(kd.getJadwal().getBobotUts().divide(new BigDecimal(100)));
                BigDecimal presensi = khsDto.getPresensi().multiply(kd.getJadwal().getBobotPresensi().divide(new BigDecimal(100)));
//
                khsDto.setTotal(tugas.add(uas).add(uts).add(presensi));
                khsDtos.add(khsDto);
            }
            model.addAttribute("khs",khsDtos);
        }

    }

    @GetMapping("/menumahasiswa/khs/form")
    public void  formKhs(){

    }

}
