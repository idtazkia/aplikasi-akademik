package id.ac.tazkia.smilemahasiswa.service;

import com.lowagie.text.*;
import com.lowagie.text.pdf.*;
import com.oracle.webservices.api.message.BaseDistributedPropertySet;
import com.sun.xml.xsom.impl.UName;
import id.ac.tazkia.smilemahasiswa.controller.DashboardController;
import id.ac.tazkia.smilemahasiswa.dao.*;
import id.ac.tazkia.smilemahasiswa.dto.graduation.SeminarDto;
import id.ac.tazkia.smilemahasiswa.dto.graduation.SemproDto;
import id.ac.tazkia.smilemahasiswa.dto.graduation.SidangDto;
import id.ac.tazkia.smilemahasiswa.entity.*;
import org.openxmlformats.schemas.presentationml.x2006.main.CTEmbeddedFontDataId;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestParam;

import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.chrono.HijrahDate;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import java.util.concurrent.Phaser;

@Service
public class SidangService {

    @Value("${logo.surat}")
    private String logo;

    @Value("${logo.ttd}")
    private String tandaTangan;

    @Autowired
    private SidangDao sidangDao;

    @Autowired
    private SeminarDao seminarDao;

    @Autowired
    private JenjangDao jenjangDao;

    @Autowired
    private EnableFitureDao enableFitureDao;

    @Autowired
    private KodeSidangDao kodeSidangDao;

    @Autowired
    private HariDao hariDao;
    public Seminar updatePublish(Seminar seminar,SeminarDto seminarDto){
        BeanUtils.copyProperties(seminarDto,seminar);
        if (seminarDto.getJenis() == StatusRecord.SKRIPSI){
            BigDecimal nilaiA = seminar.getKa().add(seminar.getUa()).add(seminar.getPa()).divide(BigDecimal.valueOf(3), 2, RoundingMode.HALF_UP).multiply(BigDecimal.valueOf(0.25));
            BigDecimal nilaiB = seminar.getKb().add(seminar.getUb()).add(seminar.getPb()).divide(BigDecimal.valueOf(3), 2, RoundingMode.HALF_UP).multiply(BigDecimal.valueOf(0.15));
            BigDecimal nilaiC = seminar.getKc().add(seminar.getUc()).add(seminar.getPc()).divide(BigDecimal.valueOf(3), 2, RoundingMode.HALF_UP).multiply(BigDecimal.valueOf(0.2));
            BigDecimal nilaiD = seminar.getKd().add(seminar.getUd()).add(seminar.getPd()).divide(BigDecimal.valueOf(3), 2, RoundingMode.HALF_UP).multiply(BigDecimal.valueOf(0.1));
            BigDecimal nilaiE = seminar.getKe().add(seminar.getUe()).add(seminar.getPe()).divide(BigDecimal.valueOf(3), 2, RoundingMode.HALF_UP).multiply(BigDecimal.valueOf(0.15));
            BigDecimal nilaiF = seminar.getKf().add(seminar.getUf()).add(seminar.getPf()).divide(BigDecimal.valueOf(3), 2, RoundingMode.HALF_UP).multiply(BigDecimal.valueOf(0.15));
            seminar.setNilaiA(nilaiA);
            seminar.setNilaiB(nilaiB);
            seminar.setNilaiC(nilaiC);
            seminar.setNilaiD(nilaiD);
            seminar.setNilaiE(nilaiE);
            seminar.setNilaiF(nilaiF);
            seminar.setNilai(nilaiA.add(nilaiB).add(nilaiC).add(nilaiD).add(nilaiE).add(nilaiF));
            Object nilaiKosong = seminarDao.validasiPublishNilaiSkripsi(seminar,BigDecimal.ZERO);
            if (nilaiKosong == null) {
                if (seminar.getNilai().compareTo(new BigDecimal(70)) < 0) {
                    seminar.setStatusSempro(StatusApprove.FAILED);
                    seminar.setStatus(StatusApprove.FAILED);
                    EnableFiture enableFiture = enableFitureDao.findByMahasiswaAndFiturAndEnable(seminar.getNote().getMahasiswa(), StatusRecord.SEMPRO, Boolean.TRUE);
                    if (enableFiture != null) {
                        enableFiture.setEnable(Boolean.FALSE);
                        enableFitureDao.save(enableFiture);
                    }
                }else {
                    seminar.setStatusSempro(StatusApprove.APPROVED);
                }
            }

        }

        if (seminarDto.getJenis() == StatusRecord.STUDI_KELAYAKAN){
            BigDecimal nilaiA = seminar.getKa().add(seminar.getUa()).add(seminar.getPa()).divide(BigDecimal.valueOf(3), 2, RoundingMode.HALF_UP).multiply(BigDecimal.valueOf(0.3));
            BigDecimal nilaiB = seminar.getKb().add(seminar.getUb()).add(seminar.getPb()).divide(BigDecimal.valueOf(3), 2, RoundingMode.HALF_UP).multiply(BigDecimal.valueOf(0.25));
            BigDecimal nilaiC = seminar.getKc().add(seminar.getUc()).add(seminar.getPc()).divide(BigDecimal.valueOf(3), 2, RoundingMode.HALF_UP).multiply(BigDecimal.valueOf(0.2));
            BigDecimal nilaiD = seminar.getKd().add(seminar.getUd()).add(seminar.getPd()).divide(BigDecimal.valueOf(3), 2, RoundingMode.HALF_UP).multiply(BigDecimal.valueOf(0.15));
            BigDecimal nilaiE = seminar.getKe().add(seminar.getUe()).add(seminar.getPe()).divide(BigDecimal.valueOf(3), 2, RoundingMode.HALF_UP).multiply(BigDecimal.valueOf(0.1));
            seminar.setNilaiA(nilaiA);
            seminar.setNilaiB(nilaiB);
            seminar.setNilaiC(nilaiC);
            seminar.setNilaiD(nilaiD);
            seminar.setNilaiE(nilaiE);
            seminar.setNilai(nilaiA.add(nilaiB).add(nilaiC).add(nilaiD).add(nilaiE));
            Object nilaiKosong = seminarDao.validasiPublishNilaiStudy(seminar,BigDecimal.ZERO);
            if (nilaiKosong == null) {
                if (seminar.getNilai().compareTo(new BigDecimal(70)) < 0) {
                    seminar.setStatusSempro(StatusApprove.FAILED);
                    seminar.setStatus(StatusApprove.FAILED);
                    EnableFiture enableFiture = enableFitureDao.findByMahasiswaAndFiturAndEnable(seminar.getNote().getMahasiswa(), StatusRecord.SEMPRO, Boolean.TRUE);
                    if (enableFiture != null) {
                        enableFiture.setEnable(Boolean.FALSE);
                        enableFitureDao.save(enableFiture);
                    }
                }else {
                    seminar.setStatusSempro(StatusApprove.APPROVED);
                }
            }
        }

        if (seminarDto.getJenis() == StatusRecord.TESIS){
            seminar.setPa2(seminarDto.getPa2());
            seminar.setPb2(seminarDto.getPb2());
            seminar.setPc2(seminarDto.getPc2());
            seminar.setPd2(seminarDto.getPd2());
            seminar.setPe2(seminarDto.getPe2());
            BigDecimal nilaiA = seminar.getKa().add(seminar.getUa()).add(seminar.getPa()).add(seminar.getPa2()).divide(BigDecimal.valueOf(4), 2, RoundingMode.HALF_UP).multiply(BigDecimal.valueOf(0.2));
            BigDecimal nilaiB = seminar.getKb().add(seminar.getUb()).add(seminar.getPb()).add(seminar.getPb2()).divide(BigDecimal.valueOf(4), 2, RoundingMode.HALF_UP).multiply(BigDecimal.valueOf(0.3));
            BigDecimal nilaiC = seminar.getKc().add(seminar.getUc()).add(seminar.getPc()).add(seminar.getPc2()).divide(BigDecimal.valueOf(4), 2, RoundingMode.HALF_UP).multiply(BigDecimal.valueOf(0.1));
            BigDecimal nilaiD = seminar.getKd().add(seminar.getUd()).add(seminar.getPd()).add(seminar.getPd2()).divide(BigDecimal.valueOf(4), 2, RoundingMode.HALF_UP).multiply(BigDecimal.valueOf(0.1));
            BigDecimal nilaiE = seminar.getKe().add(seminar.getUe()).add(seminar.getPe()).add(seminar.getPe2()).divide(BigDecimal.valueOf(4), 2, RoundingMode.HALF_UP).multiply(BigDecimal.valueOf(0.3));
            seminar.setNilaiA(nilaiA);
            seminar.setNilaiB(nilaiB);
            seminar.setNilaiC(nilaiC);
            seminar.setNilaiD(nilaiD);
            seminar.setNilaiE(nilaiE);
            seminar.setNilai(nilaiA.add(nilaiB).add(nilaiC).add(nilaiD).add(nilaiE));
            Object nilaiKosong = seminarDao.validasiPublishNilaiTesis(seminar,BigDecimal.ZERO);
            if (nilaiKosong == null) {
                if (seminar.getNilai().compareTo(new BigDecimal(70)) < 0) {
                    seminar.setStatusSempro(StatusApprove.FAILED);
                    seminar.setStatus(StatusApprove.FAILED);
                    EnableFiture enableFiture = enableFitureDao.findByMahasiswaAndFiturAndEnable(seminar.getNote().getMahasiswa(), StatusRecord.SEMPRO, Boolean.TRUE);
                    if (enableFiture != null) {
                        enableFiture.setEnable(Boolean.FALSE);
                        enableFitureDao.save(enableFiture);
                    }
                }else {
                    seminar.setStatusSempro(StatusApprove.APPROVED);
                }
            }

        }


        seminarDao.save(seminar);
        return seminar;
    }

