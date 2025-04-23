$(document).ready(function() {

    $('#btnNovosDados').on('click', function() {
        $('#modalUploadJson').modal('show');
    });

    const dropArea = document.getElementById('dropArea');
    const fileUpload = document.getElementById('fileUpload');

    if (dropArea && fileUpload) {
        const dropText = document.getElementById('dropText');
        const fileInfo = document.getElementById('fileInfo');
        const fileName = document.getElementById('fileName');
        const btnSalvarJson = document.getElementById('btnSalvarJson');
        let jsonFile = null;

        dropArea.addEventListener('click', function() {
            fileUpload.click();
        });

        ['dragenter', 'dragover', 'dragleave', 'drop'].forEach(eventName => {
            dropArea.addEventListener(eventName, preventDefaults, false);
        });

        function preventDefaults(e) {
            e.preventDefault();
            e.stopPropagation();
        }

        ['dragenter', 'dragover'].forEach(eventName => {
            dropArea.addEventListener(eventName, highlight, false);
        });

        ['dragleave', 'drop'].forEach(eventName => {
            dropArea.addEventListener(eventName, unhighlight, false);
        });

        function highlight() {
            dropArea.style.borderColor = '#007bff';
            dropArea.style.backgroundColor = '#f8f9fa';
        }

        function unhighlight() {
            dropArea.style.borderColor = '#ccc';
            dropArea.style.backgroundColor = '';
        }

        dropArea.addEventListener('drop', handleDrop, false);

        function handleDrop(e) {
            const dt = e.dataTransfer;
            const files = dt.files;
            handleFiles(files);
        }

        fileUpload.addEventListener('change', function() {
            handleFiles(this.files);
        });

        function handleFiles(files) {
            if (files.length > 0) {
                const file = files[0];
                if (file.type === 'application/json' || file.name.endsWith('.json')) {
                    jsonFile = file;
                    showFileInfo(file);
                    btnSalvarJson.disabled = false;
                } else {
                    Swal.fire({
                        icon: 'error',
                        title: 'Arquivo inválido',
                        text: 'Por favor, selecione um arquivo JSON válido.'
                    });
                    resetUpload();
                }
            }
        }

        function showFileInfo(file) {
            dropText.style.display = 'none';
            fileInfo.style.display = 'block';
            fileName.textContent = file.name;
        }

        function resetUpload() {
            dropText.style.display = 'block';
            fileInfo.style.display = 'none';
            fileName.textContent = '';
            fileUpload.value = '';
            jsonFile = null;
            btnSalvarJson.disabled = true;
        }

        if (btnSalvarJson) {
            btnSalvarJson.addEventListener('click', function() {
                if (jsonFile) {
                    uploadJsonFile(jsonFile);
                }
            });
        }

        function uploadJsonFile(file) {
            const formData = new FormData();
            formData.append('file', file);

            const progressBar = document.querySelector('.progress');
            const progressBarInner = progressBar.querySelector('.progress-bar');
            progressBar.style.display = 'block';
            progressBarInner.style.width = '0%';
            progressBarInner.setAttribute('aria-valuenow', 0);

            $.ajax({
                url: '/bancodesangue/upload',
                type: 'POST',
                data: formData,
                processData: false,
                contentType: false,
                xhr: function() {
                    const xhr = new window.XMLHttpRequest();
                    xhr.upload.addEventListener('progress', function(e) {
                        if (e.lengthComputable) {
                            const percent = Math.round((e.loaded / e.total) * 100);
                            progressBarInner.style.width = percent + '%';
                            progressBarInner.setAttribute('aria-valuenow', percent);
                        }
                    }, false);
                    return xhr;
                },
                success: function(response) {
                    setTimeout(function() {
                        $('#modalUploadJson').modal('hide');
                        resetUpload();
                        progressBar.style.display = 'none';

                        Swal.fire({
                            icon: 'success',
                            title: 'Sucesso!',
                            text: 'Dados importados com sucesso!',
                            confirmButtonColor: '#28a745'
                        }).then(() => {

                            window.location.reload();
                        });
                    }, 1000);
                },
                error: function(xhr, status, error) {
                    progressBar.style.display = 'none';

                    Swal.fire({
                        icon: 'error',
                        title: 'Erro',
                        text: 'Erro ao importar dados: ' + error
                    });
                }
            });
        }
    } else {
        console.error("Elementos de upload não encontrados!");
    }

    carregarDadosBancoSangue();

    function carregarDadosBancoSangue() {
        $.ajax({
            url: '/bancodesangue/listar',
            type: 'GET',
            success: function(data) {
                processarDados(data);
            },
            error: function(xhr, status, error) {
                console.error("Erro ao carregar dados:", error);
                Swal.fire({
                    icon: 'error',
                    title: 'Erro',
                    text: 'Erro ao carregar dados. Por favor, tente novamente mais tarde.'
                });
            }
        });
    }

    function processarDados(dados) {
        if (!dados || dados.length === 0) {
            console.warn("Nenhum dado recebido para processar");
            return;
        }

        atualizarContadores(dados);

        processarDadosGraficos(dados);

        atualizarTabelaCompatibilidade(dados);
    }

    function atualizarContadores(dados) {
        $('#totalCandidatos').text(dados.length);

        const possiveisDoadores = dados.filter(candidato => {
            const idade = calcularIdade(candidato.dataNasc);
            return idade >= 16 && idade <= 69 && candidato.peso > 50;
        });
        $('#totalDoadores').text(possiveisDoadores.length);

        const somaIdades = dados.reduce((soma, candidato) => {
            return soma + calcularIdade(candidato.dataNasc);
        }, 0);
        const mediaIdade = (somaIdades / dados.length).toFixed(1);
        $('#mediaIdade').text(mediaIdade);

        const somaImc = dados.reduce((soma, candidato) => {
            const imc = candidato.peso / (candidato.altura * candidato.altura);
            return soma + imc;
        }, 0);
        const mediaImc = (somaImc / dados.length).toFixed(1);
        $('#mediaImc').text(mediaImc);
    }

    function processarDadosGraficos(dados) {
        const candidatosPorEstado = contarPorPropriedade(dados, 'estado');
        criarGraficoCandidatosPorEstado(candidatosPorEstado);

        const imcPorFaixaEtaria = calcularImcPorFaixaEtaria(dados);
        criarGraficoImcPorFaixaEtaria(imcPorFaixaEtaria);

        const obesosPorSexo = calcularObesosPorSexo(dados);
        criarGraficoObesosPorSexo(obesosPorSexo);

        const idadePorTipoSanguineo = calcularIdadePorTipoSanguineo(dados);
        criarGraficoIdadePorTipoSanguineo(idadePorTipoSanguineo);
    }


    function atualizarTabelaCompatibilidade(dados) {
        const possiveisDoadores = dados.filter(candidato => {
            const idade = calcularIdade(candidato.dataNasc);
            return idade >= 16 && idade <= 69 && candidato.peso > 50;
        });

        const doadoresPorTipo = contarPorPropriedade(possiveisDoadores, 'tipoSanguineo');               // Essa função atualiza a tabela, faz a contagem e popula na tabela de sangue.


        $('#doadoresA\\+').text(doadoresPorTipo['A+'] || 0);
        $('#doadoresA\\-').text(doadoresPorTipo['A-'] || 0);
        $('#doadoresB\\+').text(doadoresPorTipo['B+'] || 0);
        $('#doadoresB\\-').text(doadoresPorTipo['B-'] || 0);
        $('#doadoresAB\\+').text(doadoresPorTipo['AB+'] || 0);
        $('#doadoresAB\\-').text(doadoresPorTipo['AB-'] || 0);
        $('#doadoresO\\+').text(doadoresPorTipo['O+'] || 0);
        $('#doadoresO\\-').text(doadoresPorTipo['O-'] || 0);
    }

    function calcularIdade(dataNasc) {
        const hoje = new Date();
        const nascimento = new Date(dataNasc);
        let idade = hoje.getFullYear() - nascimento.getFullYear();
        const m = hoje.getMonth() - nascimento.getMonth();                      //calcula idade da pessoa de acordo com a data de nascimento

        if (m < 0 || (m === 0 && hoje.getDate() < nascimento.getDate())) {
            idade--;
        }

        return idade;
    }

    function contarPorPropriedade(dados, propriedade) {
        return dados.reduce((contador, item) => {
            const valor = item[propriedade];
            contador[valor] = (contador[valor] || 0) + 1;
            return contador;
        }, {});
    }


    function calcularImcPorFaixaEtaria(dados) {
        const faixas = {
            '0-10': { soma: 0, count: 0 },
            '11-20': { soma: 0, count: 0 },
            '21-30': { soma: 0, count: 0 },
            '31-40': { soma: 0, count: 0 },
            '41-50': { soma: 0, count: 0 },
            '51-60': { soma: 0, count: 0 },
            '61+': { soma: 0, count: 0 }
        };

        dados.forEach(candidato => {
            const idade = calcularIdade(candidato.dataNasc);
            const imc = candidato.peso / (candidato.altura * candidato.altura);

            let faixa;
            if (idade <= 10) faixa = '0-10';                                                                //define as faixas etarias e calcula o IMC, em seguida caulcula as médias
            else if (idade <= 20) faixa = '11-20';
            else if (idade <= 30) faixa = '21-30';
            else if (idade <= 40) faixa = '31-40';
            else if (idade <= 50) faixa = '41-50';
            else if (idade <= 60) faixa = '51-60';
            else faixa = '61+';

            faixas[faixa].soma += imc;
            faixas[faixa].count++;
        });

        const resultado = {};
        for (const [faixa, dados] of Object.entries(faixas)) {
            resultado[faixa] = dados.count > 0 ? dados.soma / dados.count : 0;
        }

        return resultado;
    }

    function calcularObesosPorSexo(dados) {
        const contagem = {
            'Masculino': { obesos: 0, total: 0 },
            'Feminino': { obesos: 0, total: 0 }
        };

        dados.forEach(candidato => {
            const imc = candidato.peso / (candidato.altura * candidato.altura);                                    //função para calcular obesos por sexo
            const sexo = candidato.sexo === 'M' ? 'Masculino' : 'Feminino';

            contagem[sexo].total++;
            if (imc > 30) {
                contagem[sexo].obesos++;
            }
        });

        return {
            'Masculino': contagem['Masculino'].total > 0 ?
                (contagem['Masculino'].obesos / contagem['Masculino'].total) * 100 : 0,
            'Feminino': contagem['Feminino'].total > 0 ?
                (contagem['Feminino'].obesos / contagem['Feminino'].total) * 100 : 0
        };
    }

    function calcularIdadePorTipoSanguineo(dados) {
        const tiposSanguineos = {
            'A+': { somaIdades: 0, count: 0 },
            'A-': { somaIdades: 0, count: 0 },
            'B+': { somaIdades: 0, count: 0 },
            'B-': { somaIdades: 0, count: 0 },
            'AB+': { somaIdades: 0, count: 0 },
            'AB-': { somaIdades: 0, count: 0 },
            'O+': { somaIdades: 0, count: 0 },
            'O-': { somaIdades: 0, count: 0 }
        };

        dados.forEach(candidato => {
            const idade = calcularIdade(candidato.dataNasc);
            const tipo = candidato.tipoSanguineo;

            if (tiposSanguineos[tipo]) {
                tiposSanguineos[tipo].somaIdades += idade;
                tiposSanguineos[tipo].count++;
            }
        });

        const resultado = {};
        for (const [tipo, dados] of Object.entries(tiposSanguineos)) {
            resultado[tipo] = dados.count > 0 ? dados.somaIdades / dados.count : 0;
        }

        return resultado;
    }

    // Função para criar gráfico de candidatos por estado
    function criarGraficoCandidatosPorEstado(dados) {
        const ctx = document.getElementById('candidatosPorEstadoChart').getContext('2d');

        // Ordenar estados por quantidade de candidatos
        const estados = Object.keys(dados).sort((a, b) => dados[b] - dados[a]);
        const quantidades = estados.map(estado => dados[estado]);

        new Chart(ctx, {
            type: 'bar',
            data: {
                labels: estados,
                datasets: [{
                    label: 'Candidatos por Estado',
                    data: quantidades,
                    backgroundColor: 'rgba(54, 162, 235, 0.6)',
                    borderColor: 'rgba(54, 162, 235, 1)',
                    borderWidth: 1
                }]
            },
            options: {
                responsive: true,
                scales: {
                    y: {
                        beginAtZero: true,
                        ticks: {
                            precision: 0
                        }
                    }
                }
            }
        });
    }

    // Função para criar gráfico de IMC por faixa etária
    function criarGraficoImcPorFaixaEtaria(dados) {
        const ctx = document.getElementById('imcPorFaixaEtariaChart').getContext('2d');

        const faixas = Object.keys(dados);
        const valores = faixas.map(faixa => dados[faixa].toFixed(1));

        new Chart(ctx, {
            type: 'bar',
            data: {
                labels: faixas,
                datasets: [{
                    label: 'IMC Médio',
                    data: valores,
                    backgroundColor: 'rgba(75, 192, 192, 0.6)',
                    borderColor: 'rgba(75, 192, 192, 1)',
                    borderWidth: 1
                }]
            },
            options: {
                responsive: true,
                scales: {
                    y: {
                        beginAtZero: true
                    }
                }
            }
        });
    }

    // Função para criar gráfico de percentual de obesos por sexo
    function criarGraficoObesosPorSexo(dados) {
        const ctx = document.getElementById('obesosPorSexoChart').getContext('2d');

        const sexos = Object.keys(dados);
        const percentuais = sexos.map(sexo => dados[sexo].toFixed(1));

        new Chart(ctx, {
            type: 'pie',
            data: {
                labels: sexos,
                datasets: [{
                    data: percentuais,
                    backgroundColor: [
                        'rgba(54, 162, 235, 0.6)',
                        'rgba(255, 99, 132, 0.6)'
                    ],
                    borderColor: [
                        'rgba(54, 162, 235, 1)',
                        'rgba(255, 99, 132, 1)'
                    ],
                    borderWidth: 1
                }]
            },
            options: {
                responsive: true,
                plugins: {
                    tooltip: {
                        callbacks: {
                            label: function(context) {
                                return `${context.label}: ${context.raw}%`;
                            }
                        }
                    }
                }
            }
        });
    }

    // Função para criar gráfico de média de idade por tipo sanguíneo
    function criarGraficoIdadePorTipoSanguineo(dados) {
        const ctx = document.getElementById('idadePorTipoSanguineoChart').getContext('2d');

        const tipos = Object.keys(dados);
        const medias = tipos.map(tipo => dados[tipo].toFixed(1));

        new Chart(ctx, {
            type: 'bar',
            data: {
                labels: tipos,
                datasets: [{
                    label: 'Média de Idade',
                    data: medias,
                    backgroundColor: 'rgba(153, 102, 255, 0.6)',
                    borderColor: 'rgba(153, 102, 255, 1)',
                    borderWidth: 1
                }]
            },
            options: {
                responsive: true,
                scales: {
                    y: {
                        beginAtZero: true
                    }
                }
            }
        });
    }
});
