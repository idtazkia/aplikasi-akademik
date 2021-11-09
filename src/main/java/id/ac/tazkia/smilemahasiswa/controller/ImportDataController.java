package id.ac.tazkia.smilemahasiswa.controller;

import id.ac.tazkia.smilemahasiswa.dao.*;
import id.ac.tazkia.smilemahasiswa.dto.ImportMahasiswaDto;
import id.ac.tazkia.smilemahasiswa.dto.request.RequestCicilanDto;
import id.ac.tazkia.smilemahasiswa.dto.response.BaseResponse;
import id.ac.tazkia.smilemahasiswa.entity.*;
import id.ac.tazkia.smilemahasiswa.service.MahasiswaService;
import id.ac.tazkia.smilemahasiswa.service.TagihanService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import javax.validation.ConstraintViolationException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@RequestMapping("/request/mahasiswa/import/")
@RestController
@Slf4j
public class ImportDataController  {

    @Autowired
    private MahasiswaService mahasiswaService;

    @Autowired
    private MahasiswaDao mahasiswaDao;

    @Autowired
    private UserDao userDao;

    @Autowired
    private TahunAkademikDao tahunAkademikDao;

    @Autowired
    private JenisTagihanDao jenisTagihanDao;

    @Autowired
    private NilaiJenisTagihanDao nilaiJenisTagihanDao;

    @Autowired
    private TagihanService tagihanService;

    @Autowired
    private RequestCicilanDao requestCicilanDao;

    @Autowired
    private TagihanDao tagihanDao;

    @PostMapping(value = "/proses", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.OK)
    @ResponseBody
    public BaseResponse importMahasiswa(@RequestBody ImportMahasiswaDto request) {

        User user = userDao.findByUsernameAndId(request.getUser(), request.getId());
        if (user != null) {
                Mahasiswa mahasiswa = mahasiswaDao.findByNimAndStatus(request.getNim(),StatusRecord.AKTIF);
                if (mahasiswa != null) {

                    return new  BaseResponse(HttpStatus.ALREADY_REPORTED.getReasonPhrase(),
                            String.valueOf(HttpStatus.ALREADY_REPORTED.value()));
                } else {
                    try {
                        mahasiswaService.importMahasiswa(request);
                        Mahasiswa m = mahasiswaDao.findByNim(request.getNim());
                        if (!request.getCicilan().isEmpty() || request.getCicilan() != null) {
                            createTagihan(request.getCicilan(), m);
                        }
                        log.info("Detail Import {}" , m);
                        return new BaseResponse(HttpStatus.CREATED.getReasonPhrase(),
                                String.valueOf(HttpStatus.CREATED.value()));
                    }catch (ConstraintViolationException err){
                        err.printStackTrace();
                        log.info("ConstraintViolationException : {} ", err.getMessage());
                        throw err;
                    }

                }
        }else {
            return new BaseResponse(HttpStatus.FORBIDDEN.getReasonPhrase(),
                    String.valueOf(HttpStatus.FORBIDDEN.value()));

        }
    }

