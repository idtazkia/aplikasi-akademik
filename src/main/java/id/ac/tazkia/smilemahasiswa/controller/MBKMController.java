package id.ac.tazkia.smilemahasiswa.controller;

import id.ac.tazkia.smilemahasiswa.dao.DosenDao;
import id.ac.tazkia.smilemahasiswa.dao.KaryawanDao;
import id.ac.tazkia.smilemahasiswa.dao.PendaftaranMbkmDao;
import id.ac.tazkia.smilemahasiswa.dao.TahunAkademikDao;
import id.ac.tazkia.smilemahasiswa.entity.*;
import id.ac.tazkia.smilemahasiswa.service.CurrentUserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@Controller
@Slf4j
public class MBKMController {

    @Autowired private PendaftaranMbkmDao pendaftaranMbkmDao;

    @Autowired private CurrentUserService currentUserService;

    @Autowired private KaryawanDao karyawanDao;

    @Autowired private DosenDao dosenDao;

    @Autowired private TahunAkademikDao tahunAkademikDao;

    @PostMapping("/mbkm/approved")
    public String approvedMBKM(@RequestParam PendaftaranMbkm mbkm){

        mbkm.setStatusApproval(StatusApprove.APPROVED);
        pendaftaranMbkmDao.save(mbkm);

        return "redirect:/studiesActivity/krs/approval/list";
    }

    @PostMapping("/mbkm/reject")
    public String rejectMBKM(@RequestParam PendaftaranMbkm mbkm){

        mbkm.setStatusApproval(StatusApprove.REJECTED);
        pendaftaranMbkmDao.save(mbkm);

        return "redirect:/studiesActivity/krs/approval/list";
    }

}
