package id.ac.tazkia.smilemahasiswa.controller;

import id.ac.tazkia.smilemahasiswa.dao.BimbinganDao;
import id.ac.tazkia.smilemahasiswa.dao.DosenDao;
import id.ac.tazkia.smilemahasiswa.dao.NoteDao;
import id.ac.tazkia.smilemahasiswa.dao.UserDao;
import id.ac.tazkia.smilemahasiswa.entity.*;
import id.ac.tazkia.smilemahasiswa.service.CurrentUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import javax.validation.Valid;
import java.util.List;

@Controller
public class BimbinganController {

    @Autowired private UserDao userDao;

    @Autowired private CurrentUserService currentUserService;

    @Autowired private DosenDao dosenDao;

    @Autowired private NoteDao noteDao;

    @Autowired private BimbinganDao bimbinganDao;

    @GetMapping("/graduation/bimbingan/dosen/listBimbingan")
    public void listBimbingan(Model model, Authentication authentication){

        User user = currentUserService.currentUser(authentication);
        Dosen dosen = dosenDao.findByKaryawanIdUser(user);

        List<Note> cariNote = noteDao.findByDosenAndStatusAndMahasiswaIdProdiIdJenjangIdOrderByTanggalInputDesc(dosen, StatusApprove.APPROVED, "01");
        List<Note> cariNotePasca = noteDao.cariDosenPembimbingPasca(dosen);

        model.addAttribute("list", cariNote);
        model.addAttribute("listPasca", cariNotePasca);

    }

    @GetMapping("/graduation/bimbingan/dosen/detail")
    public void detailBimbingan(Model model, @RequestParam Note note){

        model.addAttribute("listBimbingan", bimbinganDao.findByMahasiswaAndStatus(note.getMahasiswa(), StatusRecord.AKTIF));
        model.addAttribute("note", note);
    }

    @PostMapping("/graduation/bimbingan/input")
    public String inputBimbingan(@Valid @ModelAttribute Bimbingan bimbingan, @RequestParam Note note){

        return "redirect:dosen/detail?note="+note.getId();
    }
}
