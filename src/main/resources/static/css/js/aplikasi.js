$(document).ready(function(){
    var urlKelurahan = "/api/kelurahan";
    var urlProdi = "/api/prodi";
    var urlMatkul = "/api/matakuliah";
    var urlKurikulum = "/api/kurikulum";
    var templateUrlKurikulum = "/api/prodi/{prodi}/kurikulum";


    var kelurahan = null;
    var prodi = null;
    var matakuliah = null;
    var kurikulum = null;
    var kelas = null;
    var plotKelas = null;
    var hari = null;
    var ruangan = null;
    var sesi = null;
    var plotMatakuliah= null;

    var urlKurikulum = null;
    var urlKelas= null;
    var urlPlotKelas= null;
    var urlPlotmatakuliah= null;

    var inputMatakuliah = $("#matakuliah");
    var inputSesi = $("#sesi");
    var inputKelas= $("#kelas");
    var inputHari= $("#hari");
    var inputRuangan= $("#ruangan");
    var inputKelurahan = $("#kelurahan");
    var inputProdi = $("#prodi");
    var inputKurikulum = $("#kurikulum");
    var inputPlotKelas= $("#plotKelas");
    var inputSKS= $("#sks");
    var inputDosen= $("#idDosen");
    var inputPlotMatakuliah= $("#plotmatakuliah");
    var isiProdi= $("#isiProdi");
    var isiRuangan= $("#isiRuangan");
    var isiKurikulum= $("#isiKurikulum");
    var isiPlotKelas= $("#isiPlotKelas");
    var isiPlotmatakuliah= $("#isiPlotmatakuliah");
    var isiHari= $("#isiHari");
    var jamMulai= $("#jamMulai");
    var jamSelesai= $("#jamSelesai");
    var isiSesi= $("#isiSesi");
    var singkatan= $("#singkatan");
    var matkulEng= $("#matkulEng");
    var kodeMatkul= $("#kodeMatkul");
    // var isiSesi= $("#isiSesi");
    //uploadSmartTest
    var inputHiddenIdKelurahan= $("input[name=idKelurahan]");
    var inputHiddenIdKecamatan = $("input[name=idKecamatan]");
    var inputHiddenIsiProdi= $("input[name=prodi]");
    var inputHiddenIsiHari = $("input[name=idHari]");
    var inputHiddenIsiRuangan= $("input[name=ruangan]");
    var inputHiddenIsiKelas= $("input[name=kelas]");
    var inputHiddenIsiSesi= $("input[name=sesi]");
    var inputHiddenIsiPlotKelas= $("input[name=idKelas]");
    var inputHiddenIsiPlotMatakuliah= $("input[name=matakuliahKurikulum]");
    var inputHiddenIsiKurikulum= $("input[name=kurikulum]");
    var inputHiddenIdKotakabupaten = $("input[name=idKotaKabupaten]");
    var inputHiddenIdprovinsi = $("input[name=idProvinsi]");
    var inputKodepos = $("input[name=kodepos]");
    var inputKodeMatakuliah= $("input[name=kodeMatakuliah]");
    var inputNamaMatakuliahEnglish = $("input[name=namaMatakuliahEnglish]");
    var inputSingkatan = $("input[name=singkatan]");
    var inputIdMatkul = $("input[name=matakuliah]");


    var resetInput = function(inputField){
        inputField.val('');
        inputField.prop('disabled', true);
    };


    inputProdi.typeahead({
        displayText: function(item){ return item.namaProdi;},
        source: _.debounce(function(cari, process){
            prodi = null;
            $.get(urlProdi, {search: cari}, function(hasil){
                process(hasil.content);
            }, "json");
        }, 500),
        afterSelect: function(pilihan){
            prodi = pilihan.namaProdi;
            inputKurikulum.prop('disabled', false);
            isiProdi = pilihan.id;
            inputHiddenIsiProdi.val(pilihan.id);
            console.log(inputHiddenIsiProdi)
            urlKurikulum = _.replace(templateUrlKurikulum, '{prodi}', pilihan.id);
        }
    });

    inputPlotKelas.typeahead({
        displayText: function(item){ return item.namaKelas;},
        source: _.debounce(function(cari, process){
            plotKelas = null;
            $.get('/api/plotKelas', {search: cari}, function(hasil){
                process(hasil.content);
            }, "json");
        }, 500),
        afterSelect: function(pilihan){
            plotKelas = pilihan.namaKelas;
            // inputKurikulum.prop('disabled', false);
            isiPlotKelas = pilihan.id;
            inputHiddenIsiPlotKelas.val(pilihan.id);
        }
    });

    inputPlotMatakuliah.typeahead({
        displayText: function(item){ return item.matakuliah.namaMatakuliah + '  ,  ' + item.matakuliah.namaMatakuliahEnglish;},
        source: _.debounce(function(cari, process){
            kurikulum = null;
            $.get('/api/plotMatakuliah', {id: inputPlotKelas.val(),search: cari}, function(hasil){
                process(hasil.content);
            }, "json");
        }, 500),
        afterSelect: function(pilihan) {
            isiPlotmatakuliah = pilihan.matakuliah.id;
            inputHiddenIsiPlotMatakuliah.val(pilihan.id);
            inputSKS = pilihan.jumlahSks;

        }
    });

    inputHari.typeahead({
        displayText: function(item){ return item.namaHari + '  ,  ' + item.namaHariEng;},
        source: _.debounce(function(cari, process){
            hari = null;
            $.get('/api/hari', {search: cari}, function(hasil){
                process(hasil);
            }, "json");
        }, 500),
        afterSelect: function(pilihan) {
            isiHari = pilihan.id;
            inputHiddenIsiHari.val(pilihan.id);

        }
    });

    inputRuangan.typeahead({
        displayText: function(item){ return item.namaRuangan;},
        source: _.debounce(function(cari, process){
            ruangan = null;
            $.get('/api/ruangan', {search: cari}, function(hasil){
                process(hasil);
            }, "json");
        }, 500),
        afterSelect: function(pilihan) {
            isiRuangan = pilihan.id;
            inputHiddenIsiRuangan.val(pilihan.id);

        }
    });

    inputSesi.typeahead({
        displayText: function(item){ return item.namaSesi;},
        source: _.debounce(function(cari, process){
            sesi = null;
            $.get('/api/sesi', {idHari: inputHiddenIsiHari.val(),idRuangan: inputHiddenIsiRuangan.val(),sks: inputSKS.val(),kelas : isiPlotKelas.val(),dosen : inputDosen.val(),search: cari}, function(hasil){
                process(hasil);
            }, "json");
        }, 500),
        afterSelect: function(pilihan) {
            isiSesi = pilihan.id;
            inputHiddenIsiSesi.val(pilihan.sesi);
            jamMulai.val(pilihan.jamMulai);
            jamSelesai.val(pilihan.jamSelesai)

        }
    });

    inputKurikulum.typeahead({
        displayText: function(item){ return item.namaKurikulum;},
        source: _.debounce(function(cari, process){
            kurikulum = null;
            $.get('/api/kurikulum', {namaProdi: inputHiddenIsiProdi.val(),search: cari}, function(hasil){
                process(hasil);
            }, "json");
        }, 500),
        afterSelect: function(pilihan) {
            console.log( pilihan.id);
            console.log( isiProdi);
            console.log( inputHiddenIsiProdi);
            inputHiddenIsiKurikulum.val(pilihan.id);

        }
    });

    inputKelas.typeahead({
        displayText: function(item){ return item.namaKelas;},
        source: _.debounce(function(cari, process){
            kelas = null;
            $.get('/api/kelas', {idProdi: inputHiddenIsiProdi.val(),search: cari}, function(hasil){
                process(hasil.content);
            }, "json");
        }, 500),
        afterSelect: function(pilihan) {
            inputHiddenIsiKelas.val(pilihan.id);

        }
    });

    // resetInput(inputKabupatenKota);
    inputKelurahan.typeahead({
        displayText: function(item){ return item.propinsi  + '  ,  ' + item.kabupaten + '  ,  ' + item.kecamatan + '  ,  ' + item.kelurahan;},
        source: _.debounce(function(cari, process){
            kelurahan = null;
            $.get(urlKelurahan, {search: cari}, function(hasil){
                process(hasil.content);
            }, "json");
        }, 500),
        afterSelect: function(pilihan) {
            inputHiddenIdKelurahan.val(pilihan.kelurahan);
            inputHiddenIdKecamatan.val(pilihan.kecamatan);
            inputHiddenIdKotakabupaten.val(pilihan.kabupaten);
            inputHiddenIdprovinsi.val(pilihan.propinsi);
            inputKodepos.val(pilihan.kodepos);
            console.log( pilihan.kelurahan);
            console.log( pilihan.kecamatan);
            console.log( pilihan.kabupaten);
            console.log( pilihan.propinsi);
            console.log( pilihan.kodepos);
        }

    });



    inputMatakuliah.typeahead({
        displayText: function(item){ return item.namaMatakuliah;},
        source: _.debounce(function(cari, process){
            matakuliah = null;
            $.get(urlMatkul, {search: cari}, function(hasil){
                process(hasil.content);
            }, "json");
        }, 500),
        afterSelect: function(pilihan) {
            inputNamaMatakuliahEnglish.val(pilihan.namaMatakuliahEnglish);
            inputKodeMatakuliah.val(pilihan.kodeMatakuliah);
            inputSingkatan.val(pilihan.singkatan);
            singkatan.val(pilihan.kodeMatakuliah);
            matkulEng.val(pilihan.namaMatakuliahEnglish);
            kodeMatkul.val(pilihan.kodeMatakuliah);
            inputIdMatkul.val(pilihan.id);
            console.log( pilihan.namaMatakuliahEnglish);
            console.log(pilihan)
        }

    });

});