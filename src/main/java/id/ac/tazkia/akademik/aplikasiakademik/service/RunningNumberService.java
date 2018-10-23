package id.ac.tazkia.akademik.aplikasiakademik.service;

import id.ac.tazkia.akademik.aplikasiakademik.dao.RunningNumberDao;
import id.ac.tazkia.akademik.aplikasiakademik.entity.RunningNumber;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
public class RunningNumberService {

    @Autowired
    private RunningNumberDao runningNumberDao;

    private final Logger logger = LoggerFactory.getLogger(getClass());

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public RunningNumber generate(String nama){
        RunningNumber rn = runningNumberDao.findByNama(nama);
        if(rn == null){
            rn = new RunningNumber();
            rn.setNama(nama);
            rn.setNomerTerakhir(0L);
        }
        logger.info("Angka lama : {}", rn.getNomerTerakhir());
        rn.setNomerTerakhir(rn.getNomerTerakhir() + 1);
        runningNumberDao.save(rn);
        logger.info("Angka baru : {}",rn.getNomerTerakhir());

        return rn;
    }
}