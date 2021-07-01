package id.ac.tazkia.smilemahasiswa.controller;


import id.ac.tazkia.smilemahasiswa.dao.*;
import id.ac.tazkia.smilemahasiswa.entity.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

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



    @GetMapping("/api/tahun2")
    @ResponseBody
    public List<Jadwal> tahun(@RequestParam(required = false) String idTahun,
                              @RequestParam(required = false) String idProdi) {

        TahunAkademik tahunAkademik = tahunAkademikDao.findById(idTahun).get();
        Prodi p = prodiDao.findById(idProdi).get();
        List<Jadwal> jadwal = jadwalDao.findByTahunAkademikAndProdiAndHariNotNull(tahunAkademik, p);


        return jadwal;
    }

    @GetMapping("/api/jadwal2")
    @ResponseBody
    public List<KrsDetail> krsDetail(@RequestParam(required = false) String idJadwal,
                               @RequestParam(required = false) String idTahun,
                               @RequestParam(required = false) String idMahasiswa){

        Jadwal jadwal = jadwalDao.findById(idJadwal).get();
        TahunAkademik tahunAkademik = tahunAkademikDao.findById(idTahun).get();
//        Mahasiswa mhs = mahasiswaDao.findByNim(idMahasiswa);
        List<KrsDetail> krsDetail = krsDetailDao.findByStatusAndJadwalOrderByMahasiswaNim(StatusRecord.AKTIF,jadwal);

        return krsDetail;
    }


    @GetMapping("/elearning/importNilai")
    public void importNilai(Model model){

        model.addAttribute("tahunAkademik", tahunAkademikDao.findByStatusNotInOrderByTahunDesc(Arrays.asList(StatusRecord.HAPUS)));
        model.addAttribute("prodi", prodiDao.findByStatus(StatusRecord.AKTIF));
    }


}
