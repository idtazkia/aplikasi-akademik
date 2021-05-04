package id.ac.tazkia.smilemahasiswa.service;

import id.ac.tazkia.smilemahasiswa.dao.SidangDao;
import id.ac.tazkia.smilemahasiswa.dto.graduation.SidangDto;
import id.ac.tazkia.smilemahasiswa.entity.Sidang;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;

@Service
public class SidangService {
    @Autowired
    private SidangDao sidangDao;

    public SidangDto getKetua(Sidang sidang){
        SidangDto sidangDto = new SidangDto();
        sidangDto.setId(sidang.getId());
        sidangDto.setKomentar(sidang.getKomentarKetua());
        sidangDto.setBeritaAcara(sidang.getBeritaAcara());
        sidangDto.setNilaiA(sidang.getKa());
        sidangDto.setNilaiB(sidang.getKa());
        sidangDto.setNilaiC(sidang.getKa());
        sidangDto.setNilaiD(sidang.getKd());

        return sidangDto;
    }

    public SidangDto getPenguji(Sidang sidang){
        SidangDto sidangDto = new SidangDto();
        sidangDto.setId(sidang.getId());
        sidangDto.setKomentar(sidang.getKomentarPenguji());
        sidangDto.setNilaiA(sidang.getUa());
        sidangDto.setNilaiB(sidang.getUa());
        sidangDto.setNilaiC(sidang.getUa());
        sidangDto.setNilaiD(sidang.getUd());

        return sidangDto;
    }

    public SidangDto getPembimbing(Sidang sidang){
        SidangDto sidangDto = new SidangDto();
        sidangDto.setId(sidang.getId());
        sidangDto.setKomentar(sidang.getKomentarPembimbing());
        sidangDto.setNilaiA(sidang.getPa());
        sidangDto.setNilaiB(sidang.getPa());
        sidangDto.setNilaiC(sidang.getPa());
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
