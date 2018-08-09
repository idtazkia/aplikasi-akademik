package id.ac.tazkia.akademik.aplikasiakademik.service;

import id.ac.tazkia.akademik.aplikasiakademik.dao.RoleDao;
import id.ac.tazkia.akademik.aplikasiakademik.dao.UserDao;
import id.ac.tazkia.akademik.aplikasiakademik.dao.UserPasswordDao;
import id.ac.tazkia.akademik.aplikasiakademik.entity.Role;
import id.ac.tazkia.akademik.aplikasiakademik.entity.RunningNumber;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;



@Service
@Transactional
public class RegistrasiService {

    private static final Logger LOGGER = LoggerFactory.getLogger(RegistrasiService.class);

    @Autowired
    private RunningNumberService runningNumberService;
    private RoleDao roleDao;
    @Autowired
    private UserDao userDao;
    @Autowired
    private UserPasswordDao userPasswordDao;
    @Autowired
    private PasswordEncoder passwordEncoder;

    public String generateNomorRegistrasi(String formatNim){
        RunningNumber terbaru = runningNumberService.generate(formatNim);

        LOGGER.debug("Format NIM : {}", formatNim);
        LOGGER.debug("Nomer Terakhir : {}", terbaru.getNomerTerakhir());

        String nomorRegistrasi = formatNim + String.format("%03d", terbaru.getNomerTerakhir());
        LOGGER.debug("Nomor Registrasi : {}", nomorRegistrasi);

        return nomorRegistrasi;
    }

}
