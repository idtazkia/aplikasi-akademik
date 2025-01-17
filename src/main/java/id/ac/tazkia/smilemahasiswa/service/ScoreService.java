package id.ac.tazkia.smilemahasiswa.service;

import id.ac.tazkia.smilemahasiswa.dao.BobotTugasDao;
import id.ac.tazkia.smilemahasiswa.dao.GradeDao;
import id.ac.tazkia.smilemahasiswa.dao.KrsDetailDao;
import id.ac.tazkia.smilemahasiswa.dao.NilaiTugasDao;
import id.ac.tazkia.smilemahasiswa.dto.assesment.ScoreDto;
import id.ac.tazkia.smilemahasiswa.dto.assesment.ScoreInput;
import id.ac.tazkia.smilemahasiswa.entity.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class ScoreService {

    @Autowired
    private KrsDetailDao krsDetailDao;
    @Autowired
    private GradeDao gradeDao;
    @Autowired
    private BobotTugasDao bobotTugasDao;
    @Autowired
    private NilaiTugasDao nilaiTugasDao;

    public void reCalculatingNilai(Jadwal jadwal){

        try {
            nilaiTugasDao.updateNilaiTerbobot(jadwal);
            nilaiTugasDao.updateNilaiTotalKrsDetail(jadwal);
        }catch (Exception e){
            throw new RuntimeException("Error meng-calculating nilai. ", e);
        }

    }

    public void hitungNilaiTugas(NilaiTugas nilaiTugas) {
        List<NilaiTugas> semuaNilaiTugas = nilaiTugasDao.findByStatusAndKrsDetailAndBobotTugasStatusAndKategoriTugas(StatusRecord.AKTIF, nilaiTugas.getKrsDetail(), StatusRecord.AKTIF, KategoriTugas.TUGAS);
        BigDecimal sumNilaiTugas = semuaNilaiTugas.stream().map(NilaiTugas::getNilaiAkhir).reduce(BigDecimal.ZERO, BigDecimal::add);

        KrsDetail krsDetail = krsDetailDao.findById(nilaiTugas.getKrsDetail().getId()).get();
        krsDetail.setNilaiTugas(sumNilaiTugas);
//        krsDetail.setNilaiAkhir(sumNilaiTugas.add(krsDetail.getNilaiPresensi()).add(krsDetail.getNilaiUts()).add(krsDetail.getNilaiUas()));
        krsDetailDao.save(krsDetail);

//        hitungNilaiAkhir(krsDetail);
    }

    public void hitungNilaiUTS(NilaiTugas nilaiTugas) {
        List<NilaiTugas> semuaNilaiUts = nilaiTugasDao.findByStatusAndKrsDetailAndBobotTugasStatusAndKategoriTugas(StatusRecord.AKTIF, nilaiTugas.getKrsDetail(), StatusRecord.AKTIF, KategoriTugas.UTS);
        BigDecimal sumNilaiUtsAkhir = semuaNilaiUts.stream().map(NilaiTugas::getNilaiAkhir).reduce(BigDecimal.ZERO, BigDecimal::add);

        Optional<KrsDetail> kd = krsDetailDao.findById(nilaiTugas.getKrsDetail().getId());
        if (kd.isPresent()){
            KrsDetail krsDetail = kd.get();
            krsDetail.setNilaiUts(sumNilaiUtsAkhir);
//            krsDetail.setNilaiAkhir(sumNilaiUtsAkhir.add(krsDetail.getNilaiTugas()).add(krsDetail.getNilaiPresensi()).add(krsDetail.getNilaiUas()));
            krsDetailDao.save(krsDetail);

//            hitungNilaiAkhir(krsDetail);
        }
    }

    public void hitungNilaiUAS(NilaiTugas nilaiTugas) {
        List<NilaiTugas> semuaNilaiUas = nilaiTugasDao.findByStatusAndKrsDetailAndBobotTugasStatusAndKategoriTugas(StatusRecord.AKTIF, nilaiTugas.getKrsDetail(), StatusRecord.AKTIF, KategoriTugas.UAS);
        BigDecimal sumUas = semuaNilaiUas.stream().map(NilaiTugas::getNilaiAkhir).reduce(BigDecimal.ZERO, BigDecimal::add);

        KrsDetail krsDetail = krsDetailDao.findById(nilaiTugas.getKrsDetail().getId()).get();
        krsDetail.setNilaiUas(sumUas);
        krsDetail.setNilaiAkhir(sumUas.add(krsDetail.getNilaiPresensi()).add(krsDetail.getNilaiTugas()).add(krsDetail.getNilaiUts()));
        krsDetailDao.save(krsDetail);

        hitungNilaiAkhir(krsDetail);
    }

    public KrsDetail hitungKomponenNilai(ScoreInput scoreInput) {
        KrsDetail krsDetail = krsDetailDao.findById(scoreInput.getKrsDetail()).get();
        switch (scoreInput.getType()) {
            case "uts":
                BigDecimal nilaiUtsInput = scoreInput.getNilai().multiply(krsDetail.getJadwal().getBobotUts()).divide(new BigDecimal(100));
                BigDecimal nilaiUasInput = krsDetail.getNilaiUas().multiply(krsDetail.getJadwal().getBobotUas()).divide(new BigDecimal(100));
                krsDetail.setNilaiPresensi(scoreInput.getAbsensi());
                krsDetail.setNilaiUts(scoreInput.getNilai());
                krsDetail.setNilaiAkhir(krsDetail.getNilaiTugas().add(nilaiUtsInput).add(krsDetail.getNilaiPresensi()).add(nilaiUasInput));

                hitungNilaiAkhir(krsDetail);
                break;
            case "uas":
                BigDecimal nilaiUas = scoreInput.getNilai().multiply(krsDetail.getJadwal().getBobotUas()).divide(new BigDecimal(100));
                BigDecimal nilaiUts = krsDetail.getNilaiUts().multiply(krsDetail.getJadwal().getBobotUts()).divide(new BigDecimal(100));
                krsDetail.setNilaiUas(scoreInput.getNilai());
                krsDetail.setNilaiAkhir(krsDetail.getNilaiTugas().add(nilaiUts).add(krsDetail.getNilaiPresensi()).add(nilaiUas));
                krsDetail.setNilaiPresensi(scoreInput.getAbsensi());

                hitungNilaiAkhir(krsDetail);
                break;
            default:
                BobotTugas bobotTugas = bobotTugasDao.findById(scoreInput.getTugas()).get();
                NilaiTugas validasi = nilaiTugasDao.findByStatusAndBobotTugasAndKrsDetail(
                        StatusRecord.AKTIF,
                        bobotTugas, krsDetail);
                BigDecimal nilai = bobotTugas.getBobot()
                        .multiply(scoreInput.getNilai()
                                .divide(new BigDecimal(100)));
                validasi.setNilai(scoreInput.getNilai());
                validasi.setNilaiAkhir(nilai);
                nilaiTugasDao.save(validasi);
                List<NilaiTugas> nilaiAkhir = nilaiTugasDao
                        .findByStatusAndKrsDetailAndBobotTugasStatus(
                                StatusRecord.AKTIF, krsDetail,
                                StatusRecord.AKTIF);
                BigDecimal sum = nilaiAkhir.stream()
                        .map(NilaiTugas::getNilaiAkhir)
                        .reduce(BigDecimal.ZERO, BigDecimal::add);
                KrsDetail kd = krsDetailDao.findById(validasi.getKrsDetail().getId()).get();

                BigDecimal nilaiUasTugas = krsDetail.getNilaiUas()
                        .multiply(krsDetail.getJadwal().getBobotUas())
                        .divide(new BigDecimal(100));
                BigDecimal nilaiUtsTugas = krsDetail.getNilaiUts()
                        .multiply(krsDetail.getJadwal().getBobotUts())
                        .divide(new BigDecimal(100));

                kd.setNilaiPresensi(scoreInput.getAbsensi());
                kd.setNilaiTugas(sum);
                kd.setNilaiAkhir(kd.getNilaiTugas().add(nilaiUtsTugas).add(kd.getNilaiPresensi())
                        .add(nilaiUasTugas));
                hitungNilaiAkhir(kd);
        }

        return krsDetail;
    }


    public void hitungNilaiAkhir(KrsDetail krsDetail) {

        Grade a = gradeDao.findById("1").get();
        Grade amin = gradeDao.findById("2").get();
        Grade bplus = gradeDao.findById("3").get();
        Grade b = gradeDao.findById("4").get();
        Grade bmin = gradeDao.findById("5").get();
        Grade cplus = gradeDao.findById("6").get();
        Grade c = gradeDao.findById("7").get();
        Grade d = gradeDao.findById("8").get();
        Grade e = gradeDao.findById("9").get();


        if (krsDetail.getNilaiAkhir().toBigInteger().intValue() >= 80 && krsDetail.getNilaiAkhir().toBigInteger().intValue() < 85) {
            System.out.println("a-");
            krsDetail.setGrade(amin.getNama());
            krsDetail.setBobot(amin.getBobot());
        }

        if (krsDetail.getNilaiAkhir().toBigInteger().intValue() >= 75 && krsDetail.getNilaiAkhir().toBigInteger().intValue() < 80) {
            System.out.println("b+");
            krsDetail.setGrade(bplus.getNama());
            krsDetail.setBobot(bplus.getBobot());

        }

        if (krsDetail.getNilaiAkhir().toBigInteger().intValue() >= 70 && krsDetail.getNilaiAkhir().toBigInteger().intValue() < 75) {
            System.out.println("b");
            krsDetail.setGrade(b.getNama());
            krsDetail.setBobot(b.getBobot());
        }

        if (krsDetail.getNilaiAkhir().toBigInteger().intValue() >= 65 && krsDetail.getNilaiAkhir().toBigInteger().intValue() < 70) {
            System.out.println("b-");
            krsDetail.setGrade(bmin.getNama());
            krsDetail.setBobot(bmin.getBobot());
        }

        if (krsDetail.getNilaiAkhir().toBigInteger().intValue() >= 60 && krsDetail.getNilaiAkhir().toBigInteger().intValue() < 65) {
            System.out.println("c+");
            krsDetail.setGrade(cplus.getNama());
            krsDetail.setBobot(cplus.getBobot());
        }

        if (krsDetail.getNilaiAkhir().toBigInteger().intValue() >= 55 && krsDetail.getNilaiAkhir().toBigInteger().intValue() < 60) {
            System.out.println("c");
            krsDetail.setGrade(c.getNama());
            krsDetail.setBobot(c.getBobot());
        }

        if (krsDetail.getNilaiAkhir().toBigInteger().intValue() >= 50 && krsDetail.getNilaiAkhir().toBigInteger().intValue() < 55) {
            System.out.println("d");
            krsDetail.setGrade(d.getNama());
            krsDetail.setBobot(d.getBobot());
        }

        if (krsDetail.getNilaiAkhir().toBigInteger().intValue() >= 0 && krsDetail.getNilaiAkhir().toBigInteger().intValue() < 50) {
            System.out.println("e");
            krsDetail.setGrade(e.getNama());
            krsDetail.setBobot(e.getBobot());
        }

        if (krsDetail.getNilaiAkhir().toBigInteger().intValue() >= 85) {
            System.out.println("a");
            krsDetail.setGrade(a.getNama());
            krsDetail.setBobot(a.getBobot());
        }
        krsDetailDao.save(krsDetail);
    }
}
