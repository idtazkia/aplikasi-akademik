package id.ac.tazkia.smilemahasiswa.service;

import id.ac.tazkia.smilemahasiswa.dao.StatusImportElearningDao;
import id.ac.tazkia.smilemahasiswa.dto.elearning.MdlAttendanceLogDosenDto;
import id.ac.tazkia.smilemahasiswa.entity.StatusImportElearning;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.reactive.function.client.WebClient;

import java.time.LocalDate;
import java.util.List;

@Service
public class ImportPresensiService {

    @Autowired
    private ElearningWebClientService elearningWebClientService;

    @Autowired
    private StatusImportElearningDao statusImportElearningDao;

    WebClient webClient1 = WebClient.builder()
            .baseUrl("https://elearning.tazkia.ac.id")
            .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
            .build();

    public List<MdlAttendanceLogDosenDto> getAttendanceDosen(@RequestParam LocalDate tanggalImport) {
        return webClient1.get()
                .uri("/api/sessiondosen?tanggalImport=" + tanggalImport)
                .retrieve().bodyToFlux(id.ac.tazkia.smilemahasiswa.dto.elearning.MdlAttendanceLogDosenDto.class)
                .collectList()
                .block();
    }

    //    @Scheduled(fixedDelay = 60*60)
    //    @Scheduled(cron = "0 51 14 * * ? ", zone = "Asia/Jakarta")
    @Scheduled(cron = "0 0 22 * * ? ", zone = "Asia/Jakarta")
    public void importPresensi(){

        LocalDate tanggalProses = terakhirImport().plusDays(1);
        LocalDate tanggalProses2 = terakhirImport();
//         dibatasi sampai yang ditentukan
//        if(tanggalProses.compareTo(LocalDate.of(2021,5,8)) <= 0) {
            System.out.println("HASIL TANGGAL = " + terakhirImport().plusDays(1));
            importPresensi(tanggalProses);
//        }

//            if (tanggalProses.compareTo(LocalDate.of(2021, 1, 11)) > 0) {
//                System.out.println("HASIL TANGGAL = " + terakhirImport().plusDays(1));
//                importPresensi(tanggalProses);
//            }


    }

    public void importPresensi(LocalDate tanggalImport){

        elearningWebClientService.syncPresensiMoodle(tanggalImport);

          StatusImportElearning statusImportElearning = new StatusImportElearning();
          statusImportElearning.setTanggalImport(tanggalImport);
          statusImportElearning.setStatus("SUKSES");
          statusImportElearningDao.save(statusImportElearning);


//        System.out.println("Import presensi untuk tanggal "+tanggalImport);
//        System.out.println("insert into status_import (tanggal, status) values (tanggalImport, sukses)");
    }


    private LocalDate terakhirImport(){
        // tabel status_import harus diisi satu record, yaitu sehari sebelum pemakaian moodle pertama
//        System.out.println("select max tanggal_import from status_import where status = sukses order by tanggal import desc");
        return statusImportElearningDao.terakhirData();

    }

}
