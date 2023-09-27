package id.ac.tazkia.smilemahasiswa.dao;

import id.ac.tazkia.smilemahasiswa.dto.PilihMatakuliahSetaraDto;
import id.ac.tazkia.smilemahasiswa.dto.select2.CourseDto;
import id.ac.tazkia.smilemahasiswa.entity.Mahasiswa;
import id.ac.tazkia.smilemahasiswa.entity.Matakuliah;
import id.ac.tazkia.smilemahasiswa.entity.StatusRecord;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.PagingAndSortingRepository;

import java.util.List;

public interface MatakuliahDao extends PagingAndSortingRepository<Matakuliah,String> {
    Page<Matakuliah> findByStatusNotInOrderByNamaMatakuliahAsc(List<StatusRecord> status, Pageable page);

    Page<Matakuliah> findByNamaMatakuliahContainingIgnoreCase(String search, Pageable page);

    Page<Matakuliah> findByNamaMatakuliahContainingIgnoreCaseOrNamaMatakuliahEnglishContainingIgnoreCase(String nama,String name,Pageable page);

    @Query(value = "select id,nama_matakuliah as text,nama_matakuliah_english as english,kode_matakuliah as kode,singkatan from matakuliah where status = 'AKTIF' and nama_matakuliah like %?1% or status = 'AKTIF' and nama_matakuliah_english like %?1%", nativeQuery = true)
    List<CourseDto> cariMatakuliah(String search);

    @Query(value = "select id,nama_matakuliah as text,nama_matakuliah_english as english,kode_matakuliah as kode,singkatan from matakuliah where status = 'AKTIF'", nativeQuery = true)
    List<CourseDto> cariMatakuliah1();


    @Query(value = "SELECT a.id,a.kode_matakuliah as kodeMatakuliah,a.nama_matakuliah as namaMatakuliah,a.nama_matakuliah_english as namaMatakuliahEnglish FROM matakuliah AS a\n" +
            "LEFT JOIN \n" +
            "(SELECT * FROM\n" +
            "(SELECT a.id,b.id AS id_matakuliah,b.kode_matakuliah,b.nama_matakuliah,b.nama_matakuliah_english  FROM matakuliah_setara AS a \n" +
            "\tINNER JOIN matakuliah AS b ON a.id_matakuliah_setara = b.id \n" +
            "\tWHERE a.status = 'AKTIF' AND a.id_matakuliah = ?1 \n" +
            "UNION\n" +
            "SELECT a.id,b.id AS id_matakuliah,b.kode_matakuliah,b.nama_matakuliah,b.nama_matakuliah_english FROM matakuliah_setara AS a\n" +
            "\tINNER JOIN matakuliah AS b ON a.id_matakuliah = b.id\n" +
            "\tWHERE a.status = 'AKTIF' AND a.id_matakuliah_setara = ?1\n" +
            "\t)aa ORDER BY kode_matakuliah)b ON a.id = b.id_matakuliah\n" +
            "\tWHERE a.status = 'AKTIF' AND b.id IS NULL AND a.id <> ?1 ORDER BY a.kode_matakuliah", nativeQuery = true ,
            countQuery = "SELECT COUNT(id)AS id FROM\n" +
                    "(SELECT a.id,a.kode_matakuliah,a.nama_matakuliah,a.nama_matakuliah_english FROM matakuliah AS a\n" +
                    "LEFT JOIN \n" +
                    "(SELECT * FROM\n" +
                    "(SELECT a.id,b.id AS id_matakuliah,b.kode_matakuliah,b.nama_matakuliah,b.nama_matakuliah_english  FROM matakuliah_setara AS a \n" +
                    "\tINNER JOIN matakuliah AS b ON a.id_matakuliah_setara = b.id \n" +
                    "\tWHERE a.status = 'AKTIF' AND a.id_matakuliah = ?1 \n" +
                    "UNION\n" +
                    "SELECT a.id,b.id AS id_matakuliah,b.kode_matakuliah,b.nama_matakuliah,b.nama_matakuliah_english FROM matakuliah_setara AS a\n" +
                    "\tINNER JOIN matakuliah AS b ON a.id_matakuliah = b.id\n" +
                    "\tWHERE a.status = 'AKTIF' AND a.id_matakuliah_setara = ?1 \n" +
                    "\t)aa ORDER BY kode_matakuliah)b ON a.id = b.id_matakuliah\n" +
                    "\tWHERE a.status = 'AKTIF' AND b.id IS NULL AND a.id <> ?1)aa")
    Page<PilihMatakuliahSetaraDto> pilihMatakuliahSetara(String idMatakuliah, Pageable page);

    @Query(value = "select id_matakuliah_kurikulum id, kode_matakuliah, nama_matakuliah text, nama_matakuliah_english, semester from\n" +
            "            (SELECT a.*,id_matakuliahh, b.bobot\n" +
            "                    FROM (\n" +
            "                     SELECT a.id, a.id_matakuliah_kurikulum, a.id_matakuliah,\n" +
            "             GROUP_CONCAT('(', b.id_matakuliah_setara, ')' SEPARATOR ', ') AS setara,\n" +
            "    GROUP_CONCAT('(', c.id_matakuliah, ')' SEPARATOR ', ') AS setara2,\n" +
            "    kode_matakuliah, nama_matakuliah, nama_matakuliah_english, semester\n" +
            "    FROM (\n" +
            "                    SELECT a.id, b.id AS id_matakuliah_kurikulum, c.id AS id_matakuliah,\n" +
            "                    kode_matakuliah, nama_matakuliah, nama_matakuliah_english, semester\n" +
            "                            FROM mahasiswa AS a\n" +
            "                            INNER JOIN matakuliah_kurikulum AS b ON a.id_kurikulum = b.id_kurikulum\n" +
            "                            INNER JOIN matakuliah AS c ON b.id_matakuliah = c.id\n" +
            "                            WHERE a.id = ?1 AND b.status = 'AKTIF' AND c.status = 'AKTIF' and jumlah_sks > 0\n" +
            "                            ORDER BY b.semester, c.kode_matakuliah\n" +
            "                    ) AS a\n" +
            "    LEFT JOIN matakuliah_setara AS b ON a.id_matakuliah = b.id_matakuliah\n" +
            "    LEFT JOIN matakuliah_setara AS c ON a.id_matakuliah = c.id_matakuliah_setara\n" +
            "    GROUP BY a.id_matakuliah_kurikulum\n" +
            ") AS a\n" +
            "    LEFT JOIN (\n" +
            "            SELECT a.id_matakuliah_kurikulum, b.id_matakuliah AS id_matakuliahh, max(a.bobot) as bobot\n" +
            "    FROM krs_detail AS a\n" +
            "    INNER JOIN matakuliah_kurikulum AS b ON a.id_matakuliah_kurikulum = b.id\n" +
            "    WHERE a.status = 'AKTIF' AND a.id_mahasiswa = ?1 AND a.bobot >= 2 group by id_matakuliah\n" +
            ") AS b\n" +
            "    ON a.id_matakuliah = b.id_matakuliahh OR a.setara LIKE CONCAT('%(', b.id_matakuliahh, '),%') OR a.setara2 LIKE CONCAT('%(', b.id_matakuliahh, '),%')) as a\n" +
            "    where id_matakuliahh is null group by kode_matakuliah order by semester, kode_matakuliah", nativeQuery = true)
    List<CourseDto> findByMahasiswaKurikulum(Mahasiswa mahasiswa);

}
