package id.ac.tazkia.akademik.aplikasiakademik.controller;


import id.ac.tazkia.akademik.aplikasiakademik.dao.*;
import id.ac.tazkia.akademik.aplikasiakademik.dto.SesiDto;
import id.ac.tazkia.akademik.aplikasiakademik.entity.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.ArrayList;
import java.util.List;

@Controller
public class JadwalDosenController {

    @Autowired
    private HariDao hariDao;

    @Autowired
    private JadwalDao jadwalDao;

    @Autowired
    private TahunAkademikDao tahunAkademikDao;

    @Autowired
    private DosenDao dosenDao;



    @GetMapping("/jadwaldosen/list")
    public void listJadwalDosen(Model model,@RequestParam(required = false) Dosen dosen) {

        TahunAkademik tahunAkademik = tahunAkademikDao.findByStatus(StatusRecord.AKTIF);
        model.addAttribute("dosen",dosenDao.findByStatusNotIn(StatusRecord.AKTIF));
        model.addAttribute("selectedDosen", dosen);


        if (dosen != null){


            List<Hari> hari = hariDao.findByStatus(StatusRecord.AKTIF);
            List<SesiDto> sesi = new ArrayList<>();
            for (Hari hari1 : hari ){

                SesiDto sesiDto = new SesiDto();
                sesiDto.setId(hari1.getId());
                sesiDto.setHari(hari1.getNamaHariEng());

                List<Jadwal> jadwal1= jadwalDao.findByStatusAndTahunAkademikAndDosenAndHari
                        (StatusRecord.AKTIF,tahunAkademik,dosen,hari1);

                for (Jadwal jh : jadwal1){

                    if (jh.getSesi().equals("1")){
                        sesiDto.setSesi1(jh.getMatakuliahKurikulum().getMatakuliah().getNamaMatakuliah());
                        sesiDto.setRuangan1(jh.getRuangan().getNamaRuangan());
                        sesiDto.setKelas1(jh.getKelas().getNamaKelas());
                    }

                    if (jh.getSesi().equals("2")){
                        sesiDto.setSesi2(jh.getMatakuliahKurikulum().getMatakuliah().getNamaMatakuliah());
                        sesiDto.setRuangan2(jh.getRuangan().getNamaRuangan());
                        sesiDto.setKelas2(jh.getKelas().getNamaKelas());
                    }

                    if(jh.getSesi().equals("3")){
                        sesiDto.setSesi3(jh.getMatakuliahKurikulum().getMatakuliah().getNamaMatakuliah());
                        sesiDto.setRuangan3(jh.getRuangan().getNamaRuangan());
                        sesiDto.setKelas3(jh.getKelas().getNamaKelas());
                    }

                    if (jh.getSesi().equals("4")){
                        sesiDto.setSesi4(jh.getMatakuliahKurikulum().getMatakuliah().getNamaMatakuliah());
                        sesiDto.setRuangan4(jh.getRuangan().getNamaRuangan());
                        sesiDto.setKelas4(jh.getKelas().getNamaKelas());
                    }

                }

                sesi.add(sesiDto);

            }

            model.addAttribute("jadwalDosen",sesi);

        }

    }
}



