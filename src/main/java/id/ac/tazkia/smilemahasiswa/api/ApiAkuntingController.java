package id.ac.tazkia.smilemahasiswa.api;

import id.ac.tazkia.smilemahasiswa.dao.KrsDetailDao;
import id.ac.tazkia.smilemahasiswa.dao.MahasiswaDao;
import id.ac.tazkia.smilemahasiswa.dao.TahunAkademikDao;
import id.ac.tazkia.smilemahasiswa.dto.api.AkuntingMahasiswaDto;
import id.ac.tazkia.smilemahasiswa.dto.api.BaseResponseDto;
import id.ac.tazkia.smilemahasiswa.entity.Mahasiswa;
import id.ac.tazkia.smilemahasiswa.entity.StatusRecord;
import id.ac.tazkia.smilemahasiswa.entity.TahunAkademik;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

@Controller
@Slf4j
public class ApiAkuntingController {
    @Autowired
    private MahasiswaDao mahasiswaDao;

    @Autowired
    private KrsDetailDao krsDetailDao;

    @Autowired
    private TahunAkademikDao tahunAkademikDao;

    @GetMapping("/api/akunting-mahasiswa/{nim}")
    @ResponseBody
    public ResponseEntity<AkuntingMahasiswaDto> akuntingMahasiswa(@PathVariable String nim) {
        Mahasiswa mahasiswa = mahasiswaDao.findByNim(nim);
        AkuntingMahasiswaDto akuntingMahasiswaDto = new AkuntingMahasiswaDto();
        akuntingMahasiswaDto.setId(mahasiswa.getId());
        akuntingMahasiswaDto.setNim(mahasiswa.getNim());
        akuntingMahasiswaDto.setNama(mahasiswa.getNama());
        akuntingMahasiswaDto.setProdi(mahasiswa.getIdProdi().getNamaProdi());
        akuntingMahasiswaDto.setProgram(mahasiswa.getIdProgram().getNamaProgram());
        akuntingMahasiswaDto.setJenjang(mahasiswa.getIdProdi().getIdJenjang().getKodeJenjang());
        TahunAkademik tahunAkademik = tahunAkademikDao.findByStatus(StatusRecord.AKTIF);

        Integer semester = krsDetailDao.cariSemester(mahasiswa.getId(), tahunAkademik.getId());
        Integer semesterSekarang  = krsDetailDao.cariSemesterSekarang(mahasiswa.getId(), tahunAkademik.getId());
        akuntingMahasiswaDto.setSemester(semester);
        akuntingMahasiswaDto.setSemesterSekarang(semesterSekarang);


        final HttpHeaders httpHeaders= new HttpHeaders();
        httpHeaders.setContentType(MediaType.APPLICATION_JSON);

        return new ResponseEntity<>(akuntingMahasiswaDto, httpHeaders, HttpStatus.OK);
    }
}
