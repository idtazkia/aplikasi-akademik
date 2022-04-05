package id.ac.tazkia.smilemahasiswa.service;

import id.ac.tazkia.smilemahasiswa.dao.EnableFitureDao;
import id.ac.tazkia.smilemahasiswa.dao.JenjangDao;
import id.ac.tazkia.smilemahasiswa.dao.SeminarDao;
import id.ac.tazkia.smilemahasiswa.dao.SidangDao;
import id.ac.tazkia.smilemahasiswa.dto.graduation.SeminarDto;
import id.ac.tazkia.smilemahasiswa.dto.graduation.SemproDto;
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
    private JenjangDao jenjangDao;

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

        }

        /*seminar.setPublish(StatusRecord.AKTIF.toString());
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
        }*/
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
        akumulasiNilai(sidang);

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
}
