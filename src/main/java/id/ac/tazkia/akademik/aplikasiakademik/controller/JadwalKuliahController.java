package id.ac.tazkia.akademik.aplikasiakademik.controller;

import id.ac.tazkia.akademik.aplikasiakademik.dao.*;
import id.ac.tazkia.akademik.aplikasiakademik.entity.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import javax.validation.Valid;
import java.math.BigDecimal;

@Controller
public class JadwalKuliahController {

    @Autowired
    private ProgramDao programDao;

    @Autowired
    private MatakuliahKurikulumDao matakuliahKurikulumDao;

    @Autowired
    private TahunAkademikProdiDao tahunAkademikProdiDao;

    @Autowired
    private JadwalDao jadwalDao;

    @Autowired
    private DosenDao dosenDao;

    @Autowired
    private KelasDao kelasDao;

    @Autowired
    private RuanganDao ruanganDao;

    @ModelAttribute("dosen")
    public Iterable<Dosen> dosen() {
        return dosenDao.findByStatusNotIn(StatusRecord.HAPUS);
    }

    @ModelAttribute("ruangan")
    public Iterable<Ruangan> ruangan() {
        return ruanganDao.findByStatusNotIn(StatusRecord.HAPUS);
    }

    @ModelAttribute("kelas")
    public Iterable<Kelas> kelas() {
        return kelasDao.findByStatusNotIn(StatusRecord.HAPUS);
    }

    @ModelAttribute("tahunAkademik")
    public Iterable<TahunAkademikProdi> tahunAkademik() {
        return tahunAkademikProdiDao.findByStatusNotInOrderByTahunAkademikDesc(StatusRecord.HAPUS);
    }

    @ModelAttribute("program")
    public Iterable<Program> program() {
        return programDao.findByStatusNotIn(StatusRecord.HAPUS);
    }


    @GetMapping("/plotingdosen/list")
    public void plotList(Model model,@PageableDefault(direction = Sort.Direction.DESC) Pageable pageable,
                           @RequestParam(required = false)Program program,
                            @RequestParam(required = false) TahunAkademikProdi tahunAkademik){

        if (program != null && tahunAkademik != null){
            model.addAttribute("matakuliah",matakuliahKurikulumDao.findByStatusNotInAndKurikulum(StatusRecord.HAPUS,tahunAkademik.getIdKurikulum()));
            model.addAttribute("selectedTahun",tahunAkademik);
            model.addAttribute("selectedProgram",program);
            model.addAttribute("jadwal", jadwalDao.findByStatusNotInAndProdiAndTahunAkademikProdi(StatusRecord.HAPUS,tahunAkademik.getProdi(),tahunAkademik,pageable));
        }
    }

    @PostMapping("/plotingdosen/list")
    public String buatPlot(@ModelAttribute @Valid Jadwal jadwal){
        jadwal.setJumlahSesi(1);
        jadwal.setBobotUts(BigDecimal.ZERO);
        jadwal.setBobotUas(BigDecimal.ZERO);
        jadwal.setBobotPresensi(BigDecimal.ZERO);
        jadwal.setBobotTugas(BigDecimal.ZERO);
        jadwalDao.save(jadwal);
        System.out.println(jadwal.getId());

        return "redirect:list?tahunAkademik="+jadwal.getTahunAkademikProdi().getId()+"&program="+jadwal.getProgram().getId();
    }

    @GetMapping("/jadwalkuliah/list")
    public void jadwalList(){}
}
