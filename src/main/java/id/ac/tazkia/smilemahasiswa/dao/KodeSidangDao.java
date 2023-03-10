package id.ac.tazkia.smilemahasiswa.dao;

import id.ac.tazkia.smilemahasiswa.entity.KodeSidang;
import id.ac.tazkia.smilemahasiswa.entity.Prodi;
import id.ac.tazkia.smilemahasiswa.entity.StatusRecord;
import id.ac.tazkia.smilemahasiswa.entity.TahunAkademik;
import org.springframework.data.repository.PagingAndSortingRepository;

import java.util.List;

public interface KodeSidangDao extends PagingAndSortingRepository<KodeSidang, String> {

    KodeSidang findTopByProdiAndTahunAndStatusOrderByKodeDesc(Prodi prodi, String tahun, StatusRecord statusRecord);

}
