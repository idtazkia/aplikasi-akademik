package id.ac.tazkia.akademik.aplikasiakademik.entity;

import lombok.Data;

import javax.persistence.Entity;
import javax.persistence.Table;

@Data
@Entity
@Table(name="tb_pilihan")
public class Pilihan {

    private String idPilihan;

    private String namaPilihan;

    private String status;
}
