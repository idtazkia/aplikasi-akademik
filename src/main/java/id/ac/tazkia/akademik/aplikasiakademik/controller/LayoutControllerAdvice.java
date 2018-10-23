package id.ac.tazkia.akademik.aplikasiakademik.controller;

import id.ac.tazkia.akademik.aplikasiakademik.dao.DosenDao;
import id.ac.tazkia.akademik.aplikasiakademik.dao.KaryawanDao;
import id.ac.tazkia.akademik.aplikasiakademik.dao.MahasiswaDao;
import id.ac.tazkia.akademik.aplikasiakademik.dao.UserDao;
import id.ac.tazkia.akademik.aplikasiakademik.entity.Dosen;
import id.ac.tazkia.akademik.aplikasiakademik.entity.Karyawan;
import id.ac.tazkia.akademik.aplikasiakademik.entity.Mahasiswa;
import id.ac.tazkia.akademik.aplikasiakademik.entity.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

import java.util.HashMap;
import java.util.Map;

@ControllerAdvice
public class LayoutControllerAdvice {

    private static final Logger LOGGER = LoggerFactory.getLogger(LayoutControllerAdvice.class);

    @Autowired private UserDao userDao;
    @Autowired private MahasiswaDao mahasiswaDao;

    @ModelAttribute("currentUser")
    public Map<String, Object> currentUser(Authentication auth){
        Map<String, Object> currentUser = new HashMap<>();

        if(auth != null) {
            LOGGER.debug("Authentication Object : {}", auth);
            User user = userDao.findByUsername(auth.getName());
            Mahasiswa mahasiswa = mahasiswaDao.findByUser(user);
            currentUser.put("mahasiswa", mahasiswa);
        }

        return currentUser;
    }
}
