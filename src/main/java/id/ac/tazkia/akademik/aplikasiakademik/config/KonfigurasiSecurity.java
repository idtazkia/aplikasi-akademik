package id.ac.tazkia.akademik.aplikasiakademik.config;

import id.ac.tazkia.akademik.aplikasiakademik.dao.UserDao;
import id.ac.tazkia.akademik.aplikasiakademik.entity.Permission;
import id.ac.tazkia.akademik.aplikasiakademik.entity.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
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

    @Autowired private UserDao userDao;

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http
                .authorizeRequests()
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
                .antMatchers("/api/akademikAktif")
                .antMatchers("/404")
                .antMatchers("/images/**");



    }


//    @Configuration
//    @Order(1)
//    public static class ApiWebSecurityConfigurationAdapter extends WebSecurityConfigurerAdapter {
//        protected void configure(HttpSecurity http) throws Exception {
//            http
//                    .antMatcher("/api/**")
//                    .authorizeRequests()
//                    .anyRequest().hasAnyAuthority("VIEW_API")
//                    .and()
//                    .httpBasic();
//        }
//    }

    @Bean
    public SpringSecurityDialect springSecurityDialect() {
        return new SpringSecurityDialect();
    }
}