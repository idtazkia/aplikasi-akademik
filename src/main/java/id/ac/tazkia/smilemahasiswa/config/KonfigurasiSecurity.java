package id.ac.tazkia.smilemahasiswa.config;


import id.ac.tazkia.smilemahasiswa.dao.UserDao;
import id.ac.tazkia.smilemahasiswa.entity.Permission;
import id.ac.tazkia.smilemahasiswa.entity.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.authority.mapping.GrantedAuthoritiesMapper;
import org.springframework.security.oauth2.core.user.OAuth2UserAuthority;
import org.thymeleaf.extras.springsecurity5.dialect.SpringSecurityDialect;

import java.util.ArrayList;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@EnableWebSecurity
@EnableGlobalMethodSecurity(prePostEnabled = true)
public class KonfigurasiSecurity extends WebSecurityConfigurerAdapter {

    @Autowired
    private UserDao userDao;

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http
                .authorizeRequests()
                .antMatchers("/equipment/institution/list").hasAnyAuthority("VIEW_SUPERUSER")
                .antMatchers("/equipment/building/list").hasAnyAuthority("VIEW_SUPERUSER")
                .antMatchers("/equipment/building/form").hasAnyAuthority("VIEW_SUPERUSER")
                .antMatchers("/equipment/campus/list").hasAnyAuthority("VIEW_SUPERUSER")
                .antMatchers("/equipment/campus/form").hasAnyAuthority("VIEW_SUPERUSER")
                .antMatchers("/equipment/room/list").hasAnyAuthority("VIEW_SUPERUSER")
                .antMatchers("/equipment/room/form").hasAnyAuthority("VIEW_SUPERUSER")
                .antMatchers("/equipment/class/list").hasAnyAuthority("VIEW_SUPERUSER", "VIEW_AKADEMIK2", "VIEW_KPS")
                .antMatchers("/equipment/class/form").hasAnyAuthority("VIEW_SUPERUSER", "VIEW_AKADEMIK2")
                .antMatchers("/setting/program/list").hasAnyAuthority("VIEW_SUPERUSER", "VIEW_AKADEMIK2")
                .antMatchers("/setting/program/form").hasAnyAuthority("VIEW_SUPERUSER", "VIEW_AKADEMIK2")
                .antMatchers("/setting/beasiswa/list").hasAnyAuthority("VIEW_SUPERUSER", "VIEW_AKADEMIK2", "VIEW_KPS", "VIEW_FINANCE", "VIEW_WAREK2")
                .antMatchers("/setting/level/list").hasAnyAuthority("VIEW_SUPERUSER", "VIEW_AKADEMIK2")
                .antMatchers("/setting/level/form").hasAnyAuthority("VIEW_SUPERUSER", "VIEW_AKADEMIK2")
                .antMatchers("/setting/prody/list").hasAnyAuthority("VIEW_SUPERUSER", "VIEW_AKADEMIK2")
                .antMatchers("/setting/concentration/list").hasAnyAuthority("VIEW_SUPERUSER", "VIEW_AKADEMIK2")
                .antMatchers("/setting/mahasiswa/dosen_wali").hasAnyAuthority("VIEW_SUPERUSER", "VIEW_AKADEMIK2","VIEW_KPS")
                .antMatchers("/academic/year/list").hasAnyAuthority("VIEW_SUPERUSER", "VIEW_AKADEMIK1", "VIEW_AKADEMIK2")
                .antMatchers("/academic/year/form").hasAnyAuthority("VIEW_SUPERUSER", "VIEW_AKADEMIK1", "VIEW_AKADEMIK2")
                .antMatchers("/academic/curriculum/list").hasAnyAuthority("VIEW_SUPERUSER", "VIEW_AKADEMIK2", "VIEW_KPS")
                .antMatchers("/academic/curriculum/form").hasAnyAuthority("VIEW_SUPERUSER", "VIEW_AKADEMIK2", "VIEW_KPS")
                .antMatchers("/academic/curriculum/form").hasAnyAuthority("VIEW_SUPERUSER", "VIEW_AKADEMIK2", "VIEW_KPS")
                .antMatchers("/academic/courses/list").hasAnyAuthority("VIEW_SUPERUSER", "VIEW_AKADEMIK2", "VIEW_KPS")
                .antMatchers("/academic/courses/form").hasAnyAuthority("VIEW_SUPERUSER", "VIEW_AKADEMIK2", "VIEW_KPS")
                .antMatchers("/academic/courses/form").hasAnyAuthority("VIEW_SUPERUSER", "VIEW_AKADEMIK2", "VIEW_KPS")
                .antMatchers("/academic/curriculumCourses/list").hasAnyAuthority("VIEW_SUPERUSER", "VIEW_AKADEMIK2", "VIEW_KPS")
                .antMatchers("/academic/ploting/list").hasAnyAuthority("VIEW_SUPERUSER", "VIEW_AKADEMIK2", "VIEW_KPS")
                .antMatchers("/academic/schedule/room").hasAnyAuthority("VIEW_SUPERUSER", "VIEW_AKADEMIK2", "VIEW_KPS")
                .antMatchers("/academic/schedule/list").hasAnyAuthority("VIEW_SUPERUSER", "VIEW_AKADEMIK2", "VIEW_KPS", "VIEW_STAFF")
                .antMatchers("/academic/conversion/list").hasAnyAuthority("VIEW_SUPERUSER", "VIEW_AKADEMIK2", "VIEW_KPS")
                .antMatchers("/academic/prodi/list").hasAnyAuthority("VIEW_SUPERUSER", "VIEW_AKADEMIK2")
                .antMatchers("/mahasiswa/list").hasAnyAuthority("VIEW_SUPERUSER", "VIEW_AKADEMIK1", "VIEW_AKADEMIK2", "VIEW_STAFF", "VIEW_FINANCE")
                .antMatchers("/studiesActivity/krs/list").hasAnyAuthority("VIEW_SUPERUSER", "VIEW_KPS", "VIEW_WAREK2", "VIEW_FINANCE", "VIEW_AKADEMIK2", "VIEW_AKADEMIK1", "VIEW_STAFF")
                .antMatchers("/studiesActivity/krs/paket").hasAnyAuthority("VIEW_SUPERUSER", "VIEW_KPS", "VIEW_AKADEMIK2", "VIEW_AKADEMIK1")
                .antMatchers("/studiesActivity/attendance/list").hasAnyAuthority("VIEW_SUPERUSER", "VIEW_AKADEMIK2", "VIEW_AKADEMIK1")
                .antMatchers("/studiesActivity/attendance/listdosen").hasAnyAuthority("VIEW_KPS", "VIEW_WAREK2", "VIEW_DOSEN")
                .antMatchers("/studiesActivity/validation/list").hasAnyAuthority("VIEW_SUPERUSER", "VIEW_KPS", "VIEW_AKADEMIK2", "VIEW_AKADEMIK1")
                .antMatchers("/studiesActivity/assesment/list").hasAnyAuthority("VIEW_SUPERUSER", "VIEW_AKADEMIK2", "VIEW_AKADEMIK1")
                .antMatchers("/studiesActivity/assesment/listdosen").hasAnyAuthority("VIEW_KPS", "VIEW_DOSEN", "VIEW_WAREK2")
                .antMatchers("/studiesActivity/khs/list").hasAnyAuthority("VIEW_SUPERUSER", "VIEW_KPS", "VIEW_AKADEMIK2", "VIEW_AKADEMIK1", "VIEW_STAFF")
                .antMatchers("/studiesActivity/transcript/list").hasAnyAuthority("VIEW_SUPERUSER", "VIEW_KPS", "VIEW_AKADEMIK2", "VIEW_AKADEMIK1")
                .antMatchers("/studiesActivity/transcript/cetaktranscript").hasAnyAuthority("VIEW_SUPERUSER", "VIEW_AKADEMIK2", "VIEW_AKADEMIK1")
                .antMatchers("/activation/krs").hasAnyAuthority("VIEW_FINANCE", "VIEW_WAREK2")
                .antMatchers("/activation/rfid").hasAnyAuthority("VIEW_FINANCE")
                .antMatchers("/activation/kartu").hasAnyAuthority("VIEW_FINANCE")
                .antMatchers("/studentBill/requestPenangguhan/list").hasAnyAuthority("VIEW_FINANCE", "VIEW_WAREK2")
                .antMatchers("/studentBill/requestCicilan/list").hasAnyAuthority("VIEW_FINANCE", "VIEW_WAREK2")
                .antMatchers("/studentBill/billAdmin/list").hasAnyAuthority("VIEW_FINANCE", "VIEW_WAREK2")
                .antMatchers("/studentBill/billAdmin/generate").hasAnyAuthority("VIEW_FINANCE", "VIEW_WAREK2")
                .antMatchers("/studentBill/typeBill/list").hasAnyAuthority("VIEW_FINANCE", "VIEW_WAREK2")
                .antMatchers("/studentBill/valueType/list").hasAnyAuthority("VIEW_FINANCE", "VIEW_WAREK2")
                .antMatchers("/studentBill/jenisDiskon/list").hasAnyAuthority("VIEW_FINANCE", "VIEW_WAREK2")
                .antMatchers("/studentBill/payment/report").hasAnyAuthority("VIEW_FINANCE", "VIEW_WAREK2")
                .antMatchers("/graduation/admin/score").hasAnyAuthority("VIEW_SUPERUSER", "VIEW_KPS", "VIEW_AKADEMIK1")
                .antMatchers("/graduation/admin/list").hasAnyAuthority("VIEW_SUPERUSER", "VIEW_KPS", "VIEW_AKADEMIK1")
                .antMatchers("/graduation/seminar/list").hasAnyAuthority("VIEW_SUPERUSER", "VIEW_KPS", "VIEW_AKADEMIK1")
                .antMatchers("/graduation/lecture/list").hasAnyAuthority("VIEW_DOSEN", "VIEW_KPS", "VIEW_WAREK2")
                .antMatchers("/graduation/lecture/sempro").hasAnyAuthority("VIEW_DOSEN", "VIEW_KPS", "VIEW_WAREK2")
                .antMatchers("/report/recapitulation/lecturer").hasAnyAuthority("VIEW_SUPERUSER", "VIEW_AKADEMIK1", "VIEW_AKADEMIK2")
                .antMatchers("/report/recapitulation/bkd").hasAnyAuthority("VIEW_DOSEN", "VIEW_WAREK2", "VIEW_KPS")
                .antMatchers("/report/recapitulation/edom").hasAnyAuthority("VIEW_SUPERUSER", "VIEW_AKADEMIK1","VIEW_AKADEMIK2", "VIEW_KPS")
                .antMatchers("/report/recapitulation/ipk").hasAnyAuthority("VIEW_SUPERUSER", "VIEW_AKADEMIK1","VIEW_AKADEMIK2", "VIEW_STAFF")
                .antMatchers("/report/historymahasiswa").hasAnyAuthority("VIEW_SUPERUSER", "VIEW_AKADEMIK1","VIEW_AKADEMIK2", "VIEW_STAFF", "VIEW_KPS")
                .antMatchers("/report/cuti").hasAnyAuthority("VIEW_SUPERUSER", "VIEW_AKADEMIK1","VIEW_AKADEMIK2", "VIEW_STAFF", "VIEW_KPS")
                .anyRequest().authenticated()
                .and().logout().permitAll()
                .and().oauth2Login().loginPage("/login").permitAll()
                .userInfoEndpoint()
                .userAuthoritiesMapper(authoritiesMapper())
                .and().defaultSuccessUrl("/dashboard",true);
    }

    private GrantedAuthoritiesMapper authoritiesMapper(){
        return (authorities) -> {
            String emailAttrName = "email";
            String email = authorities.stream()
                    .filter(OAuth2UserAuthority.class::isInstance)
                    .map(OAuth2UserAuthority.class::cast)
                    .filter(userAuthority -> userAuthority.getAttributes().containsKey(emailAttrName))
                    .map(userAuthority -> userAuthority.getAttributes().get(emailAttrName).toString())
                    .findFirst()
                    .orElse(null);

            if (email == null) {
                throw new IllegalStateException("Data email "+email+" tidak ada di google");
//                return authorities;     // data email tidak ada di userInfo dari Google
            }

            User user = userDao.findByUsername(email);
            if(user == null) {
                throw new IllegalStateException("Email "+email+" tidak ada dalam database");
//                return null;
//                return authorities;     // email user ini belum terdaftar di database
            }

            Set<Permission> userAuthorities = user.getRole().getPermissions();
            if (userAuthorities.isEmpty()) {
                return authorities;     // authorities defaultnya ROLE_USER
            }

            return Stream.concat(
                    authorities.stream(),
                    userAuthorities.stream()
                            .map(Permission::getValue)
                            .map(SimpleGrantedAuthority::new)
            ).collect(Collectors.toCollection(ArrayList::new));
        };
    }

    @Override
    public void configure(WebSecurity web) throws Exception {
        web.ignoring()
                .antMatchers("/login")
                .antMatchers("/css/**")
                .antMatchers("/api/jenjang")
                .antMatchers("/api/tarikData")
                .antMatchers("/api/cekpresensi")
                .antMatchers("/api/cekpresensi/mahasiswa")
                .antMatchers("/api/getruangan")
                .antMatchers("/api/getrfid")
                .antMatchers("/api/rfidkaryawan")
                .antMatchers("/api/presensimahasiswa")
                .antMatchers("/api/uploadMesin")
                .antMatchers("/api/inputpresensi")
                .antMatchers("/api/deleteMesin")
                .antMatchers("/api/getjadwal")
                .antMatchers("/api/getdataMahasiswa")
                .antMatchers("/api/akademikAktif")
                .antMatchers("/404")
                .antMatchers("/uploaded/{note}/bukti/")
                .antMatchers("/images/**");



    }



    @Bean
    public SpringSecurityDialect springSecurityDialect() {
        return new SpringSecurityDialect();
    }
}