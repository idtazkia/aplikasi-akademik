package id.ac.tazkia.smilemahasiswa.controller;

import id.ac.tazkia.smilemahasiswa.dao.JadwalDao;
import id.ac.tazkia.smilemahasiswa.dao.KaryawanDao;
import id.ac.tazkia.smilemahasiswa.dao.KelasDao;
import id.ac.tazkia.smilemahasiswa.dto.courses.DetailJadwalDto;
import id.ac.tazkia.smilemahasiswa.dto.courses.DetailJadwalIntDto;
import id.ac.tazkia.smilemahasiswa.dto.human.KaryawanDto;
import id.ac.tazkia.smilemahasiswa.dto.human.KaryawanIntDto;
import id.ac.tazkia.smilemahasiswa.dto.kelas.KelasDto;
import id.ac.tazkia.smilemahasiswa.dto.kelas.KelasIntDto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;

@RestController
public class SmileApiController {

    @Autowired
    private JadwalDao jadwalDao;
    @Autowired
    private KelasDao kelasDao;
    @Autowired
    private KaryawanDao karyawanDao;

    //detailJadwal
    @GetMapping("/api/getDetailJadwal")
    @ResponseBody
    public List<DetailJadwalDto> getDetailJadwal(){

        List<DetailJadwalIntDto> alog = jadwalDao.getDetailJadwal();
        List<DetailJadwalDto> adto = new ArrayList<>();

        for (DetailJadwalIntDto detailJadwalIntDto : alog){
            DetailJadwalDto detailJadwalDto = new DetailJadwalDto();

            detailJadwalDto.setId(detailJadwalIntDto.getId());
            detailJadwalDto.setNamaProdi(detailJadwalIntDto.getNamaProdi());
            detailJadwalDto.setNamaKelas(detailJadwalIntDto.getNamaKelas());
            detailJadwalDto.setKodeMatakuliah(detailJadwalIntDto.getKodeMatakuliah());
            detailJadwalDto.setNamaMatakuliah(detailJadwalIntDto.getNamaMatakuliah());
            detailJadwalDto.setNamaMatakuliahEnglish(detailJadwalIntDto.getNamaMatakuliahEnglish());
            detailJadwalDto.setIdDosen(detailJadwalIntDto.getIdDosen());
            detailJadwalDto.setDosen(detailJadwalIntDto.getDosen());
            detailJadwalDto.setJamMulai(detailJadwalIntDto.getJamMulai());
            detailJadwalDto.setJamSelesai(detailJadwalIntDto.getJamSelesai());
            detailJadwalDto.setIdNumberElearning(detailJadwalIntDto.getIdNumberElearning());
            detailJadwalDto.setIdTahunAkademik(detailJadwalIntDto.getIdTahunAkademik());
            detailJadwalDto.setStatus(detailJadwalIntDto.getStatus());

            adto.add(detailJadwalDto);
        }

        return adto;

    }


    //kelas
    @GetMapping("/api/getKelas")
    @ResponseBody
    public List<KelasDto> getKelas(){

        List<KelasIntDto> alog = kelasDao.apiKelas();
        List<KelasDto> adto = new ArrayList<>();

        for (KelasIntDto kelasIntDto : alog){
            KelasDto kelasDto = new KelasDto();

            kelasDto.setIdKelas(kelasIntDto.getIdKelas());
            kelasDto.setKodeKelas(kelasIntDto.getKodeKelas());
            kelasDto.setNamaKelas(kelasIntDto.getNamaKelas());
            kelasDto.setKeterangan(kelasIntDto.getKeterangan());
            kelasDto.setIdProdi(kelasIntDto.getIdProdi());
            kelasDto.setStatus(kelasIntDto.getStatus());
            kelasDto.setKurikulum(kelasIntDto.getKurikulum());
            kelasDto.setKonsentrasi(kelasIntDto.getKonsentrasi());
            kelasDto.setAngkatan(kelasIntDto.getAngkatan());
            kelasDto.setBahasa(kelasIntDto.getBahasa());


            adto.add(kelasDto);
        }

        return adto;

    }

    //kelas
    @GetMapping("/api/apiGetKaryawan")
    @ResponseBody
    public List<KaryawanDto> getKaryawan(){

        List<KaryawanIntDto> alog = karyawanDao.apiGetKaryawan();
        List<KaryawanDto> adto = new ArrayList<>();

        for (KaryawanIntDto karyawanIntDto : alog){
            KaryawanDto karyawanDto = new KaryawanDto();

            karyawanDto.setId(karyawanIntDto.getId());
            karyawanDto.setNik(karyawanIntDto.getNik());
            karyawanDto.setNamaKaryawan(karyawanIntDto.getNamaKaryawan());
            karyawanDto.setGelar(karyawanIntDto.getGelar());
            karyawanDto.setJenisKelamin(karyawanIntDto.getJenisKelamin());
            karyawanDto.setStatus(karyawanIntDto.getStatus());
            karyawanDto.setIdUser(karyawanIntDto.getIdUser());
            karyawanDto.setNidn(karyawanIntDto.getNidn());
            karyawanDto.setEmail(karyawanIntDto.getEmail());
            karyawanDto.setTanggalLahir(karyawanIntDto.getTanggalLahir());
            karyawanDto.setRfid(karyawanIntDto.getRfid());
            karyawanDto.setIdAbsen(karyawanIntDto.getIdAbsen());
            karyawanDto.setFoto(karyawanIntDto.getFoto());

            adto.add(karyawanDto);
        }

        return adto;

    }
}
