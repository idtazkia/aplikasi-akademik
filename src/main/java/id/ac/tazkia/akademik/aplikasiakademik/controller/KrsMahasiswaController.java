package id.ac.tazkia.akademik.aplikasiakademik.controller;

import id.ac.tazkia.akademik.aplikasiakademik.dao.*;
import id.ac.tazkia.akademik.aplikasiakademik.entity.*;
import id.ac.tazkia.akademik.aplikasiakademik.service.CurrentUserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Controller
public class KrsMahasiswaController {

    private static final Logger LOGGER = LoggerFactory.getLogger(KrsMahasiswaController.class);

    @Autowired
    private TahunAkademikDao tahunAkademikDao;
    @Autowired
    private TahunAkademikProdiDao tahunAkademikProdiDao;
    @Autowired
    private MahasiswaDao mahasiswaDao;
    @Autowired
    private KrsDao krsDao;
    @Autowired
    private JadwalDao jadwalDao;
    @Autowired
    private KrsDetailDao krsDetailDao;
    @Autowired
    private CurrentUserService currentUserService;

    @ModelAttribute("listTahunAkademik")
    public Iterable<TahunAkademik> daftarKonfig() {
        return tahunAkademikDao.findByStatusNotInOrderByNamaTahunAkademikDesc(StatusRecord.HAPUS);
    }


    @GetMapping("/menumahasiswa/krs/list")
    public void daftarKRS(Model model, Authentication authentication,Pageable page,
                          @RequestParam(required = false) TahunAkademik tahunAkademik){
        User user = currentUserService.currentUser(authentication);
        Mahasiswa mahasiswa = mahasiswaDao.findByUser(user);
        TahunAkademik ta = tahunAkademikDao.findByStatus(StatusRecord.AKTIF);
        Krs k = krsDao.findByMahasiswaAndTahunAkademik(mahasiswa,ta);

        if (k!= null &&LocalDate.now().compareTo(ta.getTanggalMulaiKrs()) > 0 == true && LocalDate.now().compareTo(ta.getTanggalSelesaiKrs()) < 0 == true){
            model.addAttribute("krsAktif", k);
        }
        model.addAttribute("mahasiswa",mahasiswa);
        TahunAkademikProdi tahunProdi = tahunAkademikProdiDao.findByTahunAkademikStatusAndProdi(StatusRecord.AKTIF,mahasiswa.getIdProdi());

        model.addAttribute("jadwal",jadwalDao.findByTahunAkademikProdiAndIdHariNotNull(tahunProdi));

        if (tahunAkademik != null){
            model.addAttribute("search", tahunAkademik);
            Krs krs = krsDao.findByTahunAkademikAndMahasiswa(tahunAkademik,mahasiswa);
            model.addAttribute("krs",krs);


            model.addAttribute("data",krsDetailDao.findByKrsAndMahasiswaOrderByJadwalIdHariAscJadwalJamMulaiAsc(krs,mahasiswa,page));
        }else {
            Krs krs = krsDao.findByTahunAkademikStatusAndMahasiswa(StatusRecord.AKTIF,mahasiswa);
            model.addAttribute("krs",krs);


            model.addAttribute("data",krsDetailDao.findByKrsAndMahasiswaOrderByJadwalIdHariAscJadwalJamMulaiAsc(krs,mahasiswa,page));
        }



    }

    @PostMapping("/proses/krs")
    public String addKrs(@RequestParam String[] data,Authentication authentication){
        User user = currentUserService.currentUser(authentication);
        Mahasiswa mahasiswa = mahasiswaDao.findByUser(user);
        TahunAkademik tahunAkademik = tahunAkademikDao.findByStatus(StatusRecord.AKTIF);
        TahunAkademikProdi tahunProdi = tahunAkademikProdiDao.findByTahunAkademikStatusAndProdi(StatusRecord.AKTIF,mahasiswa.getIdProdi());

        Krs cariKrs = krsDao.findByTahunAkademikStatusAndMahasiswa(StatusRecord.AKTIF,mahasiswa);

        if (cariKrs != null){
            for (String da : data){
                Jadwal jadwal = jadwalDao.findById(da).get();

                KrsDetail kd = krsDetailDao.findByJadwalAndMahasiswaAndKrs(jadwal,mahasiswa,cariKrs);
                if (kd == null) {
                    KrsDetail krsDetail = new KrsDetail();
                    krsDetail.setJadwal(jadwal);
                    krsDetail.setKrs(cariKrs);
                    krsDetail.setMahasiswa(mahasiswa);
                    krsDetail.setMatakuliahKurikulum(jadwal.getMatakuliahKurikulum());
                    krsDetail.setNilaiPresensi(BigDecimal.ZERO);
                    krsDetail.setNilaiTugas(BigDecimal.ZERO);
                    krsDetail.setNilaiUas(BigDecimal.ZERO);
                    krsDetail.setNilaiUts(BigDecimal.ZERO);
                    krsDetail.setFinalisasi("N");
                    krsDetailDao.save(krsDetail);
                }
            }
        }else {


        }

        return "redirect:/menumahasiswa/krs/list";
    }



}
