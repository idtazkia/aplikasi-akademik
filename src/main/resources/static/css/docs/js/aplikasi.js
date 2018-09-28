$(document).ready(function(){
    var urlKelurahan = "/api/kelurahan";

    var kelurahan = null;


    var inputKelurahan = $("#kelurahan");
    //uploadSmartTest
    var inputHiddenIdKelurahan= $("input[name=idKelurahan]");
    var inputHiddenIdKecamatan = $("input[name=idKecamatan]");
    var inputHiddenIdKotakabupaten = $("input[name=idKotaKabupaten]");
    var inputHiddenIdprovinsi = $("input[name=idProvinsi]");


    var resetInput = function(inputField){
        inputField.val('');
        inputField.prop('disabled', true);
    };

    // resetInput(inputKabupatenKota);
    inputKelurahan.typeahead({
        displayText: function(item){ return item.nama  + item.kecamatan.nama;},
        source: _.debounce(function(cari, process){
            kelurahan = null;
            $.get(urlKelurahan, {nama: cari}, function(hasil){
                process(hasil.content);
            }, "json");
        }, 500),
        afterSelect: function(pilihan) {
            inputHiddenIdKelurahan.val(pilihan.id);
            console.log( pilihan.id);
        }

    });

    inputProvinsi.typeahead({
        displayText: function(item){ return item.nama;},
        source: _.debounce(function(cari, process){
            provinsi = null;
            // resetInput(inputKabupatenKota);
            $.get(urlProvinsi, {nama: cari}, function(hasil){
                process(hasil);
            }, "json");
        }, 500),
        afterSelect: function(pilihan){
            provinsi = pilihan;
            inputKabupatenKota.prop('disabled', false);
            urlKabupaten = _.replace(templateUrlKabupaten, '{provinsi}', provinsi.id);
        }
    });

    inputKabupatenKota.typeahead({
        displayText: function(item){ return item.nama;},
        source: _.debounce(function(cari, process){
            kabupatenKota = null;
            $.get(urlKabupaten, {nama: cari}, function(hasil){
                process(hasil);
            }, "json");
        }, 500),
        afterSelect: function(pilihan) {
            console.log( pilihan.id);}
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