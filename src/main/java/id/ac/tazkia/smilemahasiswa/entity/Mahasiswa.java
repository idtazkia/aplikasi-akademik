package id.ac.tazkia.smilemahasiswa.entity;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import org.hibernate.annotations.GenericGenerator;
import org.springframework.format.annotation.DateTimeFormat;
import com.fasterxml.jackson.annotation.JsonBackReference;
import lombok.Data;

@Entity
@Data
public class Mahasiswa {

    @Id
    @GeneratedValue(generator = "uuid" )
    @GenericGenerator(name = "uuid", strategy = "uuid2")
    private String id;

    @NotNull
    private String angkatan;

    @NotBlank
    @NotNull
    private String nim;

    @NotBlank
    private String nama;

    @NotNull
    @Enumerated(EnumType.STRING)
    private  JenisKelamin jenisKelamin;

    @ManyToOne
    @NotNull
    @JoinColumn(name = "id_agama")
    private Agama idAgama;

    @ManyToOne
    @NotNull
    @JoinColumn(name = "id_prodi")
    private Prodi idProdi;

    @ManyToOne
    @JoinColumn(name = "id_konsentrasi")
    private  Konsentrasi idKonsentrasi;

    @NotNull
    private  String tempatLahir;

    @NotNull
    private String statusMatrikulasi;

    @ManyToOne
    @NotNull
    @JoinColumn(name = "id_program")
    private  Program idProgram;

    @Column(columnDefinition = "DATE")
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDate tanggalLahir;

    private String idKelurahan;

    private  String idKecamatan;

    private  String idKotaKabupaten;

    private  String idProvinsi;

    private  String idNegara;

    private  String kewarganegaraan;

    @NotNull
    private  String nik;

    private  String nisn;

    private String namaJalan;
    private String rt;
    private String rw;
    private String namaDusun;


    private String kodepos;
    @ManyToOne
    @JoinColumn(name = "jenis_tinggal")
    private JenisTinggal jenisTinggal;

    @ManyToOne
    @JoinColumn(name = "id_ibu")
    private Ibu ibu;

    @ManyToOne
    @JoinColumn(name = "id_ayah")
    private Ayah ayah;

    @ManyToOne
    @JoinColumn(name = "alat_transportasi")
    private Transportasi alatTransportasi;

    private String teleponRumah;
    private String teleponSeluler;
    private String emailPribadi;
    private String emailTazkia;

    @NotNull
    private String statusAktif;

    @ManyToOne
    @JoinColumn(name = "id_tahun_mengundurkan_diri")
    private TahunAkademik tahunAkademikMengundurkanDiri;

    @Column(columnDefinition = "DATE")
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDate tanggalMengundurkanDiri;

    @NotNull @Enumerated(EnumType.STRING)
    private StatusRecord status = StatusRecord.AKTIF;

    private String statusUpdateUser;

    @ManyToOne
    @JoinColumn(name = "id_user")
    private  User user;

    @ManyToOne
    @JsonBackReference
    @JoinColumn(name = "id_dosen_wali")
    private  Dosen dosen ;

    private String ukuranBaju;
    private String kps = "Tidak";
    private String nomorKps;

    @Column(columnDefinition = "DATE")
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDate terakhirUpdate;

    @ManyToOne
    @JoinColumn(name = "id_kurikulum")
    private  Kurikulum kurikulum;

    private String rfid;
    @NotNull
    private Integer idAbsen;

    private String nirm;

    private String nirl;

    @Column(columnDefinition = "LONGTEXT")
    private String judul;
    @Column(columnDefinition = "LONGTEXT")
    private String title;
    private String noTranskript;
    private String indukNasional;

    @Column(columnDefinition = "DATE")
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDate tanggalMasuk;

    /**
     * Jenis masuk mahasiswa
     * Menentukan jalur/cara mahasiswa masuk ke perguruan tinggi
     */
    @Enumerated(EnumType.STRING)
    private JenisMasuk jenisMasuk;

