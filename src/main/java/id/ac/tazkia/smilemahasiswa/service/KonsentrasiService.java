package id.ac.tazkia.smilemahasiswa.service;

import id.ac.tazkia.smilemahasiswa.dao.JenjangDao;
import id.ac.tazkia.smilemahasiswa.dao.KonsentrasiDao;
import id.ac.tazkia.smilemahasiswa.dto.konsentrasi.ListKonsentrasiDto;
import id.ac.tazkia.smilemahasiswa.entity.Jenjang;
import id.ac.tazkia.smilemahasiswa.entity.Konsentrasi;
import id.ac.tazkia.smilemahasiswa.entity.StatusRecord;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.ArrayList;
import java.util.List;

@Service
public class KonsentrasiService {

    @Autowired private KonsentrasiDao konsentrasiDao;
    @Autowired private JenjangDao jenjangDao;

    // Konsentrasi
    public List<ListKonsentrasiDto> getListKonsentrasiS1(){
        Jenjang jenjangS1 = jenjangDao.findById("01").get();
        List<Konsentrasi> konsentrasis = konsentrasiDao.findByStatusAndIdProdiIdJenjangOrderByIdProdiNamaProdi(StatusRecord.AKTIF, jenjangS1);
        List<ListKonsentrasiDto> listKonsentrasiDtos = new ArrayList<>();

        for (Konsentrasi konsentrasi : konsentrasis){
            ListKonsentrasiDto konsentrasiDto = new ListKonsentrasiDto();

            konsentrasiDto.setId(konsentrasi.getId());
            konsentrasiDto.setNamaProdi(konsentrasi.getIdProdi().getNamaProdi());
            konsentrasiDto.setJenjangProdi(konsentrasi.getIdProdi().getIdJenjang().getId());
            konsentrasiDto.setKodeKonsentrasi(konsentrasi.getKodeKonsentrasi());
            konsentrasiDto.setNamaKonsentrasi(konsentrasi.getNamaKonsentrasi());
            konsentrasiDto.setKodeSpmb(konsentrasi.getIdProdi().getKodeSpmb());

            listKonsentrasiDtos.add(konsentrasiDto);
        }

        return listKonsentrasiDtos;
    }

    // Konsentrasi
    public List<ListKonsentrasiDto> getListKonsentrasiS2(){
        Jenjang jenjangS1 = jenjangDao.findById("02").get();
        List<Konsentrasi> konsentrasis = konsentrasiDao.findByStatusAndIdProdiIdJenjangOrderByIdProdiNamaProdi(StatusRecord.AKTIF, jenjangS1);
        List<ListKonsentrasiDto> listKonsentrasiDtos = new ArrayList<>();

        for (Konsentrasi konsentrasi : konsentrasis){
            ListKonsentrasiDto konsentrasiDto = new ListKonsentrasiDto();

            konsentrasiDto.setId(konsentrasi.getId());
            konsentrasiDto.setNamaProdi(konsentrasi.getIdProdi().getNamaProdi());
            konsentrasiDto.setJenjangProdi(konsentrasi.getIdProdi().getIdJenjang().getId());
            konsentrasiDto.setKodeKonsentrasi(konsentrasi.getKodeKonsentrasi());
            konsentrasiDto.setNamaKonsentrasi(konsentrasi.getNamaKonsentrasi());
            konsentrasiDto.setKodeSpmb(konsentrasi.getIdProdi().getKodeSpmb());

            listKonsentrasiDtos.add(konsentrasiDto);
        }

        return listKonsentrasiDtos;
    }

}
