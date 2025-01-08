package id.ac.tazkia.smilemahasiswa.service;

import id.ac.tazkia.smilemahasiswa.dao.*;
import id.ac.tazkia.smilemahasiswa.entity.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;


@Service
@Slf4j
public class KhsService {

    @Autowired
    private RequestKonversiDao requestKonversiDao;

    @Autowired
    private CurrentUserService currentUserService;

    @Autowired
    private KaryawanDao karyawanDao;

    @Autowired
    private MahasiswaDao mahasiswaDao;

    @Autowired
    private KrsDetailDao krsDetailDao;

    @Autowired
    private GradeDao gradeDao;

    public RequestKonversi getRequestKonversiById(String id){
        return requestKonversiDao.findById(id).get();
    }

    public RequestKonversi validasiUploadFileKonversi(KrsDetail krsDetail){
        return requestKonversiDao.findByKrsDetailAndStatus(krsDetail,StatusRecord.AKTIF);
    }

    public List<RequestKonversi> getRequestList(KrsDetail krsDetail, Jadwal jadwal){
        List<RequestKonversi> getRequestList = requestKonversiDao.findByKrsDetailAndKrsDetailJadwalAndStatus(krsDetail, jadwal,StatusRecord.AKTIF);
        return getRequestList;

    }


    public void uploadFileKonversi(RequestKonversi request, KrsDetail krsDetail, MultipartFile file, String lokasiUpload) throws IOException{


        try {
            if (file == null || file.isEmpty()) {
                log.info("Document kosong, tidak di proses!");
                return;
            }

                RequestKonversi requestKonversi;

                if (requestKonversiDao.findByKrsDetailAndStatus(krsDetail,StatusRecord.AKTIF) == null){
                    requestKonversi = new RequestKonversi();
                }else {
                    requestKonversi = requestKonversiDao.findByKrsDetailAndStatus(krsDetail,StatusRecord.AKTIF);
                }

                requestKonversi.setKrsDetail(krsDetail);
                requestKonversi.setGradeLama(krsDetail.getGrade());


                String namaFile = file.getName();
                String jenisFile = file.getContentType();
                String namaAsli = file.getOriginalFilename();
                Long ukuran = file.getSize();

                String extension = "";

                int i = namaAsli.lastIndexOf('.');
                int p = Math.max(namaAsli.lastIndexOf('/'), namaAsli.lastIndexOf('\\'));

                if (i > p) {
                    extension = namaAsli.substring(i + 1);
                }

                String idFile = UUID.randomUUID().toString();

                requestKonversi.setFilename(idFile + '.' + extension);
                requestKonversi.setNamaFile(file.getOriginalFilename());
                requestKonversi.setDeskripsi(request.getDeskripsi());
                requestKonversi.setJenisNilai(request.getJenisNilai());


                File tujuan = new File(lokasiUpload + File.separator + requestKonversi.getFilename());
                file.transferTo(tujuan);
                requestKonversiDao.save(requestKonversi);


        }catch (Exception err){
            log.error(err.getMessage(), err);
        }


    }

    public void deleteRequestKonversi(String requestKonversi, Authentication authentication, KrsDetail krsDetail){

        User user = currentUserService.currentUser(authentication);
        Mahasiswa mahasiswa = mahasiswaDao.findByUser(user);

        RequestKonversi requestKonversi1 = requestKonversiDao.findById(requestKonversi).get();
        requestKonversi1.setStatus(StatusRecord.HAPUS);
        requestKonversiDao.save(requestKonversi1);
    }

    public void approveRequestKonversi(Authentication authentication, String requestKonversi, KrsDetail krsDetail) {

        User user = currentUserService.currentUser(authentication);
        Karyawan karyawan = karyawanDao.findByIdUser(user);

        RequestKonversi requestKonversi2 = requestKonversiDao.findById(requestKonversi).get();
        requestKonversi2.setApprovedDate(LocalDateTime.now());
        requestKonversi2.setApprovedBy(karyawan.getEmail());
        requestKonversi2.setApproval(StatusRecord.APPROVED);
        requestKonversiDao.save(requestKonversi2);


    }

    public void rejectRequestKonversi(Authentication authentication, String requestKonversi, KrsDetail krsDetail) {

        User user = currentUserService.currentUser(authentication);
        Karyawan karyawan = karyawanDao.findByIdUser(user);

        RequestKonversi requestKonversi3 = requestKonversiDao.findById(requestKonversi).get();
        requestKonversi3.setRejectedDate(LocalDateTime.now());
        requestKonversi3.setRejectedBy(karyawan.getEmail());
        requestKonversi3.setApproval(StatusRecord.REJECTED);
        requestKonversiDao.save(requestKonversi3);


    }

    public void updateNilai(Authentication authentication, KrsDetail krsDetail, String grade) {

        User user = currentUserService.currentUser(authentication);
        Karyawan karyawan = karyawanDao.findByIdUser(user);

        RequestKonversi requestKonversi = requestKonversiDao.findByKrsDetailAndStatusAndApproval(krsDetail,StatusRecord.AKTIF, StatusRecord.APPROVED);
        Optional<Grade> optionalGrade = gradeDao.findById(grade);
        if (optionalGrade.isPresent()){
            requestKonversi.setGradeBaru(optionalGrade.get().getNama());
            requestKonversi.setFinalizationDate(LocalDateTime.now());
            requestKonversi.setFinalizationBy(karyawan.getEmail());
            requestKonversi.setFinalization(StatusRecord.Y);
            requestKonversiDao.save(requestKonversi);

            krsDetail.setGrade(optionalGrade.get().getNama());
            krsDetail.setBobot(optionalGrade.get().getBobot());
            krsDetail.setNilaiAkhir(optionalGrade.get().getBawah());
            krsDetail.setRequestKonversi(StatusRecord.KONVERSI);
            krsDetailDao.save(krsDetail);
        }


    }

}