    @Column(columnDefinition = "DATE")
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDate tanggalLulus;

    /**
     * Status keluar mahasiswa
     * Diisi saat mahasiswa sudah keluar/lulus
     * null = mahasiswa masih aktif/kuliah
     */
    @Enumerated(EnumType.STRING)
    private StatusKeluar statusKeluar;

    /**
     * Tanggal keluar (lebih umum dari tanggalLulus)
     * Bisa tanggal lulus, tanggal mengundurkan diri, dll
     */
    @Column(columnDefinition = "DATE")
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDate tanggalKeluar;

    /**
     * Tahun akademik saat mahasiswa keluar
     * Lebih umum dari tahunAkademikMengundurkanDiri
     */
    @ManyToOne
    @JoinColumn(name = "id_tahun_akademik_keluar")
    private TahunAkademik tahunAkademikKeluar;

    /**
     * Alasan keluar (opsional)
     * Bisa diisi untuk keterangan tambahan
     */
    @Column(columnDefinition = "TEXT")
    private String alasanKeluar;

    /**
     * User yang mengupdate status keluar
     */
    private String statusKeluarUpdateUser;

    /**
     * Tanggal update status keluar
     */
    private LocalDateTime statusKeluarUpdateDate;

    @ManyToOne
    @JoinColumn(name = "id_beasiswa")
    private Beasiswa beasiswa;

    private String fileKtp;
    private String fileIjazah;
    private String foto;

    @Column(columnDefinition = "DATE")
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDate tanggalDisahkan;

    private String mbkmPermission;

    public String selectionText() {
        return "";
    }

    private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd MMMM yyyy", new Locale("id"));

    // Constructor
    public void tampilTanggalLahir(LocalDate date) {
        this.tanggalLahir = date;
    }


    // Formatter untuk dateTime
    public String formatTanggalLahir() {
        return tanggalLahir.format(formatter);
    }

    // Custom setter untuk ukuranBaju
    public void setUkuranBaju(String ukuranBaju) {
        // Logging untuk debugging
        if (ukuranBaju == null && this.ukuranBaju != null && !this.ukuranBaju.trim().isEmpty()) {
            // Log stack trace untuk melihat dari mana method ini dipanggil
            System.out.println("⚠️  MENCOBA SET UKURAN BAJU KE NULL!");
            System.out.println("   Nilai sebelumnya: '" + this.ukuranBaju + "'");
            System.out.println("   NIM: " + this.nim);
            System.out.println("   Stack trace:");
            StackTraceElement[] stackTrace = Thread.currentThread().getStackTrace();
            for (int i = 2; i < Math.min(6, stackTrace.length); i++) {
                System.out.println("   -> " + stackTrace[i].getClassName() + "." + stackTrace[i].getMethodName() + ":" + stackTrace[i].getLineNumber());
            }
            System.out.println("   Nilai TIDAK DIUBAH - dipertahankan: '" + this.ukuranBaju + "'");
            // Tidak mengubah nilai, tetap mempertahankan nilai lama
            return;
        }
        
        // Log perubahan nilai untuk debugging
        if (this.ukuranBaju != null && ukuranBaju != null && !this.ukuranBaju.equals(ukuranBaju)) {
            System.out.println("✅ Mengubah ukuran baju dari '" + this.ukuranBaju + "' ke '" + ukuranBaju + "' (NIM: " + this.nim + ")");
        }
        
        // Jika nilai baru bukan null atau nilai lama memang kosong, update nilai
        this.ukuranBaju = ukuranBaju;
    }

    // Custom getter untuk ukuranBaju
    public String getUkuranBaju() {
        return this.ukuranBaju;
    }

    // Method untuk memaksa update ukuranBaju (jika diperlukan untuk bypass logic di setter)
    public void forceSetUkuranBaju(String ukuranBaju) {
        this.ukuranBaju = ukuranBaju;
    }
}
