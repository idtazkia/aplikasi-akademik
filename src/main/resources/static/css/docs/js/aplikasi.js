$(document).ready(function(){
    var urlKelurahan = "/api/kelurahan";

    var kelurahan = null;


    var inputKelurahan = $("#kelurahan");
    //uploadSmartTest
    var inputHiddenIdKelurahan= $("input[name=idKelurahan]");
    var inputHiddenIdKecamatan = $("input[name=idKecamatan]");
    var inputHiddenIdKotakabupaten = $("input[name=idKotaKabupaten]");
    var inputHiddenIdprovinsi = $("input[name=idProvinsi]");
    var inputKodepos = $("input[name=kodepos]");


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




    inputCariKokab.typeahead({
        displayText: function(item){ return item.nama;},
        source: _.debounce(function(cari, process){
            sekolah = null;
            $.get(urlCariKokab, {nama: cari}, function(hasil){
                process(hasil.content);
            }, "json");
        }, 500),
        afterSelect: function(pilihan) {
            inputHiddenIdKotakabupaten.val(pilihan.id);
            console.log( pilihan.id);

    }

    });

});