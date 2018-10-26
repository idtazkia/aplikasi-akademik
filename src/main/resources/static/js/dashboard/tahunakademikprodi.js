function getMissAttendance() {
    var resultElement=document.getElementById('isiTahunAkademikProdi');
    var todoId=document.getElementById('isiTahunAkademikProdi')
    resultElement.innerHTML='';

    axios.get('/tahunakademik/prodi')
        .then(function (response) {
            resultElement.innerHTML=generateSuccessHTMLOutput(response);
        })
        .catch(function (error) {
            resultElement.innerHTML=generateErrorHTMLOutput(error);
        })
}

function generateSuccessHTMLOutput(response) {
    for(i=0;i < response.data.length;i++) {
        hasil += generateRowPersentase(response.data[i])

    }
}
