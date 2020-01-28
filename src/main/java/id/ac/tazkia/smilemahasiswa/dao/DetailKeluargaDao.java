package id.ac.tazkia.smilemahasiswa.dao;

import id.ac.tazkia.smilemahasiswa.dto.user.ProfileDto;
import id.ac.tazkia.smilemahasiswa.entity.Mahasiswa;
import id.ac.tazkia.smilemahasiswa.entity.MahasiswaDetailKeluarga;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.repository.query.Param;

public interface DetailKeluargaDao extends PagingAndSortingRepository<MahasiswaDetailKeluarga, String> {

    @Query("select new id.ac.tazkia.smilemahasiswa.dto.user.ProfileDto(m.mahasiswa.id,m.mahasiswa.nim,m.mahasiswa.nama,m.mahasiswa.idAgama,m.mahasiswa.tempatLahir,m.mahasiswa.tanggalLahir,m.mahasiswa.jenisKelamin,m.mahasiswa.nik,m.mahasiswa.nisn,m.mahasiswa.kewarganegaraan,m.mahasiswa.alatTransportasi,m.mahasiswa.teleponRumah,m.mahasiswa.teleponSeluler,m.mahasiswa.emailPribadi,m.mahasiswa.emailTazkia,m.mahasiswa.ukuranBaju,m.mahasiswa.idNegara,m.mahasiswa.namaJalan,m.ayah.nik,m.ayah.namaAyah,m.ayah.idJenjangPendidikan,m.ayah.tempatLahir,m.ayah.tanggalLahir,m.ayah.idPekerjaan,m.ayah.penghasilan,m.ayah.agama,m.ayah.statusHidup,m.ayah.kebutuhanKhusus, m.ibu.nik,m.ibu.namaIbuKandung,m.ibu.idJenjangPendidikan,m.ibu.tempatLahir,m.ibu.tanggalLahir,m.ibu.idPekerjaan,m.ibu.penghasilan,m.ibu.agama,m.ibu.statusHidup,m.ibu.kebutuhanKhusus,m.mahasiswa.idProdi.namaProdi,m.mahasiswa.angkatan) from MahasiswaDetailKeluarga m where m.status = 'AKTIF' and m.mahasiswa = :mahasiswa")
    ProfileDto userProfile(@Param("mahasiswa")Mahasiswa mahasiswa);
}