    public Sidang updateSidangPasca(Sidang sidang,SeminarDto seminarDto){
        BeanUtils.copyProperties(seminarDto,sidang);


       sidang.setPa2(seminarDto.getPa2());
       sidang.setPb2(seminarDto.getPb2());
       sidang.setPc2(seminarDto.getPc2());
       sidang.setPd2(seminarDto.getPd2());
       BigDecimal nilaiA = sidang.getKa().add(sidang.getUa()).add(sidang.getPa()).add(sidang.getPa2()).divide(BigDecimal.valueOf(4), 2, RoundingMode.HALF_UP).multiply(BigDecimal.valueOf(0.2));
       BigDecimal nilaiB = sidang.getKb().add(sidang.getUb()).add(sidang.getPb()).add(sidang.getPb2()).divide(BigDecimal.valueOf(4), 2, RoundingMode.HALF_UP).multiply(BigDecimal.valueOf(0.3));
       BigDecimal nilaiC = sidang.getKc().add(sidang.getUc()).add(sidang.getPc()).add(sidang.getPc2()).divide(BigDecimal.valueOf(4), 2, RoundingMode.HALF_UP).multiply(BigDecimal.valueOf(0.3));
       BigDecimal nilaiD = sidang.getKd().add(sidang.getUd()).add(sidang.getPd()).add(sidang.getPd2()).divide(BigDecimal.valueOf(4), 2, RoundingMode.HALF_UP).multiply(BigDecimal.valueOf(0.2));
       sidang.setNilaiA(nilaiA);
       sidang.setNilaiB(nilaiB);
       sidang.setNilaiC(nilaiC);
       sidang.setNilaiD(nilaiD);
       sidang.setNilai(nilaiA.add(nilaiB).add(nilaiC).add(nilaiD));
        sidangDao.save(sidang);
        return sidang;
    }

    public Sidang updateSidang(Sidang sidang,SeminarDto seminarDto){
        BeanUtils.copyProperties(seminarDto,sidang);

        BigDecimal nilaiA = sidang.getKa().add(sidang.getUa()).add(sidang.getPa()).divide(BigDecimal.valueOf(3), 2, RoundingMode.HALF_UP).multiply(BigDecimal.valueOf(0.4));
        BigDecimal nilaiB = sidang.getKb().add(sidang.getUb()).add(sidang.getPb()).divide(BigDecimal.valueOf(3), 2, RoundingMode.HALF_UP).multiply(BigDecimal.valueOf(0.3));
        BigDecimal nilaiC = sidang.getKc().add(sidang.getUc()).add(sidang.getPc()).divide(BigDecimal.valueOf(3), 2, RoundingMode.HALF_UP).multiply(BigDecimal.valueOf(0.15));
        BigDecimal nilaiD = sidang.getKd().add(sidang.getUd()).add(sidang.getPd()).divide(BigDecimal.valueOf(3), 2, RoundingMode.HALF_UP).multiply(BigDecimal.valueOf(0.15));
        sidang.setNilaiA(nilaiA);
        sidang.setNilaiB(nilaiB);
        sidang.setNilaiC(nilaiC);
        sidang.setNilaiD(nilaiD);
        sidang.setNilai(nilaiA.add(nilaiB).add(nilaiC).add(nilaiD));
        sidangDao.save(sidang);
        return sidang;
    }

    public SidangDto getKetua(Sidang sidang){
        SidangDto sidangDto = new SidangDto();
        sidangDto.setId(sidang.getId());
        sidangDto.setKomentar(sidang.getKomentarKetua());
        sidangDto.setBeritaAcara(sidang.getBeritaAcara());
        sidangDto.setNilaiA(sidang.getKa());
        sidangDto.setNilaiB(sidang.getKb());
        sidangDto.setNilaiC(sidang.getKc());
        sidangDto.setNilaiD(sidang.getKd());

        return sidangDto;
    }

    public SemproDto getKetuaSempro(Seminar seminar){
        SemproDto semproDto = new SemproDto();
        semproDto.setId(seminar.getId());
        semproDto.setKomentar(seminar.getKomentarKetua());
        semproDto.setBeritaAcara(seminar.getBeritaAcara());
        semproDto.setNilaiA(seminar.getKa());
        semproDto.setNilaiB(seminar.getKb());
        semproDto.setNilaiC(seminar.getKc());
        semproDto.setNilaiD(seminar.getKd());
        semproDto.setNilaiE(seminar.getKe());

        return semproDto;
    }

    public SemproDto getPengujiSempro(Seminar seminar){
        SemproDto semproDto = new SemproDto();
        semproDto.setId(seminar.getId());
        semproDto.setKomentar(seminar.getKomentarKetua());
        semproDto.setNilaiA(seminar.getUa());
        semproDto.setNilaiB(seminar.getUb());
        semproDto.setNilaiC(seminar.getUc());
        semproDto.setNilaiD(seminar.getUd());
        semproDto.setNilaiE(seminar.getUe());

        return semproDto;
    }

    public SemproDto getPembimbingSempro1(Seminar seminar){
        SemproDto semproDto = new SemproDto();
        semproDto.setId(seminar.getId());
        semproDto.setKomentar(seminar.getKomentarKetua());
        semproDto.setNilaiA(seminar.getPa());
        semproDto.setNilaiB(seminar.getPb());
        semproDto.setNilaiC(seminar.getPc());
        semproDto.setNilaiD(seminar.getPd());
        semproDto.setNilaiE(seminar.getPe());

        return semproDto;
    }

    public SemproDto getPembimbingSempro2(Seminar seminar){
        SemproDto semproDto = new SemproDto();
        semproDto.setId(seminar.getId());
        semproDto.setKomentar(seminar.getKomentarKetua());
        semproDto.setBeritaAcara(seminar.getBeritaAcara());
        semproDto.setNilaiA(seminar.getPa2());
        semproDto.setNilaiB(seminar.getPb2());
        semproDto.setNilaiC(seminar.getPc2());
        semproDto.setNilaiD(seminar.getPd2());
        semproDto.setNilaiE(seminar.getPe2());

        return semproDto;
    }

    public SidangDto getPenguji(Sidang sidang){
        SidangDto sidangDto = new SidangDto();
        sidangDto.setId(sidang.getId());
        sidangDto.setKomentar(sidang.getKomentarPenguji());
        sidangDto.setNilaiA(sidang.getUa());
        sidangDto.setNilaiB(sidang.getUb());
        sidangDto.setNilaiC(sidang.getUc());
        sidangDto.setNilaiD(sidang.getUd());

        return sidangDto;
    }

    public SidangDto getPembimbing(Sidang sidang){
        SidangDto sidangDto = new SidangDto();
        sidangDto.setId(sidang.getId());
        sidangDto.setKomentar(sidang.getKomentarPembimbing());
        sidangDto.setNilaiA(sidang.getPa());
        sidangDto.setNilaiB(sidang.getPb());
        sidangDto.setNilaiC(sidang.getPc());
        sidangDto.setNilaiD(sidang.getPd());

        return sidangDto;
    }

    public SidangDto getPembimbing2(Sidang sidang){
        SidangDto sidangDto = new SidangDto();
        sidangDto.setId(sidang.getId());
        sidangDto.setKomentar(sidang.getKomentarPembimbing2());
        sidangDto.setNilaiA(sidang.getPa2());
        sidangDto.setNilaiB(sidang.getPb2());
        sidangDto.setNilaiC(sidang.getPc2());
        sidangDto.setNilaiD(sidang.getPd2());

        return sidangDto;
    }

    public void saveKetua(SidangDto sidangDto){
        Sidang sidang = sidangDao.findById(sidangDto.getId()).get();
        sidang.setKomentarKetua(sidangDto.getKomentar());
        sidang.setBeritaAcara(sidangDto.getBeritaAcara());
        sidang.setKa(sidangDto.getNilaiA());
        sidang.setKb(sidangDto.getNilaiB());
        sidang.setKc(sidangDto.getNilaiC());
        sidang.setKd(sidangDto.getNilaiD());
        sidangDao.save(sidang);
        akumulasiNilai(sidang);

        System.out.println("berhasil");

    }

    public void saveKetuaPasca(SidangDto sidangDto){
        Sidang sidang = sidangDao.findById(sidangDto.getId()).get();
        sidang.setKomentarKetua(sidangDto.getKomentar());
        sidang.setBeritaAcara(sidangDto.getBeritaAcara());
        sidang.setKa(sidangDto.getNilaiA());
        sidang.setKb(sidangDto.getNilaiB());
        sidang.setKc(sidangDto.getNilaiC());
        sidang.setKd(sidangDto.getNilaiD());
        sidangDao.save(sidang);
        akumulasiNilaiPasca(sidang);

        System.out.println("berhasil");

    }

