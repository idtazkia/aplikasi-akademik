package id.ac.tazkia.akademik.aplikasiakademik.controller;

import id.ac.tazkia.akademik.aplikasiakademik.dao.*;
import id.ac.tazkia.akademik.aplikasiakademik.entity.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

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

    @Autowired
    private HariDao hariDao;

    @Autowired
    private SesiDao sesiDao;

    @Autowired
    private TahunAkademikDao tahunAkademikDao;

    @Autowired
    KurikulumDao kurikulumDao;

    @Autowired
    private KelasMahasiswaDao kelasMahasiswaDao;

    @GetMapping("/api/plotKelas")
    @ResponseBody
    public Page<Kelas> cariDataKelas(@RequestParam(required = false) String search, Pageable page){
        if(!StringUtils.hasText(search)) {
            return kelasDao.findAll(page);
        }
        return kelasDao.findByStatusAndNamaKelasContainingIgnoreCaseOrderByNamaKelas(StatusRecord.AKTIF,search, page);

    }

    @GetMapping("/api/hari")
    @ResponseBody
    public Iterable<Hari> cariHari(@RequestParam(required = false) String search, Pageable page){
        if(!StringUtils.hasText(search)) {
            return hariDao.findAll();
        }
        return hariDao.findByNamaHariContainingIgnoreCaseOrNamaHariEngContainingIgnoreCase(search,search);

    }

    @GetMapping("/api/waktu")
    @ResponseBody
    public Sesi sesi(@RequestParam(required = false) String id){


        return sesiDao.findById(id).get();

    }

    @GetMapping("/api/ruangan")
    @ResponseBody
    public Iterable<Ruangan> cariRuangan(@RequestParam(required = false) String search, Pageable page){
        if(!StringUtils.hasText(search)) {
            return ruanganDao.findAll();
        }
        return ruanganDao.findByStatusAndNamaRuanganContainingIgnoreCase(StatusRecord.AKTIF,search);

    }

    @GetMapping("/api/sesi")
    @ResponseBody
    public List<Sesi> cariSesi(@RequestParam(required = false) String idHari,@RequestParam(required = false) String kelas,
                               @RequestParam(required = false) String idRuangan, @RequestParam(required = false) Integer sks,
                               @RequestParam(required = false) String search, @RequestParam(required = false) String dosen){
        Ruangan ruangan = ruanganDao.findById(idRuangan).get();
        Kelas k = kelasDao.findById(kelas).get();
        Hari hari = hariDao.findById(idHari).get();
        Dosen d = dosenDao.findById(dosen).get();
        List<Jadwal> jadwal = jadwalDao.cariSesi(tahunAkademikDao.findByStatus(StatusRecord.AKTIF),hari,ruangan);
        List<Jadwal> cariKelas = jadwalDao.cariKelas(tahunAkademikDao.findByStatus(StatusRecord.AKTIF),hari,k);
        List<Jadwal> validasiDosen = jadwalDao.validasiDosen(tahunAkademikDao.findByStatus(StatusRecord.AKTIF),hari,d);
        List<Jadwal> stringList = new ArrayList<>();
        stringList.addAll(jadwal);
        stringList.addAll(cariKelas);
        stringList.addAll(validasiDosen);

        List<Jadwal> newList = stringList.stream()
                .distinct()
                .collect(Collectors.toList());
        System.out.println(newList);
        System.out.println(validasiDosen);


        if (newList == null || newList.isEmpty()){
            return sesiDao.findBySks(sks);
        }else {
            return sesiDao.findBySesiNotInAndSks(newList, sks);
        }
    }

    @GetMapping("/api/plotMatakuliah")
    @ResponseBody
    public Page<MatakuliahKurikulum> cariMatakuliah(@RequestParam(required = false) String id,@RequestParam(required = false) String search, Pageable page){
        if(!StringUtils.hasText(search)) {
            return matakuliahKurikulumDao.findAll(page);
        }
        KelasMahasiswa kelasMahasiswa = kelasMahasiswaDao.findFirstByKelasAndStatusAndMahasiswaKurikulumNotNull(kelasDao.findById(id).get(),StatusRecord.AKTIF);
        Kurikulum kurikulum = kurikulumDao.findByIdAndStatus(kelasMahasiswa.getMahasiswa().getKurikulum().getId(),StatusRecord.AKTIF);
        return matakuliahKurikulumDao.findByMatakuliahNamaMatakuliahContainingIgnoreCaseAndKurikulumAndStatusOrMatakuliahNamaMatakuliahEnglishContainingIgnoreCaseAndKurikulumAndStatus(search,kurikulum,StatusRecord.AKTIF,search,kurikulum,StatusRecord.AKTIF,page);

    }

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

    @ModelAttribute("hari")
    public Iterable<Hari> hari() {
        return hariDao.findAll();
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

    @PostMapping("/plotingdosen/delete")
    public String deleteJadwal(@RequestParam Jadwal jadwal){
        jadwal.setStatus(StatusRecord.HAPUS);
        return "redirect:list?tahunAkademik="+jadwal.getTahunAkademikProdi().getId()+"&program="+jadwal.getProgram().getId();
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
    public void jadwalList(Model model, @RequestParam(required = false) Program program,
                           @RequestParam(required = false) TahunAkademikProdi tahunAkademik,
                           @RequestParam(required = false) Hari hari){
        model.addAttribute("selectedTahun",tahunAkademik);
        model.addAttribute("selectedHari", hari);
        model.addAttribute("selectedProgram",program);

        if (program != null && tahunAkademik != null && hari != null){
            model.addAttribute("jadwal", jadwalDao.findByStatusNotInAndProdiAndTahunAkademikProdiAndIdHariAndProgram(StatusRecord.HAPUS,tahunAkademik.getProdi(),tahunAkademik,hari,program));
            model.addAttribute("ploting", jadwalDao.findByStatusNotInAndProdiAndTahunAkademikProdiAndIdHariNullAndJamMulaiNullAndJamSelesaiNull(StatusRecord.HAPUS,tahunAkademik.getProdi(),tahunAkademik));
        }


        if (program != null && tahunAkademik != null && hari == null){
            model.addAttribute("ploting", jadwalDao.findByStatusNotInAndProdiAndTahunAkademikProdiAndIdHariNullAndJamMulaiNullAndJamSelesaiNull(StatusRecord.HAPUS,tahunAkademik.getProdi(),tahunAkademik));
            model.addAttribute("minggu", jadwalDao.findByStatusNotInAndProdiAndTahunAkademikProdiAndIdHariIdAndProgram(StatusRecord.HAPUS,tahunAkademik.getProdi(),tahunAkademik,"0",program));
            model.addAttribute("senin", jadwalDao.findByStatusNotInAndProdiAndTahunAkademikProdiAndIdHariIdAndProgram(StatusRecord.HAPUS,tahunAkademik.getProdi(),tahunAkademik,"1",program));
            model.addAttribute("selasa", jadwalDao.findByStatusNotInAndProdiAndTahunAkademikProdiAndIdHariIdAndProgram(StatusRecord.HAPUS,tahunAkademik.getProdi(),tahunAkademik,"2",program));
            model.addAttribute("rabu", jadwalDao.findByStatusNotInAndProdiAndTahunAkademikProdiAndIdHariIdAndProgram(StatusRecord.HAPUS,tahunAkademik.getProdi(),tahunAkademik,"3",program));
            model.addAttribute("kamis", jadwalDao.findByStatusNotInAndProdiAndTahunAkademikProdiAndIdHariIdAndProgram(StatusRecord.HAPUS,tahunAkademik.getProdi(),tahunAkademik,"4",program));
            model.addAttribute("jumat", jadwalDao.findByStatusNotInAndProdiAndTahunAkademikProdiAndIdHariIdAndProgram(StatusRecord.HAPUS,tahunAkademik.getProdi(),tahunAkademik,"5",program));
            model.addAttribute("sabtu", jadwalDao.findByStatusNotInAndProdiAndTahunAkademikProdiAndIdHariIdAndProgram(StatusRecord.HAPUS,tahunAkademik.getProdi(),tahunAkademik,"6",program));
        }

    }

    @GetMapping("/jadwalkuliah/form")
    public void formJadwal(Model model,@RequestParam(required = false) Jadwal jadwal,@RequestParam(required = false) String plot){
        if (plot != null){
            model.addAttribute("plot",plot);
        }

        model.addAttribute("matakuliah",matakuliahKurikulumDao.findByStatusNotInAndKurikulum(StatusRecord.HAPUS,jadwal.getMatakuliahKurikulum().getKurikulum()));
        model.addAttribute("jadwal",jadwal);
        model.addAttribute("hari", hariDao.findAll());
    }

    @PostMapping("/jadwalkuliah/form")
    public String prosesJadwal(@ModelAttribute @Valid Jadwal jadwal,@RequestParam(required = false) String plot){
        

        List<Jadwal> jdwl = jadwalDao.cariJadwal(jadwal.getDosen(), jadwal.getId(),jadwal.getTahunAkademikProdi(),jadwal.getIdHari(),jadwal.getRuangan(),jadwal.getJamMulai(),jadwal.getJamSelesai(),jadwal.getJamMulai());


        if (jdwl == null || jdwl.isEmpty()) {
            jadwal.setJamMulai(jadwal.getJamMulai());
            jadwal.setJamSelesai(jadwal.getJamSelesai());
            jadwalDao.save(jadwal);
        }else {

            System.out.println("gabisa");
            return "redirect:form?jadwal=" + jadwal.getId();
        }

        if (plot != null){
            return "redirect:/plotingdosen/list?tahunAkademik="+jadwal.getTahunAkademikProdi().getId()+"&program="+jadwal.getProgram().getId();
        }

        return "redirect:list?tahunAkademik="+ jadwal.getTahunAkademikProdi().getId() +"&program="+jadwal.getProgram().getId();
    }
}