    private void createTagihan(List<RequestCicilanDto> cicilans, Mahasiswa mahasiswa){
        JenisTagihan jenisTagihan = jenisTagihanDao.findByKodeAndStatus("02",StatusRecord.AKTIF);
        NilaiJenisTagihan cekNilaiJenis = nilaiJenisTagihanDao.
                findByProdiAndAngkatanAndTahunAkademikAndProgramAndStatusAndJenisTagihan(mahasiswa.getIdProdi(),mahasiswa.getAngkatan(),tahunAkademikDao.findByStatus(StatusRecord.AKTIF),mahasiswa.getIdProgram(),StatusRecord.AKTIF,jenisTagihan);
        if (cekNilaiJenis == null){
            NilaiJenisTagihan nilaiJenisTagihan = new NilaiJenisTagihan();
            nilaiJenisTagihan.setJenisTagihan(jenisTagihan);
            nilaiJenisTagihan.setNilai(new BigDecimal(8000000));
            nilaiJenisTagihan.setTahunAkademik(tahunAkademikDao.findByStatus(StatusRecord.AKTIF));
            nilaiJenisTagihan.setProdi(mahasiswa.getIdProdi());
            nilaiJenisTagihan.setProgram(mahasiswa.getIdProgram());
            nilaiJenisTagihan.setAngkatan(mahasiswa.getAngkatan());
            nilaiJenisTagihan.setStatus(StatusRecord.AKTIF);
            nilaiJenisTagihanDao.save(nilaiJenisTagihan);

            BigDecimal totalNominal = cicilans.stream().map(RequestCicilanDto::getNominal)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            Tagihan tagihan = new Tagihan();
            tagihan.setMahasiswa(mahasiswa);
            tagihan.setNilaiJenisTagihan(nilaiJenisTagihan);
            tagihan.setKeterangan("Sisa Tagihan Daftar Ulang SPMB");
            tagihan.setNilaiTagihan(totalNominal);
            tagihan.setAkumulasiPembayaran(BigDecimal.ZERO);
            tagihan.setTanggalPembuatan(LocalDate.now());
            tagihan.setTanggalJatuhTempo(LocalDate.now().plusYears(1));
            tagihan.setTanggalPenangguhan(LocalDate.now().plusYears(1));
            tagihan.setTahunAkademik(tahunAkademikDao.findByStatus(StatusRecord.AKTIF));
            tagihan.setStatusTagihan(StatusTagihan.DICICIL);
            tagihan.setStatus(StatusRecord.AKTIF);
            tagihanDao.save(tagihan);
            handleCicilan(cicilans,tagihan);
        }else {
            BigDecimal totalNominal = cicilans.stream().map(RequestCicilanDto::getNominal)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            Tagihan tagihan = new Tagihan();
            tagihan.setMahasiswa(mahasiswa);
            tagihan.setNilaiJenisTagihan(cekNilaiJenis);
            tagihan.setKeterangan("Sisa Tagihan Daftar Ulang SPMB");
            tagihan.setNilaiTagihan(totalNominal);
            tagihan.setAkumulasiPembayaran(BigDecimal.ZERO);
            tagihan.setTanggalPembuatan(LocalDate.now());
            tagihan.setTanggalJatuhTempo(LocalDate.now().plusYears(1));
            tagihan.setTanggalPenangguhan(LocalDate.now().plusYears(1));
            tagihan.setTahunAkademik(tahunAkademikDao.findByStatus(StatusRecord.AKTIF));
            tagihan.setStatusTagihan(StatusTagihan.DICICIL);
            tagihan.setStatus(StatusRecord.AKTIF);
            tagihanDao.save(tagihan);
            handleCicilan(cicilans,tagihan);
        }

    }

    private void handleCicilan(List<RequestCicilanDto> cicilans, Tagihan tagihan){
        for (RequestCicilanDto detailCicilan : cicilans){
            if (detailCicilan.getStatus() == Boolean.TRUE) {
                RequestCicilan cicilan = new RequestCicilan();
                cicilan.setTagihan(tagihan);
                cicilan.setTanggalPengajuan(LocalDate.now());
                cicilan.setNilaiCicilan(detailCicilan.getNominal());
                cicilan.setTanggalPengajuan(LocalDate.now());
                cicilan.setTanggalJatuhTempo(detailCicilan.getTanggalKirim().plusMonths(1));
                cicilan.setStatusApprove(StatusApprove.APPROVED);
                cicilan.setStatusCicilan(StatusCicilan.SEDANG_DITAGIHKAN);
                requestCicilanDao.save(cicilan);
            }else {
                RequestCicilan cicilan = new RequestCicilan();
                cicilan.setTagihan(tagihan);
                cicilan.setTanggalPengajuan(LocalDate.now());
                cicilan.setNilaiCicilan(detailCicilan.getNominal());
                cicilan.setTanggalPengajuan(LocalDate.now());
                cicilan.setTanggalJatuhTempo(detailCicilan.getTanggalKirim().plusMonths(1));
                cicilan.setStatusApprove(StatusApprove.APPROVED);
                requestCicilanDao.save(cicilan);
            }
        }

        RequestCicilanDto requestCicilan = cicilans.stream()
                .filter(cicilan -> Boolean.TRUE.equals(cicilan.getStatus()))
                .findAny()
                .orElse(null);

        if (requestCicilan == null) {
            RequestCicilan kirimCicilan = requestCicilanDao.cariCicilanSelanjutnya(tagihan);
            kirimCicilan.setStatusCicilan(StatusCicilan.SEDANG_DITAGIHKAN);
            requestCicilanDao.save(kirimCicilan);
            tagihanService.mengirimCicilanSelanjutnya(kirimCicilan);
        }else{
            tagihan.setNomor(requestCicilan.getNomorTagihan());
            tagihanDao.save(tagihan);
        }


    }


}
