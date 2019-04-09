$(document).ready(function(){
    var urlKelurahan = "/api/kelurahan";
    var urlMatkul = "/api/matakuliah";

    var kelurahan = null;
    var matakuliah = null;

    var inputMatakuliah = $("#matakuliah");
    var inputKelurahan = $("#kelurahan");
    //uploadSmartTest
    var inputHiddenIdKelurahan= $("input[name=idKelurahan]");
    var inputHiddenIdKecamatan = $("input[name=idKecamatan]");
    var inputHiddenIdKotakabupaten = $("input[name=idKotaKabupaten]");
    var inputHiddenIdprovinsi = $("input[name=idProvinsi]");
    var inputKodepos = $("input[name=kodepos]");
    var inputKodeMatakuliah= $("input[name=kodeMatakuliah]");
    var inputNamaMatakuliahEnglish = $("input[name=namaMatakuliahEnglish]");
    var inputSingkatan = $("input[name=singkatan]");
    var inputIdMatkul = $("input[name=idMat]");


    var resetInput = function(inputField){
        inputField.val('');
        inputField.prop('disabled', true);
    };

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
            inputIdMatkul.val(pilihan.id);
            console.log( pilihan.namaMatakuliahEnglish);
        }

    });

});