    public void saveKetuaSeminar(SemproDto semproDto){
        Seminar seminar = seminarDao.findById(semproDto.getId()).get();
        seminar.setKomentarKetua(semproDto.getKomentar());
        seminar.setBeritaAcara(semproDto.getBeritaAcara());
        seminar.setKa(semproDto.getNilaiA());
        seminar.setKb(semproDto.getNilaiB());
        seminar.setKc(semproDto.getNilaiC());
        seminar.setKd(semproDto.getNilaiD());
        seminar.setKe(semproDto.getNilaiE());
        seminarDao.save(seminar);
        akumulasiNilaiSempro(seminar);
        System.out.println("berhasil");

    }

    public void savePenguji(SidangDto sidangDto){
        Sidang sidang = sidangDao.findById(sidangDto.getId()).get();
        sidang.setKomentarPenguji(sidangDto.getKomentar());
        sidang.setUa(sidangDto.getNilaiA());
        sidang.setUb(sidangDto.getNilaiB());
        sidang.setUc(sidangDto.getNilaiC());
        sidang.setUd(sidangDto.getNilaiD());
        akumulasiNilai(sidang);

    }

    public void savePengujiPasca(SidangDto sidangDto){
        Sidang sidang = sidangDao.findById(sidangDto.getId()).get();
        sidang.setKomentarPenguji(sidangDto.getKomentar());
        sidang.setUa(sidangDto.getNilaiA());
        sidang.setUb(sidangDto.getNilaiB());
        sidang.setUc(sidangDto.getNilaiC());
        sidang.setUd(sidangDto.getNilaiD());
        akumulasiNilaiPasca(sidang);

    }

    public void savePengujiSempro(SemproDto semproDto){
        Seminar seminar = seminarDao.findById(semproDto.getId()).get();
        seminar.setKomentarPenguji(semproDto.getKomentar());
        seminar.setUa(semproDto.getNilaiA());
        seminar.setUb(semproDto.getNilaiB());
        seminar.setUc(semproDto.getNilaiC());
        seminar.setUd(semproDto.getNilaiD());
        seminar.setUe(semproDto.getNilaiE());
        seminarDao.save(seminar);
        akumulasiNilaiSempro(seminar);

    }

    public void savePembimbingSempro(SemproDto semproDto){
        Seminar seminar = seminarDao.findById(semproDto.getId()).get();
        seminar.setKomentarPenguji(semproDto.getKomentar());
        seminar.setPa(semproDto.getNilaiA());
        seminar.setPb(semproDto.getNilaiB());
        seminar.setPc(semproDto.getNilaiC());
        seminar.setPd(semproDto.getNilaiD());
        seminar.setPe(semproDto.getNilaiE());
        seminarDao.save(seminar);
        akumulasiNilaiSempro(seminar);

    }

    public void savePembimbing2Sempro(SemproDto semproDto){
        Seminar seminar = seminarDao.findById(semproDto.getId()).get();
        seminar.setKomentarPenguji(semproDto.getKomentar());
        seminar.setPa2(semproDto.getNilaiA());
        seminar.setPb2(semproDto.getNilaiB());
        seminar.setPc2(semproDto.getNilaiC());
        seminar.setPd2(semproDto.getNilaiD());
        seminar.setPe2(semproDto.getNilaiE());
        seminarDao.save(seminar);
        akumulasiNilaiSempro(seminar);

    }

    public void savePembimbing(SidangDto sidangDto){
        Sidang sidang = sidangDao.findById(sidangDto.getId()).get();
        sidang.setKomentarPembimbing(sidangDto.getKomentar());
        sidang.setPa(sidangDto.getNilaiA());
        sidang.setPb(sidangDto.getNilaiB());
        sidang.setPc(sidangDto.getNilaiC());
        sidang.setPd(sidangDto.getNilaiD());
        sidangDao.save(sidang);
        akumulasiNilai(sidang);

    }

    public void savePembimbingPasca(SidangDto sidangDto){
        Sidang sidang = sidangDao.findById(sidangDto.getId()).get();
        sidang.setKomentarPembimbing(sidangDto.getKomentar());
        sidang.setPa(sidangDto.getNilaiA());
        sidang.setPb(sidangDto.getNilaiB());
        sidang.setPc(sidangDto.getNilaiC());
        sidang.setPd(sidangDto.getNilaiD());
        sidangDao.save(sidang);
        akumulasiNilaiPasca(sidang);

    }

    public void savePembimbing2(SidangDto sidangDto){
        Sidang sidang = sidangDao.findById(sidangDto.getId()).get();
        sidang.setKomentarPembimbing2(sidangDto.getKomentar());
        sidang.setPa2(sidangDto.getNilaiA());
        sidang.setPb2(sidangDto.getNilaiB());
        sidang.setPc2(sidangDto.getNilaiC());
        sidang.setPd2(sidangDto.getNilaiD());
        sidangDao.save(sidang);
        akumulasiNilaiPasca(sidang);

    }

    private void akumulasiNilai(Sidang sidang){
        BigDecimal nilaiA = sidang.getKa().add(sidang.getUa()).add(sidang.getPa()).divide(BigDecimal.valueOf(3), 2, RoundingMode.HALF_UP).multiply(BigDecimal.valueOf(0.4));
        BigDecimal nilaiB = sidang.getKb().add(sidang.getUb()).add(sidang.getPb()).divide(BigDecimal.valueOf(3), 2, RoundingMode.HALF_UP).multiply(BigDecimal.valueOf(0.3));
        BigDecimal nilaiC = sidang.getKc().add(sidang.getUc()).add(sidang.getPc()).divide(BigDecimal.valueOf(3), 2, RoundingMode.HALF_UP).multiply(BigDecimal.valueOf(0.15));
        BigDecimal nilaiD = sidang.getKd().add(sidang.getUd()).add(sidang.getPd()).divide(BigDecimal.valueOf(3), 2, RoundingMode.HALF_UP).multiply(BigDecimal.valueOf(0.15));
        sidang.setNilaiA(nilaiA);
        sidang.setNilaiB(nilaiB);
        sidang.setNilaiC(nilaiC);
        sidang.setNilaiD(nilaiD);
        sidang.setNilai(nilaiA.add(nilaiB).add(nilaiC).add(nilaiD));
        sidangDao.save(sidang);
    }

    private void akumulasiNilaiPasca(Sidang sidang){
        BigDecimal nilaiA = sidang.getKa().add(sidang.getUa()).add(sidang.getPa()).add(sidang.getPa2()).divide(BigDecimal.valueOf(4), 2, RoundingMode.HALF_UP).multiply(BigDecimal.valueOf(0.2));
        BigDecimal nilaiB = sidang.getKb().add(sidang.getUb()).add(sidang.getPb()).add(sidang.getPb2()).divide(BigDecimal.valueOf(4), 2, RoundingMode.HALF_UP).multiply(BigDecimal.valueOf(0.3));
        BigDecimal nilaiC = sidang.getKc().add(sidang.getUc()).add(sidang.getPc()).add(sidang.getPc2()).divide(BigDecimal.valueOf(4), 2, RoundingMode.HALF_UP).multiply(BigDecimal.valueOf(0.3));
        BigDecimal nilaiD = sidang.getKd().add(sidang.getUd()).add(sidang.getPd()).add(sidang.getPd2()).divide(BigDecimal.valueOf(4), 2, RoundingMode.HALF_UP).multiply(BigDecimal.valueOf(0.2));
        sidang.setNilaiA(nilaiA);
        sidang.setNilaiB(nilaiB);
        sidang.setNilaiC(nilaiC);
        sidang.setNilaiD(nilaiD);
        sidang.setNilai(nilaiA.add(nilaiB).add(nilaiC).add(nilaiD));
        sidangDao.save(sidang);
    }

    private void akumulasiNilaiSempro(Seminar seminar){
        BigDecimal nilaiA = seminar.getKa().add(seminar.getUa()).add(seminar.getPa()).add(seminar.getPa2()).divide(BigDecimal.valueOf(4), 2, RoundingMode.HALF_UP).multiply(BigDecimal.valueOf(0.2));
        BigDecimal nilaiB = seminar.getKb().add(seminar.getUb()).add(seminar.getPb()).add(seminar.getPb2()).divide(BigDecimal.valueOf(4), 2, RoundingMode.HALF_UP).multiply(BigDecimal.valueOf(0.3));
        BigDecimal nilaiC = seminar.getKc().add(seminar.getUc()).add(seminar.getPc()).add(seminar.getPc2()).divide(BigDecimal.valueOf(4), 2, RoundingMode.HALF_UP).multiply(BigDecimal.valueOf(0.1));
        BigDecimal nilaiD = seminar.getKd().add(seminar.getUd()).add(seminar.getPd()).add(seminar.getPd2()).divide(BigDecimal.valueOf(4), 2, RoundingMode.HALF_UP).multiply(BigDecimal.valueOf(0.1));
        BigDecimal nilaiE = seminar.getKe().add(seminar.getUe()).add(seminar.getPe()).add(seminar.getPe2()).divide(BigDecimal.valueOf(4), 2, RoundingMode.HALF_UP).multiply(BigDecimal.valueOf(0.3));
        seminar.setNilaiA(nilaiA);
        seminar.setNilaiB(nilaiB);
        seminar.setNilaiC(nilaiC);
        seminar.setNilaiD(nilaiD);
        seminar.setNilaiE(nilaiE);
        seminar.setNilai(nilaiA.add(nilaiB).add(nilaiC).add(nilaiD).add(nilaiE));
        seminarDao.save(seminar);
    }

