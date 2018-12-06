package id.ac.tazkia.akademik.aplikasiakademik.dao;

import id.ac.tazkia.akademik.aplikasiakademik.entity.Prodi;
import id.ac.tazkia.akademik.aplikasiakademik.entity.Program;
import id.ac.tazkia.akademik.aplikasiakademik.entity.StatusRecord;
import id.ac.tazkia.akademik.aplikasiakademik.entity.TahunAkademik;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest
public class TahunAkademikDaoTests {
    @Autowired private TahunAkademikDao tahunAkademikDao;
    @Autowired private ProdiDao prodiDao;
    @Autowired private ProgramDao programDao;

    @Test @Ignore
    public void testCariByProdiDanProgram(){
        Prodi prodi = prodiDao.findById("6ef2895b-10b1-472c-8608-e94564f073a0").get();
        Assert.assertNotNull(prodi);

        Program program = programDao.findById("09bdf769-efdc-4b3b-9af3-6884d158a7ac").get();

    }
}
