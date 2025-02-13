package id.ac.tazkia.smilemahasiswa.entity;

import com.fasterxml.jackson.annotation.JsonBackReference;
import id.ac.tazkia.smilemahasiswa.constant.Approval;
import lombok.Data;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.Where;

import javax.persistence.*;

@Entity
@Data
@Where(clause = "status not in ('HAPUS', 'REJECTED')")
@SQLDelete(sql = "UPDATE detail_pendaftaran_konversi SET status = 'HAPUS' WHERE id=?")
public class DetailPendaftaranKonversi extends Auditable{
  @Id
  @GeneratedValue(generator = "uuid" )
  @GenericGenerator(name = "uuid", strategy = "uuid2")
  private String id;

  @ManyToOne
  @JoinColumn(name = "id_pendaftaran")
  @JsonBackReference
  private PendaftaranRequestKonversi pendaftaranRequestKonversi;

  @ManyToOne
  @JoinColumn(name = "id_krs_detail")
  private KrsDetail krsDetail;

  @Lob
  @Column(columnDefinition = "LONGTEXT")
  private String keterangan;

  private String matakuliah;

  private String gradeLama;

  private String gradeBaru;

  @Enumerated(EnumType.STRING)
  private Approval status = Approval.WAITING;
}
