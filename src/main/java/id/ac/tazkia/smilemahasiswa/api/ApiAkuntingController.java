package id.ac.tazkia.smilemahasiswa.api;

import id.ac.tazkia.smilemahasiswa.dao.KrsDetailDao;
import id.ac.tazkia.smilemahasiswa.dao.MahasiswaDao;
import id.ac.tazkia.smilemahasiswa.dao.TagihanDao;
import id.ac.tazkia.smilemahasiswa.dao.TahunAkademikDao;
import id.ac.tazkia.smilemahasiswa.dto.BaseResponse;
import id.ac.tazkia.smilemahasiswa.dto.api.BaseResponseDto;
import id.ac.tazkia.smilemahasiswa.dto.api.akunting.AkuntingMahasiswaDto;
import id.ac.tazkia.smilemahasiswa.dto.api.akunting.DataTagihanAkunDto;
import id.ac.tazkia.smilemahasiswa.dto.api.akunting.DataTagihanPerbulanTahunDto;
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

import java.util.List;

@Controller
@Slf4j
public class ApiAkuntingController {
    @Autowired private MahasiswaDao mahasiswaDao;

    @Autowired private KrsDetailDao krsDetailDao;

    @Autowired private TahunAkademikDao tahunAkademikDao;

    @Autowired private TagihanDao tagihanDao;

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

    @GetMapping("/api/get-tagihan-pembayaran-perbulan")
    @ResponseBody
    public ResponseEntity<BaseResponseDto> dataTagihanPerbulanTahunDtoResponseEntity(@RequestParam String tahun, @RequestParam List<String> kodeTemplate){
        try{
            List<DataTagihanPerbulanTahunDto> listData = tagihanDao.getDataTagihanPembayaranPerKodeTemplatePerTahun(tahun, kodeTemplate);
            if (listData.isEmpty()){
                return ResponseEntity.ok().body(
                        BaseResponseDto.builder()
                                .responseCode("200")
                                .responseMessage("Data Kosong")
                                .build());
            }else{
                return ResponseEntity.ok().body(
                        BaseResponseDto.builder()
                                .responseCode("200")
                                .responseMessage("Data ditemukan")
                                .data(listData)
                                .build());
            }
        }catch (Exception e){
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                    BaseResponseDto.builder()
                            .responseCode("500")
                            .responseMessage(e.getLocalizedMessage())
                            .build());
        }
    }

    @GetMapping("/api/get-tagihan-akun")
    @ResponseBody
    public ResponseEntity<BaseResponseDto> getDataTagihanPerAkun(@RequestParam String tahun, @RequestParam List<String> kodeTemplate){
        try{
            List<DataTagihanAkunDto> listData = tagihanDao.getDataTagihanPerAkun(tahun, kodeTemplate);
            if (listData.isEmpty()){
                return ResponseEntity.ok().body(
                        BaseResponseDto.builder()
                                .responseCode("200")
                                .responseMessage("Data Kosong")
                                .build());
            }else{
                return ResponseEntity.ok().body(
                        BaseResponseDto.builder()
                                .responseCode("200")
                                .responseMessage("Data Ditemukan")
                                .data(listData).build());
            }
        }catch (Exception e){
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                    BaseResponseDto.builder()
                            .responseCode("500")
                            .responseMessage(e.getLocalizedMessage())
                            .build());
        }
    }

}
