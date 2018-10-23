package id.ac.tazkia.akademik.aplikasiakademik.controller;

import id.ac.tazkia.akademik.aplikasiakademik.dao.*;
import id.ac.tazkia.akademik.aplikasiakademik.dto.TranskriptDto;
import id.ac.tazkia.akademik.aplikasiakademik.entity.KrsDetail;
import id.ac.tazkia.akademik.aplikasiakademik.entity.Mahasiswa;
import id.ac.tazkia.akademik.aplikasiakademik.entity.StatusRecord;
import id.ac.tazkia.akademik.aplikasiakademik.entity.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Controller
public class TranskriptMahasiswaController {
    private static final Logger LOGGER = LoggerFactory.getLogger(TranskriptMahasiswaController.class);

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

    @GetMapping("/menumahasiswa/transkript/list")
    public void daftarTranskript(Model model, Authentication currentUser){
        model.addAttribute("gradeA", gradeDao.findById("1").get());
        model.addAttribute("grademinA", gradeDao.findById("2").get());
        model.addAttribute("gradeplusB", gradeDao.findById("3").get());
        model.addAttribute("gradeB", gradeDao.findById("4").get());
        model.addAttribute("grademinB", gradeDao.findById("5").get());
        model.addAttribute("gradeplusC", gradeDao.findById("6").get());
        model.addAttribute("gradeC", gradeDao.findById("7").get());
        model.addAttribute("gradeD", gradeDao.findById("8").get());
        model.addAttribute("gradeE", gradeDao.findById("9").get());

        LOGGER.debug("Authentication class : {}", currentUser.getClass().getName());

        if (currentUser == null) {
            LOGGER.warn("user tidak ditemukan");
        }

        String username = ((UserDetails) currentUser.getPrincipal()).getUsername();
        User user = userDao.findByUsername(username);

        Mahasiswa mahasiswa = mahasiswaDao.findByUser(user);
        model.addAttribute("mahasiswa",mahasiswa);

        model.addAttribute("krs",krsDao.findByMahasiswa(mahasiswa));

        List<KrsDetail> krsDetail = krsDetailDao.findByMahasiswaAndStatusOrderByKrsTahunAkademikDesc(mahasiswa,StatusRecord.AKTIF);
        List<TranskriptDto> transkriptDtos = new ArrayList<>();
        for (KrsDetail kd : krsDetail){
            TranskriptDto transkriptDto = new TranskriptDto();
            transkriptDto.setKode(kd.getMatakuliahKurikulum().getMatakuliah().getKodeMatakuliah());
            transkriptDto.setMapel(kd.getMatakuliahKurikulum().getMatakuliah().getNamaMatakuliahEnglish());
            transkriptDto.setPresensi(kd.getNilaiPresensi());
            transkriptDto.setTahunAkademik(kd.getKrs().getTahunAkademik().getNamaTahunAkademik());
            transkriptDto.setTugas(kd.getNilaiTugas());
            transkriptDto.setUas(kd.getNilaiUas());
            transkriptDto.setUts(kd.getNilaiUts());
            transkriptDto.setSks(kd.getMatakuliahKurikulum().getJumlahSks());

            BigDecimal tugas = transkriptDto.getTugas().multiply(kd.getJadwal().getBobotTugas().divide(new BigDecimal(100)));
            BigDecimal uas = transkriptDto.getUas().multiply(kd.getJadwal().getBobotUas().divide(new BigDecimal(100)));
            BigDecimal uts =transkriptDto.getUts().multiply(kd.getJadwal().getBobotUts().divide(new BigDecimal(100)));
            BigDecimal presensi = transkriptDto.getPresensi().multiply(kd.getJadwal().getBobotPresensi().divide(new BigDecimal(100)));
//
            transkriptDto.setTotal(tugas.add(uas).add(uts).add(presensi));
            transkriptDtos.add(transkriptDto);
        }
        model.addAttribute("transkript", transkriptDtos);

    }

    @GetMapping("/menumahasiswa/transkript/form")
    public void  formTranskript(){

    }
}