    // CREATE SURAT TUGAS SEMINAR
    public void suratTugasSeminar(@RequestParam Seminar seminar, HttpServletResponse response) throws IOException, DocumentException {

        Document document = new Document(PageSize.A4);
        PdfWriter.getInstance(document, response.getOutputStream());

        String tgl = seminar.getTanggalInput().toString().substring(5,7);
        String tahun = seminar.getTanggalInput().toString().substring(0,4);
        String kdBln = "";
        System.out.println("tes bulan :" + tgl);
        System.out.println("tes tahun :" + tahun);
        switch (tgl){
            case "01":
                kdBln = "I";
                break;
            case "02":
                kdBln = "II";
                break;
            case "03":
                kdBln = "III";
                break;
            case "04":
                kdBln = "IV";
                break;
            case "05":
                kdBln = "V";
                break;
            case "06":
                kdBln = "VI";
                break;
            case "07":
                kdBln = "VII";
                break;
            case "08":
                kdBln = "VIII";
                break;
            case "09":
                kdBln = "IX";
                break;
            case "10":
                kdBln = "X";
                break;
            case "11":
                kdBln = "XI";
                break;
            case "12":
                kdBln = "XII";
                break;
            default:
                kdBln = "";
                break;
        }
        System.out.println("romawi : " + kdBln);

        HijrahDate islamicDate = HijrahDate.from(seminar.getTanggalInput());
        String namaBulanHijri = islamicDate.format(DateTimeFormatter.ofPattern("MMMM", new Locale("en")));
        String tanggalHijri = islamicDate.format(DateTimeFormatter.ofPattern("d", new Locale("en")));
        String tahunHijri = islamicDate.format(DateTimeFormatter.ofPattern("yyyy", new Locale("en")));

        if ("Jumada I".equals(namaBulanHijri)) {
            namaBulanHijri = "Jumadil Awal";
        } else if ("Jumada II".equals(namaBulanHijri)) {
            namaBulanHijri = "Jumadil Akhir";
        } else if ("Rabiʻ I".equals(namaBulanHijri)) {
            namaBulanHijri = "Rabi'ul Awal";
        }else if ("Rabiʻ II".equals(namaBulanHijri)){
            namaBulanHijri = "Rabi'ul Akhir";
        }else {
            namaBulanHijri = namaBulanHijri;
        }
        String kode = "";
        String existingKode = seminar.getKodeSuratTugas();
        if (existingKode == null) {
            KodeSidang cek = kodeSidangDao.findTopByProdiAndTahunAndStatusOrderByKodeDesc(seminar.getNote().getMahasiswa().getIdProdi(), tahun, StatusRecord.AKTIF);
            if (cek == null) {
                KodeSidang newKode = new KodeSidang();
                newKode.setKode(1);
                newKode.setKodeString("001");
                newKode.setTahun(tahun);
                newKode.setProdi(seminar.getNote().getMahasiswa().getIdProdi());
                newKode.setStatus(StatusRecord.AKTIF);
                kodeSidangDao.save(newKode);
                kode = "No. " + newKode.getKodeString() + "-A/IAI TAZKIA/Prodi-" + seminar.getNote().getMahasiswa().getIdProdi().getKodeProdi() + "/" + kdBln + "/" + tahun;

                seminar.setKodeSuratTugas(kode);
            }else{
                KodeSidang addKode = new KodeSidang();
                Integer newCode = cek.getKode() + 1;
                String kodeString = String.format("%03d", newCode);
                addKode.setKodeString(kodeString);
                addKode.setKode(newCode);
                addKode.setTahun(tahun);
                addKode.setProdi(seminar.getNote().getMahasiswa().getIdProdi());
                addKode.setStatus(StatusRecord.AKTIF);
                kodeSidangDao.save(addKode);
                kode = "No. " + addKode.getKodeString() + "-A/IAI TAZKIA/Prodi-" + seminar.getNote().getMahasiswa().getIdProdi().getKodeProdi() + "/" + kdBln + "/" + tahun;

                seminar.setKodeSuratTugas(kode);
            }
        }else{
            kode = seminar.getKodeSuratTugas();
        }

        seminarDao.save(seminar);

        document.open();

        Image background = Image.getInstance(logo + File.separator + "LOGO TR.png");
        background.setAbsolutePosition(400, 190);
        background.scaleAbsolute(400, 450);
        document.add(background);

        Font titleFont = FontFactory.getFont(FontFactory.TIMES_BOLD, 12);
        titleFont.setStyle(Font.UNDERLINE);
        Font kodeFont = FontFactory.getFont(FontFactory.TIMES_BOLD, 12);
        Font fStandard = FontFactory.getFont(FontFactory.TIMES_ROMAN, 12);
        Font fontB = FontFactory.getFont(FontFactory.TIMES_BOLD, 12);

        Paragraph paragrafkosong = new Paragraph("");

        Image img = Image.getInstance(logo + File.separator + "bismillah.png");
//        img.setWidthPercentage(20);
        img.scaleToFit(140, 140);
        img.setAbsolutePosition(222, PageSize.A4.getHeight() - (img.getScaledHeight()) - 40);
        Image lTazkia = Image.getInstance(logo + File.separator + "LOGO.png");
        lTazkia.scaleToFit(100, 100);
        lTazkia.setAbsolutePosition(20, PageSize.A4.getHeight() - (lTazkia.getScaledHeight()) - 15);
        Paragraph newLine = new Paragraph("\n\n");
        document.add(img);
        document.add(lTazkia);
        document.add(newLine);

        Paragraph titleParagraph = new Paragraph("SURAT TUGAS", titleFont);
        titleParagraph.setAlignment(Paragraph.ALIGN_CENTER);
        document.add(titleParagraph);

        Paragraph kodeSurat = new Paragraph(kode);
        kodeSurat.setAlignment(Paragraph.ALIGN_CENTER);
        kodeSurat.setFont(kodeFont);
        document.add(kodeSurat);

        Paragraph nLine = new Paragraph("\n");
        Image salam = Image.getInstance(logo + File.separator + "salam.png");
        salam.scaleToFit(140, 140);
        salam.setAbsolutePosition(85, PageSize.A4.getHeight() - (salam.getScaledHeight()) - 115);
        document.add(salam);
        document.add(nLine);

        paragrafkosong = new Paragraph("Dengan senantiasa mengharap rahmat dan ridha Allah, yang bertanda tangan di bawah ini :", fStandard);
        paragrafkosong.setAlignment(Element.ALIGN_BASELINE);
        paragrafkosong.setIndentationLeft(50);
        document.add(paragrafkosong);

        PdfPTable table = new PdfPTable(3);
        table.setWidthPercentage(70);
        table.setWidths(new int[] {3,1,15});
        table.setSpacingBefore(10);
        Font font = FontFactory.getFont(FontFactory.TIMES_ROMAN);
        font.setColor(CMYKColor.BLACK);

        PdfPCell cell;
        cell = new PdfPCell(new Phrase("Nama",font));
        cell.setBorder(Rectangle.NO_BORDER);
        cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
        table.addCell(cell);
        cell = new PdfPCell(new Phrase(":", font));
        cell.setBorder(Rectangle.NO_BORDER);
        cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
        table.addCell(cell);
        cell = new PdfPCell(new Phrase(seminar.getNote().getMahasiswa().getIdProdi().getDosen().getKaryawan().getNamaKaryawan(), font));
        cell.setBorder(Rectangle.NO_BORDER);
        cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
        table.addCell(cell);
        cell = new PdfPCell(new Phrase("NIDN",font));
        cell.setBorder(Rectangle.NO_BORDER);
        cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
        table.addCell(cell);
        cell = new PdfPCell(new Phrase(":", font));
        cell.setBorder(Rectangle.NO_BORDER);
        cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
        table.addCell(cell);
        cell = new PdfPCell(new Phrase(seminar.getNote().getMahasiswa().getIdProdi().getDosen().getKaryawan().getNidn(), font));
        cell.setBorder(Rectangle.NO_BORDER);
        cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
        table.addCell(cell);
        cell = new PdfPCell(new Phrase("Jabatan",font));
        cell.setBorder(Rectangle.NO_BORDER);
        cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
        table.addCell(cell);
        cell = new PdfPCell(new Phrase(":", font));
        cell.setBorder(Rectangle.NO_BORDER);
        cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
        table.addCell(cell);
        cell = new PdfPCell(new Phrase(seminar.getNote().getMahasiswa().getIdProdi().getJabatan(), font));
        cell.setBorder(Rectangle.NO_BORDER);
        cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
        table.addCell(cell);
        cell = new PdfPCell(new Phrase("Unit Kerja",font));
        cell.setBorder(Rectangle.NO_BORDER);
        cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
        table.addCell(cell);
        cell = new PdfPCell(new Phrase(":", font));
        cell.setBorder(Rectangle.NO_BORDER);
        cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
        table.addCell(cell);
        cell = new PdfPCell(new Phrase("Institut Agama Islam Tazkia", font));
        cell.setBorder(Rectangle.NO_BORDER);
        cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
        table.addCell(cell);

        document.add(table);

        paragrafkosong = new Paragraph("Memberi tugas kepada: ", fStandard);
        paragrafkosong.setAlignment(Element.ALIGN_BASELINE);
        paragrafkosong.setIndentationLeft(50);
        document.add(paragrafkosong);

        PdfPTable table1 = new PdfPTable(4);
        table1.setSpacingBefore(15);
        table1.setWidthPercentage(90);
        table1.setWidths(new int[] {3, 5, 8, 5});

        Font fontHeader = FontFactory.getFont(FontFactory.TIMES_BOLD, 12);
        Font fontIsi = FontFactory.getFont(FontFactory.TIMES_ROMAN, 12);

        cell = new PdfPCell(new Phrase("No. ", fontHeader));
        cell.setHorizontalAlignment(Element.ALIGN_CENTER);
        cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
        table1.addCell(cell);
        cell = new PdfPCell(new Phrase("NIDN ", fontHeader));
        cell.setHorizontalAlignment(Element.ALIGN_CENTER);
        cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
        table1.addCell(cell);
        cell = new PdfPCell(new Phrase("Nama ", fontHeader));
        cell.setHorizontalAlignment(Element.ALIGN_CENTER);
        cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
        table1.addCell(cell);
        cell = new PdfPCell(new Phrase("Sebagai ", fontHeader));
        cell.setHorizontalAlignment(Element.ALIGN_CENTER);
        cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
        table1.addCell(cell);
        cell = new PdfPCell(new Phrase("1 ", fontIsi));
        cell.setHorizontalAlignment(Element.ALIGN_CENTER);
        cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
        table1.addCell(cell);
        cell = new PdfPCell(new Phrase(seminar.getKetuaPenguji().getKaryawan().getNidn(), fontIsi));
        cell.setHorizontalAlignment(Element.ALIGN_CENTER);
        cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
        table1.addCell(cell);
        cell = new PdfPCell(new Phrase(seminar.getKetuaPenguji().getKaryawan().getNamaKaryawan(), fontIsi));
        cell.setHorizontalAlignment(Element.ALIGN_CENTER);
        cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
        table1.addCell(cell);
        cell = new PdfPCell(new Phrase("Ketua Penguji ", fontIsi));
        cell.setHorizontalAlignment(Element.ALIGN_CENTER);
        cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
        table1.addCell(cell);
        cell = new PdfPCell(new Phrase("2 ", fontIsi));
        cell.setHorizontalAlignment(Element.ALIGN_CENTER);
        cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
        table1.addCell(cell);
        cell = new PdfPCell(new Phrase(seminar.getDosenPenguji().getKaryawan().getNidn(), fontIsi));
        cell.setHorizontalAlignment(Element.ALIGN_CENTER);
        cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
        table1.addCell(cell);
        cell = new PdfPCell(new Phrase(seminar.getDosenPenguji().getKaryawan().getNamaKaryawan(), fontIsi));
        cell.setHorizontalAlignment(Element.ALIGN_CENTER);
        cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
        table1.addCell(cell);
        cell = new PdfPCell(new Phrase("Anggota Penguji ", fontIsi));
        cell.setHorizontalAlignment(Element.ALIGN_CENTER);
        cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
        table1.addCell(cell);
        cell = new PdfPCell(new Phrase("3 ", fontIsi));
        cell.setHorizontalAlignment(Element.ALIGN_CENTER);
        cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
        table1.addCell(cell);
        cell = new PdfPCell(new Phrase(seminar.getNote().getDosen().getKaryawan().getNidn(), fontIsi));
        cell.setHorizontalAlignment(Element.ALIGN_CENTER);
        cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
        table1.addCell(cell);
        cell = new PdfPCell(new Phrase(seminar.getNote().getDosen().getKaryawan().getNamaKaryawan(), fontIsi));
        cell.setHorizontalAlignment(Element.ALIGN_CENTER);
        cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
        table1.addCell(cell);
        cell = new PdfPCell(new Phrase("Pembimbing ", fontIsi));
        cell.setHorizontalAlignment(Element.ALIGN_CENTER);
        cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
        table1.addCell(cell);

        document.add(table1);

        paragrafkosong = new Paragraph();
        paragrafkosong.add(new Chunk("Untuk menguji ", fStandard));
        paragrafkosong.add(new Chunk("Sidang Seminar Proposal", fontB));
        paragrafkosong.add(new Chunk(" mahasiswa Program Studi ", fStandard));
        paragrafkosong.add(new Chunk(seminar.getNote().getMahasiswa().getIdProdi().getNamaProdi(), fStandard));
        paragrafkosong.add(new Chunk("Institut Agama Islam Tazkia ", fStandard));
        paragrafkosong.add(new Chunk(seminar.getTahunAkademik().getNamaTahunAkademik(), fStandard));
        paragrafkosong.add(new Chunk(" sebagai berikut:", fStandard));
        paragrafkosong.setAlignment(Element.ALIGN_BASELINE);
        paragrafkosong.setIndentationLeft(50);
        document.add(paragrafkosong);

        PdfPTable table2 = new PdfPTable(3);
        table2.setWidthPercentage(70);
        table2.setWidths(new int[] {3,1,15});
        table2.setSpacingBefore(10);
        Font font2 = FontFactory.getFont(FontFactory.TIMES_ROMAN);
        font2.setColor(CMYKColor.BLACK);

        cell = new PdfPCell(new Phrase("Nama",font2));
        cell.setBorder(Rectangle.NO_BORDER);
        cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
        cell.setHorizontalAlignment(Element.ALIGN_LEFT);
        table2.addCell(cell);
        cell = new PdfPCell(new Phrase(":", font2));
        cell.setBorder(Rectangle.NO_BORDER);
        cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
        cell.setHorizontalAlignment(Element.ALIGN_CENTER);
        table2.addCell(cell);
        cell = new PdfPCell(new Phrase(seminar.getNote().getMahasiswa().getNama(), font2));
        cell.setBorder(Rectangle.NO_BORDER);
        cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
        cell.setHorizontalAlignment(Element.ALIGN_LEFT);
        table2.addCell(cell);
        cell = new PdfPCell(new Phrase("NIM",font2));
        cell.setBorder(Rectangle.NO_BORDER);
        cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
        cell.setHorizontalAlignment(Element.ALIGN_LEFT);
        table2.addCell(cell);
        cell = new PdfPCell(new Phrase(":", font2));
        cell.setBorder(Rectangle.NO_BORDER);
        cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
        cell.setHorizontalAlignment(Element.ALIGN_CENTER);
        table2.addCell(cell);
        cell = new PdfPCell(new Phrase(seminar.getNote().getMahasiswa().getNim(), font2));
        cell.setBorder(Rectangle.NO_BORDER);
        cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
        cell.setHorizontalAlignment(Element.ALIGN_LEFT);
        table2.addCell(cell);
        cell = new PdfPCell(new Phrase("Judul",font2));
        cell.setBorder(Rectangle.NO_BORDER);
        cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
        cell.setHorizontalAlignment(Element.ALIGN_LEFT);
        table2.addCell(cell);
        cell = new PdfPCell(new Phrase(":", font2));
        cell.setBorder(Rectangle.NO_BORDER);
        cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
        cell.setHorizontalAlignment(Element.ALIGN_CENTER);
        table2.addCell(cell);
        cell = new PdfPCell(new Phrase(seminar.getNote().getJudul(), font2));
        cell.setBorder(Rectangle.NO_BORDER);
        cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
        cell.setHorizontalAlignment(Element.ALIGN_LEFT);
        table2.addCell(cell);

        document.add(table2);

        paragrafkosong = new Paragraph("Yang dilaksanakan pada: ", fStandard);
        paragrafkosong.setAlignment(Element.ALIGN_BASELINE);
        paragrafkosong.setIndentationLeft(50);
        document.add(paragrafkosong);

        PdfPTable table3 = new PdfPTable(3);
        table3.setWidthPercentage(70);
        table3.setWidths(new int[] {4,1,15});
        table3.setSpacingBefore(10);
        Font font3 = FontFactory.getFont(FontFactory.TIMES_ROMAN, 12);
        font2.setColor(CMYKColor.BLACK);

        cell = new PdfPCell(new Phrase("Hari/Tanggal",font3));
        cell.setBorder(Rectangle.NO_BORDER);
        cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
        cell.setHorizontalAlignment(Element.ALIGN_LEFT);
        table3.addCell(cell);
        cell = new PdfPCell(new Phrase(":", font3));
        cell.setBorder(Rectangle.NO_BORDER);
        cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
        cell.setHorizontalAlignment(Element.ALIGN_CENTER);
        table3.addCell(cell);
        cell = new PdfPCell(new Phrase(hariDao.findById(String.valueOf(seminar.getTanggalUjian().getDayOfWeek().getValue())).get().getNamaHari() + ", " + seminar.getTanggalUjian().format(DateTimeFormatter.ofPattern("dd-MMM-yyyy")), font3));
        cell.setBorder(Rectangle.NO_BORDER);
        cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
        cell.setHorizontalAlignment(Element.ALIGN_LEFT);
        table3.addCell(cell);
        cell = new PdfPCell(new Phrase("Waktu",font));
        cell.setBorder(Rectangle.NO_BORDER);
        cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
        cell.setHorizontalAlignment(Element.ALIGN_LEFT);
        table3.addCell(cell);
        cell = new PdfPCell(new Phrase(":", font));
        cell.setBorder(Rectangle.NO_BORDER);
        cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
        cell.setHorizontalAlignment(Element.ALIGN_CENTER);
        table3.addCell(cell);
        Phrase w = new Phrase();
        w.add(new Chunk("Pkl ", font));
        w.add(new Chunk(seminar.getJamMulai().toString() + " - " + seminar.getJamSelesai().toString() + " WIB", fontB));
        cell = new PdfPCell(new Phrase(w));
        cell.setBorder(Rectangle.NO_BORDER);
        cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
        cell.setHorizontalAlignment(Element.ALIGN_LEFT);
        table3.addCell(cell);
        cell = new PdfPCell(new Phrase("Tempat",font));
        cell.setBorder(Rectangle.NO_BORDER);
        cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
        cell.setHorizontalAlignment(Element.ALIGN_LEFT);
        table3.addCell(cell);
        cell = new PdfPCell(new Phrase(":", font));
        cell.setBorder(Rectangle.NO_BORDER);
        cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
        cell.setHorizontalAlignment(Element.ALIGN_CENTER);
        table3.addCell(cell);
        cell = new PdfPCell(new Phrase(seminar.getRuangan().getNamaRuangan(), fontB));
        cell.setBorder(Rectangle.NO_BORDER);
        cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
        cell.setHorizontalAlignment(Element.ALIGN_LEFT);
        table3.addCell(cell);

        document.add(table3);

        paragrafkosong = new Paragraph("Demikian pernyataan ini dibuat untuk dapat dipergunakan sebagaimana mestinya.", fStandard);
        paragrafkosong.setAlignment(Element.ALIGN_BASELINE);
        paragrafkosong.setIndentationLeft(50);
        document.add(paragrafkosong);

        Image wassalam = Image.getInstance(logo + File.separator + "wassalam.png");
        wassalam.scaleToFit(140, 140);
        wassalam.setAbsolutePosition(85, PageSize.A4.getHeight() - (table1.getTotalHeight()) - (table2.getTotalHeight()) - 415);
        document.add(wassalam);

        document.add(nLine);
        document.add(nLine);
        paragrafkosong = new Paragraph();
        paragrafkosong.add(new Chunk("Bogor, ", fStandard));
        paragrafkosong.add(new Chunk(seminar.getTanggalUjian().format(DateTimeFormatter.ofPattern("dd MMM yyyy")) + " M"));
        paragrafkosong.add(new Chunk(" / ", fStandard));
        paragrafkosong.add(new Chunk(tanggalHijri+ " " + namaBulanHijri + " " + tahunHijri + " H", fStandard));
        paragrafkosong.setAlignment(Element.ALIGN_BASELINE);
        paragrafkosong.setIndentationLeft(50);
        document.add(paragrafkosong);

        Image ttd = Image.getInstance(tandaTangan + File.separator + seminar.getNote().getMahasiswa().getIdProdi().getDosen().getKaryawan().getTandaTangan());
        ttd.scaleToFit(100, 100);
        ttd.setAbsolutePosition(85, PageSize.A4.getHeight() - (table1.getTotalHeight()) - (table2.getTotalHeight()) - 520);
        document.add(ttd);

        document.add(nLine);
        document.add(nLine);
        document.add(nLine);
        document.add(nLine);

        Paragraph kpsName = new Paragraph(seminar.getNote().getMahasiswa().getIdProdi().getDosen().getKaryawan().getNamaKaryawan(), titleFont);
        kpsName.setAlignment(Paragraph.ALIGN_BASELINE);
        kpsName.setIndentationLeft(50);
        document.add(kpsName);

        Paragraph nidn = new Paragraph("NIDN: " + seminar.getNote().getMahasiswa().getIdProdi().getDosen().getKaryawan().getNidn());
        nidn.setAlignment(Paragraph.ALIGN_BASELINE);
        nidn.setIndentationLeft(50);
        nidn.setFont(kodeFont);
        document.add(nidn);

        Image footer = Image.getInstance(logo + File.separator + "footer.png");
        footer.scaleToFit(document.getPageSize().getWidth() - 20, 200);
        footer.setAbsolutePosition( 10, 10);
        document.add(footer);

        document.close();

    }

