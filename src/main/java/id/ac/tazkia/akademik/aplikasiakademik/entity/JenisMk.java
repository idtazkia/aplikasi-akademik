package id.ac.tazkia.akademik.aplikasiakademik.entity;

import lombok.Data;

import javax.persistence.Entity;
import javax.persistence.Table;

@Data
@Entity
@Table(name="tb_jenisMk")
public class JenisMk {

    private String idJenismk;

    private String jenisMk;

    private String status;

}
