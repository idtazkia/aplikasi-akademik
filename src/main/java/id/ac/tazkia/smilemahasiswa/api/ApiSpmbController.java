package id.ac.tazkia.smilemahasiswa.api;

import id.ac.tazkia.smilemahasiswa.dao.KurikulumDao;
import id.ac.tazkia.smilemahasiswa.dao.MatakuliahKurikulumDao;
import id.ac.tazkia.smilemahasiswa.dao.ProdiDao;
import id.ac.tazkia.smilemahasiswa.dto.BaseResponse;
import id.ac.tazkia.smilemahasiswa.dto.api.BaseResponseDto;
import id.ac.tazkia.smilemahasiswa.dto.konsentrasi.ListKonsentrasiDto;
import id.ac.tazkia.smilemahasiswa.entity.Kurikulum;
import id.ac.tazkia.smilemahasiswa.entity.MatakuliahKurikulum;
import id.ac.tazkia.smilemahasiswa.entity.Prodi;
import id.ac.tazkia.smilemahasiswa.entity.StatusRecord;
import id.ac.tazkia.smilemahasiswa.service.KonsentrasiService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.Arrays;
import java.util.List;

@Controller
@Slf4j
public class ApiSpmbController {

    @Autowired private ProdiDao prodiDao;
    @Autowired private KurikulumDao kurikulumDao;
    @Autowired private MatakuliahKurikulumDao matakuliahKurikulumDao;

    @Autowired private KonsentrasiService konsentrasiService;

    @GetMapping("/api/get-prodi")
    @ResponseBody
    public ResponseEntity<BaseResponseDto> getProdi() {
        try{
            List<Prodi> prodi = prodiDao.findByStatus(StatusRecord.AKTIF);
            if(prodi.isEmpty()) {
                return ResponseEntity.ok().body(
                        BaseResponseDto.builder()
                                .responseCode("200")
                                .responseMessage("Data kosong")
                                .build()
                );
            }else{
                return ResponseEntity.ok().body(
                        BaseResponseDto.builder()
                                .responseCode("200")
                                .data(prodi)
                                .responseMessage("Prodi Ditemukan")
                                .build());
            }
        }catch (Exception e){
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                    BaseResponseDto.builder()
                            .responseCode("ERR500")
                            .responseMessage(e.getLocalizedMessage())
                            .build());
        }
    }

    @GetMapping("/api/get-kurikulum-prodi/{prodiId}")
    @ResponseBody
    public ResponseEntity<BaseResponseDto> getKurikulumProdi(@PathVariable String prodiId) {
        try {
            Prodi prodi = prodiDao.findById(prodiId).orElse(null);
            List<Kurikulum> listKurikulum = kurikulumDao.findByProdiAndStatusNotIn(prodi, Arrays.asList(StatusRecord.HAPUS));
            if(listKurikulum.isEmpty()) {
                return ResponseEntity.ok().body(
                        BaseResponseDto.builder()
                                .responseCode("200")
                                .responseMessage("Data tidak ditemukan")
                                .build()
                );
            }else{
                return ResponseEntity.ok().body(
                        BaseResponseDto.builder()
                                .responseCode("200")
                                .responseMessage("Kurikulum ditemukan")
                                .data(listKurikulum)
                                .build()
                );
            }
        }catch (Exception e){
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                    BaseResponseDto.builder()
                            .responseCode("ERR500")
                            .responseMessage(e.getLocalizedMessage())
                            .build()
            );
        }
    }

    @GetMapping("/api/get-matakuliah/{prodiId}/{kurikulumId}")
    @ResponseBody
    public ResponseEntity<BaseResponseDto> getMatakuliahKonversi(@PathVariable String prodiId, @PathVariable String kurikulumId) {
        try{
            Prodi prodi = prodiDao.findById(prodiId).orElse(null);
            Kurikulum kurikulum = kurikulumDao.findById(kurikulumId).orElse(null);

            List<MatakuliahKurikulum> matakuliahKurikulumList = matakuliahKurikulumDao
                    .findByStatusNotInAndKurikulumAndKurikulumProdiAndSemesterIn(Arrays.asList(StatusRecord.HAPUS),
                            kurikulum, prodi, Arrays.asList(1, 2, 3, 4, 5, 6, 7, 8, 9));

            if(matakuliahKurikulumList.isEmpty()) {
                return ResponseEntity.ok().body(
                        BaseResponseDto.builder()
                                .responseCode("200")
                                .responseMessage("Data tidak ditemukan")
                                .build()
                );
            }else{
                return ResponseEntity.ok().body(
                        BaseResponseDto.builder()
                                .responseCode("200")
                                .responseMessage("Kurikulum ditemukan")
                                .data(matakuliahKurikulumList)
                                .build()
                );
            }
        }catch (Exception e){
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                    BaseResponseDto.builder()
                            .responseCode("ERR500")
                            .responseMessage(e.getLocalizedMessage())
                            .build()
            );
        }
    }

    @GetMapping("/api/getlistKonsentrasiS1")
    @ResponseBody
    public ResponseEntity<BaseResponseDto> getListKonsentrasiS1() {
        try {
            List<ListKonsentrasiDto> konsentrasiDatas = konsentrasiService.getListKonsentrasiS1();
            if (konsentrasiDatas.isEmpty()) {
                return ResponseEntity.ok().body(
                        BaseResponseDto.builder()
                                .responseCode("200")
                                .responseMessage("Data Kosong")
                                .build()
                );
            }else{
                return ResponseEntity.ok().body(
                        BaseResponseDto.builder()
                                .responseCode("200")
                                .data(konsentrasiDatas)
                                .responseMessage("Konsentrasi Ditemukan")
                                .build()
                );
            }
        }catch (Exception e){
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                    BaseResponseDto.builder()
                            .responseCode("ERR500")
                            .responseMessage(e.getLocalizedMessage())
                            .build()
            );
        }
    }

    @GetMapping("/api/getlistKonsentrasiS2")
    @ResponseBody
    public ResponseEntity<BaseResponseDto> getListKonsentrasiS2() {
        try {
            List<ListKonsentrasiDto> konsentrasiDatas = konsentrasiService.getListKonsentrasiS2();
            if (konsentrasiDatas.isEmpty()) {
                return ResponseEntity.ok().body(
                        BaseResponseDto.builder()
                                .responseCode("200")
                                .responseMessage("Data Kosong")
                                .build()
                );
            }else{
                return ResponseEntity.ok().body(
                        BaseResponseDto.builder()
                                .responseCode("200")
                                .data(konsentrasiDatas)
                                .responseMessage("Konsentrasi Ditemukan")
                                .build()
                );
            }
        }catch (Exception e){
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                    BaseResponseDto.builder()
                            .responseCode("ERR500")
                            .responseMessage(e.getLocalizedMessage())
                            .build()
            );
        }
    }

}
