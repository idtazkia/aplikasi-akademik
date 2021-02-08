package id.ac.tazkia.smilemahasiswa.service;

import id.ac.tazkia.smilemahasiswa.dao.GradeDao;
import id.ac.tazkia.smilemahasiswa.dao.KrsDetailDao;
import id.ac.tazkia.smilemahasiswa.entity.Grade;
import id.ac.tazkia.smilemahasiswa.entity.KrsDetail;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service @Transactional
public class ScoreService {

    @Autowired private KrsDetailDao krsDetailDao;
    @Autowired private GradeDao gradeDao;

    public void hitungNilaiAkhir(KrsDetail krsDetail) {

        Grade a = gradeDao.findById("1").get();
        Grade amin= gradeDao.findById("2").get();
        Grade bplus= gradeDao.findById("3").get();
        Grade b = gradeDao.findById("4").get();
        Grade bmin = gradeDao.findById("5").get();
        Grade cplus= gradeDao.findById("6").get();
        Grade c = gradeDao.findById("7").get();
        Grade d = gradeDao.findById("8").get();
        Grade e = gradeDao.findById("9").get();


        if (krsDetail.getNilaiAkhir().toBigInteger().intValue() >= 80 && krsDetail.getNilaiAkhir().toBigInteger().intValue() < 85){
            System.out.println("a-");
            krsDetail.setGrade(amin.getNama());
            krsDetail.setBobot(amin.getBobot());
        }

        if (krsDetail.getNilaiAkhir().toBigInteger().intValue() >= 75 && krsDetail.getNilaiAkhir().toBigInteger().intValue() < 80){
            System.out.println("b+");
            krsDetail.setGrade(bplus.getNama());
            krsDetail.setBobot(bplus.getBobot());

        }

        if (krsDetail.getNilaiAkhir().toBigInteger().intValue() >= 70 && krsDetail.getNilaiAkhir().toBigInteger().intValue() < 75){
            System.out.println("b");
            krsDetail.setGrade(b.getNama());
            krsDetail.setBobot(b.getBobot());
        }

        if (krsDetail.getNilaiAkhir().toBigInteger().intValue() >= 65 && krsDetail.getNilaiAkhir().toBigInteger().intValue() < 70){
            System.out.println("b-");
            krsDetail.setGrade(bmin.getNama());
            krsDetail.setBobot(bmin.getBobot());
        }

        if (krsDetail.getNilaiAkhir().toBigInteger().intValue() >= 60 && krsDetail.getNilaiAkhir().toBigInteger().intValue() < 65){
            System.out.println("c+");
            krsDetail.setGrade(cplus.getNama());
            krsDetail.setBobot(cplus.getBobot());
        }

        if (krsDetail.getNilaiAkhir().toBigInteger().intValue() >= 55 && krsDetail.getNilaiAkhir().toBigInteger().intValue() < 60){
            System.out.println("c");
            krsDetail.setGrade(c.getNama());
            krsDetail.setBobot(c.getBobot());
        }

        if (krsDetail.getNilaiAkhir().toBigInteger().intValue() >= 50 && krsDetail.getNilaiAkhir().toBigInteger().intValue() < 55){
            System.out.println("d");
            krsDetail.setGrade(d.getNama());
            krsDetail.setBobot(d.getBobot());
        }

        if (krsDetail.getNilaiAkhir().toBigInteger().intValue() >= 0 && krsDetail.getNilaiAkhir().toBigInteger().intValue() < 50){
            System.out.println("e");
            krsDetail.setGrade(e.getNama());
            krsDetail.setBobot(e.getBobot());
        }

        if (krsDetail.getNilaiAkhir().toBigInteger().intValue() >= 85){
            System.out.println("a");
            krsDetail.setGrade(a.getNama());
            krsDetail.setBobot(a.getBobot());
        }
        krsDetailDao.save(krsDetail);
    }
}