    // CREATE SURAT TUGAS SIDANG
    public void suratTugasSidang(@RequestParam Sidang sidang, HttpServletResponse response) throws IOException, DocumentException {

        Document document = new Document(PageSize.A4);
        PdfWriter.getInstance(document, response.getOutputStream());

        String tgl = sidang.getTanggalInput().toString().substring(5,7);
        String tahun = sidang.getTanggalInput().toString().substring(0,4);
        String kdBln = "";
        System.out.println("tes bulan :" + tgl);
        System.out.println("tes tahun :" + tahun);
        switch (tgl){
            case "01":
                kdBln = "I";
                break;
            case "02":
                kdBln = "II";
                break;
            case "03":
                kdBln = "III";
                break;
            case "04":
                kdBln = "IV";
                break;
            case "05":
                kdBln = "V";
                break;
            case "06":
                kdBln = "VI";
                break;
            case "07":
                kdBln = "VII";
                break;
            case "08":
                kdBln = "VIII";
                break;
            case "09":
                kdBln = "IX";
                break;
            case "10":
                kdBln = "X";
                break;
            case "11":
                kdBln = "XI";
                break;
            case "12":
                kdBln = "XII";
                break;
            default:
                kdBln = "";
                break;
        }
        System.out.println("romawi : " + kdBln);

        HijrahDate islamicDate = HijrahDate.from(sidang.getTanggalInput());
        String namaBulanHijri = islamicDate.format(DateTimeFormatter.ofPattern("MMMM", new Locale("en")));
        String tanggalHijri = islamicDate.format(DateTimeFormatter.ofPattern("d", new Locale("en")));
        String tahunHijri = islamicDate.format(DateTimeFormatter.ofPattern("yyyy", new Locale("en")));

        if ("Jumada I".equals(namaBulanHijri)) {
            namaBulanHijri = "Jumadil Awal";
        } else if ("Jumada II".equals(namaBulanHijri)) {
            namaBulanHijri = "Jumadil Akhir";
        } else if ("Rabiʻ I".equals(namaBulanHijri)) {
            namaBulanHijri = "Rabi'ul Awal";
        }else if ("Rabiʻ II".equals(namaBulanHijri)){
            namaBulanHijri = "Rabi'ul Akhir";
        }else {
            namaBulanHijri = namaBulanHijri;
        }
        String kode = "";
        String existingKode = sidang.getKodeSuratTugas();
        if (existingKode == null) {
            KodeSidang cek = kodeSidangDao.findTopByProdiAndTahunAndStatusOrderByKodeDesc(sidang.getSeminar().getNote().getMahasiswa().getIdProdi(), tahun, StatusRecord.AKTIF);
            if (cek == null) {
                KodeSidang newKode = new KodeSidang();
                newKode.setKode(1);
                newKode.setKodeString("001");
                newKode.setTahun(tahun);
                newKode.setProdi(sidang.getSeminar().getNote().getMahasiswa().getIdProdi());
                newKode.setStatus(StatusRecord.AKTIF);
                kodeSidangDao.save(newKode);
                kode = "No. " + newKode.getKodeString() + "-A/IAI TAZKIA/Prodi-" + sidang.getSeminar().getNote().getMahasiswa().getIdProdi().getKodeProdi() + "/" + kdBln + "/" + tahun;

                sidang.setKodeSuratTugas(kode);
            }else{
                KodeSidang addKode = new KodeSidang();
                Integer newCode = cek.getKode() + 1;
                String kodeString = String.format("%03d", newCode);
                addKode.setKodeString(kodeString);
                addKode.setKode(newCode);
                addKode.setTahun(tahun);
                addKode.setProdi(sidang.getSeminar().getNote().getMahasiswa().getIdProdi());
                addKode.setStatus(StatusRecord.AKTIF);
                kodeSidangDao.save(addKode);
                kode = "No. " + addKode.getKodeString() + "-A/IAI TAZKIA/Prodi-" + sidang.getSeminar().getNote().getMahasiswa().getIdProdi().getKodeProdi() + "/" + kdBln + "/" + tahun;

                sidang.setKodeSuratTugas(kode);
            }
        }else{
            kode = sidang.getKodeSuratTugas();
        }

        sidangDao.save(sidang);

        document.open();

        Image background = Image.getInstance(logo + File.separator + "LOGO TR.png");
        background.setAbsolutePosition(400, 190);
        background.scaleAbsolute(400, 450);
        document.add(background);

        Font titleFont = FontFactory.getFont(FontFactory.TIMES_BOLD, 12);
        titleFont.setStyle(Font.UNDERLINE);
        Font kodeFont = FontFactory.getFont(FontFactory.TIMES_BOLD, 12);
        Font fStandard = FontFactory.getFont(FontFactory.TIMES_ROMAN, 12);
        Font fontB = FontFactory.getFont(FontFactory.TIMES_BOLD, 12);

        Paragraph paragrafkosong = new Paragraph("");

        Image img = Image.getInstance(logo + File.separator + "bismillah.png");
//        img.setWidthPercentage(20);
        img.scaleToFit(140, 140);
        img.setAbsolutePosition(222, PageSize.A4.getHeight() - (img.getScaledHeight()) - 40);
        Image lTazkia = Image.getInstance(logo + File.separator + "LOGO.png");
        lTazkia.scaleToFit(100, 100);
        lTazkia.setAbsolutePosition(20, PageSize.A4.getHeight() - (lTazkia.getScaledHeight()) - 15);
        Paragraph newLine = new Paragraph("\n\n");
        document.add(img);
        document.add(lTazkia);
        document.add(newLine);

        Paragraph titleParagraph = new Paragraph("SURAT TUGAS", titleFont);
        titleParagraph.setAlignment(Paragraph.ALIGN_CENTER);
        document.add(titleParagraph);

        Paragraph kodeSurat = new Paragraph(kode);
        kodeSurat.setAlignment(Paragraph.ALIGN_CENTER);
        kodeSurat.setFont(kodeFont);
        document.add(kodeSurat);

        Paragraph nLine = new Paragraph("\n");
        Image salam = Image.getInstance(logo + File.separator + "salam.png");
        salam.scaleToFit(140, 140);
        salam.setAbsolutePosition(85, PageSize.A4.getHeight() - (salam.getScaledHeight()) - 115);
        document.add(salam);
        document.add(nLine);

        paragrafkosong = new Paragraph("Dengan senantiasa mengharap rahmat dan ridha Allah, yang bertanda tangan di bawah ini :", fStandard);
        paragrafkosong.setAlignment(Element.ALIGN_BASELINE);
        paragrafkosong.setIndentationLeft(50);
        document.add(paragrafkosong);

        PdfPTable table = new PdfPTable(3);
        table.setWidthPercentage(70);
        table.setWidths(new int[] {3,1,15});
        table.setSpacingBefore(10);
        Font font = FontFactory.getFont(FontFactory.TIMES_ROMAN);
        font.setColor(CMYKColor.BLACK);

        PdfPCell cell;
        cell = new PdfPCell(new Phrase("Nama",font));
        cell.setBorder(Rectangle.NO_BORDER);
        cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
        table.addCell(cell);
        cell = new PdfPCell(new Phrase(":", font));
        cell.setBorder(Rectangle.NO_BORDER);
        cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
        table.addCell(cell);
        cell = new PdfPCell(new Phrase(sidang.getSeminar().getNote().getMahasiswa().getIdProdi().getDosen().getKaryawan().getNamaKaryawan(), font));
        cell.setBorder(Rectangle.NO_BORDER);
        cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
        table.addCell(cell);
        cell = new PdfPCell(new Phrase("NIDN",font));
        cell.setBorder(Rectangle.NO_BORDER);
        cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
        table.addCell(cell);
        cell = new PdfPCell(new Phrase(":", font));
        cell.setBorder(Rectangle.NO_BORDER);
        cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
        table.addCell(cell);
        cell = new PdfPCell(new Phrase(sidang.getSeminar().getNote().getMahasiswa().getIdProdi().getDosen().getKaryawan().getNidn(), font));
        cell.setBorder(Rectangle.NO_BORDER);
        cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
        table.addCell(cell);
        cell = new PdfPCell(new Phrase("Jabatan",font));
        cell.setBorder(Rectangle.NO_BORDER);
        cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
        table.addCell(cell);
        cell = new PdfPCell(new Phrase(":", font));
        cell.setBorder(Rectangle.NO_BORDER);
        cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
        table.addCell(cell);
        cell = new PdfPCell(new Phrase(sidang.getSeminar().getNote().getMahasiswa().getIdProdi().getJabatan(), font));
        cell.setBorder(Rectangle.NO_BORDER);
        cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
        table.addCell(cell);
        cell = new PdfPCell(new Phrase("Unit Kerja",font));
        cell.setBorder(Rectangle.NO_BORDER);
        cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
        table.addCell(cell);
        cell = new PdfPCell(new Phrase(":", font));
        cell.setBorder(Rectangle.NO_BORDER);
        cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
        table.addCell(cell);
        cell = new PdfPCell(new Phrase("Institut Agama Islam Tazkia", font));
        cell.setBorder(Rectangle.NO_BORDER);
        cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
        table.addCell(cell);

        document.add(table);

        paragrafkosong = new Paragraph("Memberi tugas kepada: ", fStandard);
        paragrafkosong.setAlignment(Element.ALIGN_BASELINE);
        paragrafkosong.setIndentationLeft(50);
        document.add(paragrafkosong);

        PdfPTable table1 = new PdfPTable(4);
        table1.setSpacingBefore(15);
        table1.setWidthPercentage(90);
        table1.setWidths(new int[] {3, 5, 8, 5});

        Font fontHeader = FontFactory.getFont(FontFactory.TIMES_BOLD, 12);
        Font fontIsi = FontFactory.getFont(FontFactory.TIMES_ROMAN, 12);

        cell = new PdfPCell(new Phrase("No. ", fontHeader));
        cell.setHorizontalAlignment(Element.ALIGN_CENTER);
        cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
        table1.addCell(cell);
        cell = new PdfPCell(new Phrase("NIDN ", fontHeader));
        cell.setHorizontalAlignment(Element.ALIGN_CENTER);
        cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
        table1.addCell(cell);
        cell = new PdfPCell(new Phrase("Nama ", fontHeader));
        cell.setHorizontalAlignment(Element.ALIGN_CENTER);
        cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
        table1.addCell(cell);
        cell = new PdfPCell(new Phrase("Sebagai ", fontHeader));
        cell.setHorizontalAlignment(Element.ALIGN_CENTER);
        cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
        table1.addCell(cell);
        cell = new PdfPCell(new Phrase("1 ", fontIsi));
        cell.setHorizontalAlignment(Element.ALIGN_CENTER);
        cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
        table1.addCell(cell);
        cell = new PdfPCell(new Phrase(sidang.getKetuaPenguji().getKaryawan().getNidn(), fontIsi));
        cell.setHorizontalAlignment(Element.ALIGN_CENTER);
        cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
        table1.addCell(cell);
        cell = new PdfPCell(new Phrase(sidang.getKetuaPenguji().getKaryawan().getNamaKaryawan(), fontIsi));
        cell.setHorizontalAlignment(Element.ALIGN_CENTER);
        cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
        table1.addCell(cell);
        cell = new PdfPCell(new Phrase("Ketua Penguji ", fontIsi));
        cell.setHorizontalAlignment(Element.ALIGN_CENTER);
        cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
        table1.addCell(cell);
        cell = new PdfPCell(new Phrase("2 ", fontIsi));
        cell.setHorizontalAlignment(Element.ALIGN_CENTER);
        cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
        table1.addCell(cell);
        cell = new PdfPCell(new Phrase(sidang.getDosenPenguji().getKaryawan().getNidn(), fontIsi));
        cell.setHorizontalAlignment(Element.ALIGN_CENTER);
        cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
        table1.addCell(cell);
        cell = new PdfPCell(new Phrase(sidang.getDosenPenguji().getKaryawan().getNamaKaryawan(), fontIsi));
        cell.setHorizontalAlignment(Element.ALIGN_CENTER);
        cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
        table1.addCell(cell);
        cell = new PdfPCell(new Phrase("Anggota Penguji ", fontIsi));
        cell.setHorizontalAlignment(Element.ALIGN_CENTER);
        cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
        table1.addCell(cell);
        cell = new PdfPCell(new Phrase("3 ", fontIsi));
        cell.setHorizontalAlignment(Element.ALIGN_CENTER);
        cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
        table1.addCell(cell);
        cell = new PdfPCell(new Phrase(sidang.getSeminar().getNote().getDosen().getKaryawan().getNidn(), fontIsi));
        cell.setHorizontalAlignment(Element.ALIGN_CENTER);
        cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
        table1.addCell(cell);
        cell = new PdfPCell(new Phrase(sidang.getSeminar().getNote().getDosen().getKaryawan().getNamaKaryawan(), fontIsi));
        cell.setHorizontalAlignment(Element.ALIGN_CENTER);
        cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
        table1.addCell(cell);
        cell = new PdfPCell(new Phrase("Pembimbing ", fontIsi));
        cell.setHorizontalAlignment(Element.ALIGN_CENTER);
        cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
        table1.addCell(cell);

        document.add(table1);

        paragrafkosong = new Paragraph();
        paragrafkosong.add(new Chunk("Untuk menguji ", fStandard));
        paragrafkosong.add(new Chunk("Sidang Seminar Proposal", fontB));
        paragrafkosong.add(new Chunk(" mahasiswa Program Studi ", fStandard));
        paragrafkosong.add(new Chunk(sidang.getSeminar().getNote().getMahasiswa().getIdProdi().getNamaProdi(), fStandard));
        paragrafkosong.add(new Chunk("Institut Agama Islam Tazkia ", fStandard));
        paragrafkosong.add(new Chunk(sidang.getSeminar().getTahunAkademik().getNamaTahunAkademik(), fStandard));
        paragrafkosong.add(new Chunk(" sebagai berikut:", fStandard));
        paragrafkosong.setAlignment(Element.ALIGN_BASELINE);
        paragrafkosong.setIndentationLeft(50);
        document.add(paragrafkosong);

        PdfPTable table2 = new PdfPTable(3);
        table2.setWidthPercentage(70);
        table2.setWidths(new int[] {3,1,15});
        table2.setSpacingBefore(10);
        Font font2 = FontFactory.getFont(FontFactory.TIMES_ROMAN);
        font2.setColor(CMYKColor.BLACK);

        cell = new PdfPCell(new Phrase("Nama",font2));
        cell.setBorder(Rectangle.NO_BORDER);
        cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
        cell.setHorizontalAlignment(Element.ALIGN_LEFT);
        table2.addCell(cell);
        cell = new PdfPCell(new Phrase(":", font2));
        cell.setBorder(Rectangle.NO_BORDER);
        cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
        cell.setHorizontalAlignment(Element.ALIGN_CENTER);
        table2.addCell(cell);
        cell = new PdfPCell(new Phrase(sidang.getSeminar().getNote().getMahasiswa().getNama(), font2));
        cell.setBorder(Rectangle.NO_BORDER);
        cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
        cell.setHorizontalAlignment(Element.ALIGN_LEFT);
        table2.addCell(cell);
        cell = new PdfPCell(new Phrase("NIM",font2));
        cell.setBorder(Rectangle.NO_BORDER);
        cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
        cell.setHorizontalAlignment(Element.ALIGN_LEFT);
        table2.addCell(cell);
        cell = new PdfPCell(new Phrase(":", font2));
        cell.setBorder(Rectangle.NO_BORDER);
        cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
        cell.setHorizontalAlignment(Element.ALIGN_CENTER);
        table2.addCell(cell);
        cell = new PdfPCell(new Phrase(sidang.getSeminar().getNote().getMahasiswa().getNim(), font2));
        cell.setBorder(Rectangle.NO_BORDER);
        cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
        cell.setHorizontalAlignment(Element.ALIGN_LEFT);
        table2.addCell(cell);
        cell = new PdfPCell(new Phrase("Judul",font2));
        cell.setBorder(Rectangle.NO_BORDER);
        cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
        cell.setHorizontalAlignment(Element.ALIGN_LEFT);
        table2.addCell(cell);
        cell = new PdfPCell(new Phrase(":", font2));
        cell.setBorder(Rectangle.NO_BORDER);
        cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
        cell.setHorizontalAlignment(Element.ALIGN_CENTER);
        table2.addCell(cell);
        cell = new PdfPCell(new Phrase(sidang.getJudulTugasAkhir(), font2));
        cell.setBorder(Rectangle.NO_BORDER);
        cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
        cell.setHorizontalAlignment(Element.ALIGN_LEFT);
        table2.addCell(cell);

        document.add(table2);

        paragrafkosong = new Paragraph("Yang dilaksanakan pada: ", fStandard);
        paragrafkosong.setAlignment(Element.ALIGN_BASELINE);
        paragrafkosong.setIndentationLeft(50);
        document.add(paragrafkosong);

        PdfPTable table3 = new PdfPTable(3);
        table3.setWidthPercentage(70);
        table3.setWidths(new int[] {4,1,15});
        table3.setSpacingBefore(10);
        Font font3 = FontFactory.getFont(FontFactory.TIMES_ROMAN, 12);
        font2.setColor(CMYKColor.BLACK);

        cell = new PdfPCell(new Phrase("Hari/Tanggal",font3));
        cell.setBorder(Rectangle.NO_BORDER);
        cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
        cell.setHorizontalAlignment(Element.ALIGN_LEFT);
        table3.addCell(cell);
        cell = new PdfPCell(new Phrase(":", font3));
        cell.setBorder(Rectangle.NO_BORDER);
        cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
        cell.setHorizontalAlignment(Element.ALIGN_CENTER);
        table3.addCell(cell);
        cell = new PdfPCell(new Phrase(hariDao.findById(String.valueOf(sidang.getTanggalUjian().getDayOfWeek().getValue())).get().getNamaHari() + ", " + sidang.getTanggalUjian().format(DateTimeFormatter.ofPattern("dd-MMM-yyyy")), font3));
        cell.setBorder(Rectangle.NO_BORDER);
        cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
        cell.setHorizontalAlignment(Element.ALIGN_LEFT);
        table3.addCell(cell);
        cell = new PdfPCell(new Phrase("Waktu",font));
        cell.setBorder(Rectangle.NO_BORDER);
        cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
        cell.setHorizontalAlignment(Element.ALIGN_LEFT);
        table3.addCell(cell);
        cell = new PdfPCell(new Phrase(":", font));
        cell.setBorder(Rectangle.NO_BORDER);
        cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
        cell.setHorizontalAlignment(Element.ALIGN_CENTER);
        table3.addCell(cell);
        Phrase w = new Phrase();
        w.add(new Chunk("Pkl ", font));
        w.add(new Chunk(sidang.getJamMulai().toString() + " - " + sidang.getJamSelesai().toString() + " WIB", fontB));
        cell = new PdfPCell(new Phrase(w));
        cell.setBorder(Rectangle.NO_BORDER);
        cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
        cell.setHorizontalAlignment(Element.ALIGN_LEFT);
        table3.addCell(cell);
        cell = new PdfPCell(new Phrase("Tempat",font));
        cell.setBorder(Rectangle.NO_BORDER);
        cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
        cell.setHorizontalAlignment(Element.ALIGN_LEFT);
        table3.addCell(cell);
        cell = new PdfPCell(new Phrase(":", font));
        cell.setBorder(Rectangle.NO_BORDER);
        cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
        cell.setHorizontalAlignment(Element.ALIGN_CENTER);
        table3.addCell(cell);
        cell = new PdfPCell(new Phrase(sidang.getRuangan().getNamaRuangan(), fontB));
        cell.setBorder(Rectangle.NO_BORDER);
        cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
        cell.setHorizontalAlignment(Element.ALIGN_LEFT);
        table3.addCell(cell);

        document.add(table3);

        paragrafkosong = new Paragraph("Demikian pernyataan ini dibuat untuk dapat dipergunakan sebagaimana mestinya.", fStandard);
        paragrafkosong.setAlignment(Element.ALIGN_BASELINE);
        paragrafkosong.setIndentationLeft(50);
        document.add(paragrafkosong);

        Image wassalam = Image.getInstance(logo + File.separator + "wassalam.png");
        wassalam.scaleToFit(140, 140);
        wassalam.setAbsolutePosition(85, PageSize.A4.getHeight() - (table1.getTotalHeight()) - (table2.getTotalHeight()) - 415);
        document.add(wassalam);

        document.add(nLine);
        document.add(nLine);
        paragrafkosong = new Paragraph();
        paragrafkosong.add(new Chunk("Bogor, ", fStandard));
        paragrafkosong.add(new Chunk(sidang.getTanggalUjian().format(DateTimeFormatter.ofPattern("dd MMM yyyy")) + " M"));
        paragrafkosong.add(new Chunk(" / ", fStandard));
        paragrafkosong.add(new Chunk(tanggalHijri+ " " + namaBulanHijri + " " + tahunHijri + " H", fStandard));
        paragrafkosong.setAlignment(Element.ALIGN_BASELINE);
        paragrafkosong.setIndentationLeft(50);
        document.add(paragrafkosong);

        Image ttd = Image.getInstance(tandaTangan + File.separator + sidang.getSeminar().getNote().getMahasiswa().getIdProdi().getDosen().getKaryawan().getTandaTangan());
        ttd.scaleToFit(100, 100);
        ttd.setAbsolutePosition(85, PageSize.A4.getHeight() - (table1.getTotalHeight()) - (table2.getTotalHeight()) - 520);
        document.add(ttd);

        document.add(nLine);
        document.add(nLine);
        document.add(nLine);
        document.add(nLine);

        Paragraph kpsName = new Paragraph(sidang.getSeminar().getNote().getMahasiswa().getIdProdi().getDosen().getKaryawan().getNamaKaryawan(), titleFont);
        kpsName.setAlignment(Paragraph.ALIGN_BASELINE);
        kpsName.setIndentationLeft(50);
        document.add(kpsName);

        Paragraph nidn = new Paragraph("NIDN: " + sidang.getSeminar().getNote().getMahasiswa().getIdProdi().getDosen().getKaryawan().getNidn());
        nidn.setAlignment(Paragraph.ALIGN_BASELINE);
        nidn.setIndentationLeft(50);
        nidn.setFont(kodeFont);
        document.add(nidn);

        Image footer = Image.getInstance(logo + File.separator + "footer.png");
        footer.scaleToFit(document.getPageSize().getWidth() - 20, 200);
        footer.setAbsolutePosition( 10, 10);
        document.add(footer);

        document.close();

    }

}
