package id.ac.tazkia.smilemahasiswa.controller;


import id.ac.tazkia.smilemahasiswa.dao.*;
import id.ac.tazkia.smilemahasiswa.dto.elearning.MdlGradeGradesDto;
import id.ac.tazkia.smilemahasiswa.entity.*;
import org.apache.commons.lang3.RandomStringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

@Controller
public class ElearningController {

    @Autowired
    private ProdiDao prodiDao;

    @Autowired
    private JadwalDao jadwalDao;

    @Autowired
    private MahasiswaDao mahasiswaDao;

    @Autowired
    private TahunAkademikDao tahunAkademikDao;

    @Autowired
    private KrsDetailDao krsDetailDao;

    @Autowired
    private KrsDao krsDao;

    @Autowired
    private UserDao userDao;

    WebClient webClient1 = WebClient.builder()
            .baseUrl("http://localhost:8081")
            .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
            .build();


    public List<MdlGradeGradesDto> getNilaiTugas2(@RequestParam String jadwal) {
        return webClient1.get()
                .uri("/api/nilaitugas2" + jadwal)
                .retrieve().bodyToFlux(MdlGradeGradesDto.class)
                .collectList()
                .block();
    }

    public List<MdlGradeGradesDto> getNilaiUts2(@RequestParam String jadwal) {
        return webClient1.get()
                .uri("/api/nilaiuts2?jadwal=" + jadwal)
                .retrieve().bodyToFlux(MdlGradeGradesDto.class)
                .collectList()
                .block();
    }

    public List<MdlGradeGradesDto> getNilaiUas2(@RequestParam String jadwal) {
        return webClient1.get()
                .uri("/api/nilaiuas2" + jadwal)
                .retrieve().bodyToFlux(MdlGradeGradesDto.class)
                .collectList()
                .block();
    }


    public List<MdlGradeGradesDto> getNilaiTugasPerMhs(@RequestParam String jadwal, @RequestParam String mahasiswa) {
        return webClient1.get()
                .uri("/api/nilaitugaspermhs" + jadwal + mahasiswa)
                .retrieve().bodyToFlux(MdlGradeGradesDto.class)
                .collectList()
                .block();
    }

    public List<MdlGradeGradesDto> getNilaiUtsPerMhs(@RequestParam String jadwal, @RequestParam String mahasiswa) {
        return webClient1.get()
                .uri("/api/nilaiutspermhs" + jadwal + mahasiswa)
                .retrieve().bodyToFlux(MdlGradeGradesDto.class)
                .collectList()
                .block();
    }

    public List<MdlGradeGradesDto> getNilaiUasPerMhs(@RequestParam String jadwal, @RequestParam String mahasiswa) {
        return webClient1.get()
                .uri("/api/nilaiuaspermhs" + jadwal + mahasiswa)
                .retrieve().bodyToFlux(MdlGradeGradesDto.class)
                .collectList()
                .block();
    }


    @GetMapping("/api/prodi2")
    @ResponseBody
    public List<Prodi> tahun() {

        List<Prodi> prodi = prodiDao.findByStatus(StatusRecord.AKTIF);


        return prodi;
    }

    @GetMapping("/api/tahun2")
    @ResponseBody
    public List<Jadwal> tahun(@RequestParam(required = false) String ta,
                              @RequestParam(required = false) String prodi) {

        TahunAkademik tahunAkademik = tahunAkademikDao.findById(ta).get();
        Prodi p = prodiDao.findById(prodi).get();
        List<Jadwal> jadwal = jadwalDao.findByTahunAkademikAndProdiAndHariNotNull(tahunAkademik, p);


        return jadwal;
    }

    @GetMapping("/api/jadwal2")
    @ResponseBody
    public List<KrsDetail> krsDetail(@RequestParam(required = false) String jadwal,
                               @RequestParam(required = false) String ta,
                               @RequestParam(required = false) String mahasiswa){

        Jadwal jadwal1 = jadwalDao.findById(jadwal).get();
        TahunAkademik tahunAkademik = tahunAkademikDao.findById(ta).get();
//        Mahasiswa mhs = mahasiswaDao.findByNim(mahasiswa);
        List<KrsDetail> krsDetail = krsDetailDao.findByStatusAndJadwalOrderByMahasiswaNim(StatusRecord.AKTIF,jadwal1);

        return krsDetail;
    }


    @GetMapping("/elearning/importNilai")
    public void importNilai(Model model){

        model.addAttribute("tahunAkademik", tahunAkademikDao.findByStatusNotInOrderByTahunDesc(Arrays.asList(StatusRecord.HAPUS)));
        model.addAttribute("prodi", prodiDao.findByStatus(StatusRecord.AKTIF));
    }


    @PostMapping("/elearning/importNilai")
    public String inputForm(@RequestParam(required = false) String ta, @RequestParam(required = false) String prodi,
                            @RequestParam(required = false) String jadwal,@RequestParam(required = false) String nim, RedirectAttributes attributes){

        TahunAkademik tahunAkademik1 = tahunAkademikDao.findById(ta).get();
        Prodi prodi1 = prodiDao.findById(prodi).get();
        Jadwal jadwal1 = jadwalDao.findById(jadwal).get();
        Mahasiswa mhs = mahasiswaDao.findByNim(nim);



        //uts per jadwal
        List<MdlGradeGradesDto> daftarNilaiUts = getNilaiUts2(jadwal);
        for (MdlGradeGradesDto mdlniluts : daftarNilaiUts){
            Jadwal j = jadwalDao.findById(mdlniluts.getIdJadwal()).get();

            System.out.println("TA  =" + tahunAkademik1);
            System.out.println("PRODI =" + prodi1);
            System.out.println("JADWAL =" + jadwal);

            if (mdlniluts.getMahasiswa() != null) {
                User user = userDao.findByUsername(mdlniluts.getMahasiswa());

                if (user != null) {
                    Mahasiswa mahasiswa = mahasiswaDao.findByUser(user);
                    Krs k = krsDao.findByMahasiswaAndTahunAkademikAndStatus(mahasiswa, tahunAkademik1, StatusRecord.AKTIF);
                    if (k != null) {

//                        Object krsDetail2 = krsDetailDao.getKrsDetailId3(tahunAkademik1, prodi, jadwal1, StatusRecord.AKTIF);
                        KrsDetail krsDetail2 = krsDetailDao.findByTahunAkademikAndJadwalProdiAndJadwalAndStatus(tahunAkademik1, prodi1, jadwal1, StatusRecord.AKTIF);
                        if (krsDetail2 != null) {
                            krsDetail2.setNilaiUts(mdlniluts.getNilai());
//                            krsDetailDao.save(krsDetail2);
                            System.out.println(" JADWAL == " + mdlniluts.getIdJadwal());
                            System.out.println(" Mahasiswa == " + mdlniluts.getMahasiswa());
                            System.out.println(" NILAI UTS UPDATED == " + mdlniluts.getId());
                            System.out.println("  =======  ");
                        }
                    }
                }
            }

        }


        return "redirect:importNilai";

    }


}
