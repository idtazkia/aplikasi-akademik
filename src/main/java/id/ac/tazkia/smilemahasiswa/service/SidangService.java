package id.ac.tazkia.smilemahasiswa.service;

import id.ac.tazkia.smilemahasiswa.dao.EnableFitureDao;
import id.ac.tazkia.smilemahasiswa.dao.SeminarDao;
import id.ac.tazkia.smilemahasiswa.dao.SidangDao;
import id.ac.tazkia.smilemahasiswa.dto.graduation.SeminarDto;
import id.ac.tazkia.smilemahasiswa.dto.graduation.SidangDto;
import id.ac.tazkia.smilemahasiswa.entity.*;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;

@Service
public class SidangService {
    @Autowired
    private SidangDao sidangDao;

    @Autowired
    private SeminarDao seminarDao;

    @Autowired
    private EnableFitureDao enableFitureDao;

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

        }

        seminar.setPublish(StatusRecord.AKTIF.toString());
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
        seminarDao.save(seminar);
        return seminar;
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

    public void savePenguji(SidangDto sidangDto){
        Sidang sidang = sidangDao.findById(sidangDto.getId()).get();
        sidang.setKomentarPenguji(sidangDto.getKomentar());
        sidang.setUa(sidangDto.getNilaiA());
        sidang.setUb(sidangDto.getNilaiB());
        sidang.setUc(sidangDto.getNilaiC());
        sidang.setUd(sidangDto.getNilaiD());
        sidangDao.save(sidang);
        akumulasiNilai(sidang);

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
}
