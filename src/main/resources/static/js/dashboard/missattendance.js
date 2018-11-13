function getMissAttendance() {
    var resultElement=document.getElementById('getResult1');
    resultElement.innerHTML='';

    axios.get('/dashboardmahasiswa/presensi')
        .then(function (response) {
            resultElement.innerHTML=generateSuccessHTMLOutput(response);
        })
        .catch(function (error) {
            resultElement.innerHTML=generateErrorHTMLOutput(error);
        })
}

function generateSuccessHTMLOutput(response) {
    hasil = "";
    for(i=0;i < response.data.length;i++) {
        hasil += generateRowPersentase(response.data[i])

    }
    return hasil;
}

function generateRowPersentase(data) {
    matkul = data.matakuliah;
    persentase = data.persentase;
    absen = data.absen;
    if(persentase < 30) {
        warna = 'bg-green';
    }else if(persentase < 60){
        warna='bg-orange';
    }else{
        warna='bg-red';
    }
    return '<div class="widget_summary">' +
        '<div class="w_left w_25">' +
        '<span>' + matkul + ' :  ' + '</span>&nbsp;' +
        '</div>' +
        '<div class="w_center w_55">' +
        '<div class="progress">' +
        '<div class="progress-bar '+ warna +'" role="progressbar" aria-valuenow="' + persentase + '" aria-valuemin="0" aria-valuemax="100" style="width: ' + persentase + '%;">' +
        '<span class="sr-only">60% Complete</span>' +
        '</div>' +
        '</div>' +
        '</div>' +
        '<div class="w_right w_20">' +
        '&nbsp;<span>' + absen + '</span>' +
        '</div>' +
        '<div class="clearfix"></div>' +
        '</div>';
}