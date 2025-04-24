$(document).ready(function() {
    $('#cpf').mask('000.000.000-00', {reverse: true});

    pesquisarDoador();

    $('#editCpf').mask('000.000.000-00', {reverse: true});
    $('#editCep').mask('00000-000');
    $('#editTelefoneFixo').mask('(00) 0000-0000');
    $('#editCelular').mask('(00) 00000-0000');

    $('#btnSalvarEdicao').on('click', function() {
        salvarEdicaoDoador();
    });
});


function limparPesquisa() {
    $('#nome').val('');
    $('#cpf').val('');
    $('#sexo').val('');
    $('#tipoSanguineo').val('');
    $('#estado').val('');
    pesquisarDoador();
}

function pesquisarDoador() {
    let nome = $('#nome').val();
    let cpf = $('#cpf').val();
    let sexo = $('#sexo').val();
    let tipoSanguineo = $('#tipoSanguineo').val();
    let estado = $('#estado').val();

    $("#TblDoador").DataTable().destroy();
    $("#TblDoador").DataTable({
        serverSide: true,
        paging: true,
        searching: true,
        ajax: {
            url: "/doadores/pesquisar",
            type: "GET",
            async: false,
            dataType: "json",
            data: {
                nome: nome,
                cpf: cpf,
                sexo: sexo,
                tipoSanguineo: tipoSanguineo,
                estado: estado,
            }
        },
        columns: [
            { data: 'nome', className: 'text-center' },
            {
                data: 'telefone',
                className: 'text-center',
                render: function(data) {
                    // Formata o telefone para exibição
                    if (data) {
                        return formatarTelefone(data);
                    }
                    return data;
                }
            },
            { data: 'email', className: 'text-center' },
            {
                data: 'cpf',
                className: 'text-center',
                render: function(data) {
                    // Formata o CPF para exibição
                    if (data) {
                        return formatarCPF(data);
                    }
                    return data;
                }
            },
            {
                data: 'id',
                className: 'text-center',
                render: function(data) {
                    return `
                        <div class="btn-group">
                            <button class="btn btn-sm btn-warning mr-1" onclick="editarDoador(${data})">
                                <i class="fas fa-edit"></i>
                            </button>
                            <button class="btn btn-sm btn-danger" onclick="confirmarExclusao(${data})">
                                <i class="fas fa-trash"></i>
                            </button>
                        </div>
                    `;
                }
            },
        ],
        language: {
            "sEmptyTable": "Nenhum registro encontrado",
            "sInfo": "Mostrando de _START_ até _END_ de _TOTAL_ registros",
            "sInfoEmpty": "Mostrando 0 até 0 de 0 registros",
            "sInfoFiltered": "(Filtrados de _MAX_ registros)",
            "sInfoPostFix": "",
            "sInfoThousands": ".",
            "sLengthMenu": "_MENU_ resultados por página",
            "sLoadingRecords": "Carregando...",
            "sProcessing": "Processando...",
            "sZeroRecords": "Nenhum registro encontrado",
            "sSearch": "Pesquisar: ",
            "oPaginate": {
                "sNext": "Próximo",
                "sPrevious": "Anterior",
                "sFirst": "Primeiro",
                "sLast": "Último"
            },
            "oAria": {
                "sSortAscending": ": Ordenar colunas de forma ascendente",
                "sSortDescending": ": Ordenar colunas de forma descendente"
            }
        },
    });
}

function formatarCPF(cpf) {
    cpf = cpf.replace(/\D/g, '');

    if (cpf.length !== 11) {
        return cpf;
    }
    return cpf.replace(/(\d{3})(\d{3})(\d{3})(\d{2})/, "$1.$2.$3-$4");
}

function formatarTelefone(telefone) {
    telefone = telefone.replace(/\D/g, '');

    if (telefone.length === 11) {
        return telefone.replace(/(\d{2})(\d{5})(\d{4})/, "($1) $2-$3");
    } else if (telefone.length === 10) {
        return telefone.replace(/(\d{2})(\d{4})(\d{4})/, "($1) $2-$3");
    }

    return telefone;
}

function editarDoador(id) {
    $.ajax({
        url: '/bancodesangue/detalhe/' + id,
        type: 'GET',
        success: function(data) {
            preencherModalEdicao(data);
            $('#modalEditarDoador').modal('show');
        },
        error: function(xhr, status, error) {
            Swal.fire({
                icon: 'error',
                title: 'Erro',
                text: 'Erro ao carregar dados para edição. Por favor, tente novamente mais tarde.'
            });
        }
    });
}

function preencherModalEdicao(dados) {
    $('#editId').val(dados.id);
    $('#editNome').val(dados.nome);
    $('#editCpf').val(dados.cpf);
    $('#editRg').val(dados.rg);
    $('#editDataNasc').val(dados.dataNasc);
    $('#editSexo').val(dados.sexo);
    $('#editMae').val(dados.mae);
    $('#editPai').val(dados.pai);
    $('#editEmail').val(dados.email);
    $('#editCep').val(dados.cep);
    $('#editEndereco').val(dados.endereco);
    $('#editNumero').val(dados.numero);
    $('#editBairro').val(dados.bairro);
    $('#editCidade').val(dados.cidade);
    $('#editEstado').val(dados.estado);
    $('#editTelefoneFixo').val(dados.telefoneFixo);
    $('#editCelular').val(dados.celular);
    $('#editAltura').val(dados.altura);
    $('#editPeso').val(dados.peso);
    $('#editTipoSanguineo').val(dados.tipoSanguineo);
}

function salvarEdicaoDoador() {
    const doador = {
        id: $('#editId').val(),
        nome: $('#editNome').val(),
        cpf: $('#editCpf').val().replace(/\D/g, ''),
        rg: $('#editRg').val(),
        dataNasc: $('#editDataNasc').val(),
        sexo: $('#editSexo').val(),
        mae: $('#editMae').val(),
        pai: $('#editPai').val(),
        email: $('#editEmail').val(),
        cep: $('#editCep').val().replace(/\D/g, ''),
        endereco: $('#editEndereco').val(),
        numero: $('#editNumero').val(),
        bairro: $('#editBairro').val(),
        cidade: $('#editCidade').val(),
        estado: $('#editEstado').val(),
        telefoneFixo: $('#editTelefoneFixo').val().replace(/\D/g, ''),
        celular: $('#editCelular').val().replace(/\D/g, ''),
        altura: $('#editAltura').val(),
        peso: $('#editPeso').val(),
        tipoSanguineo: $('#editTipoSanguineo').val()
    };

    if (!doador.nome || !doador.cpf || !doador.dataNasc || !doador.sexo || !doador.tipoSanguineo) {
        Swal.fire({
            icon: 'error',
            title: 'Campos obrigatórios',
            text: 'Por favor, preencha todos os campos obrigatórios.'
        });
        return;
    }

    $.ajax({
        url: '/bancodesangue/atualizar',
        type: 'POST',
        contentType: 'application/json',
        data: JSON.stringify(doador),
        success: function(response) {
            $('#modalEditarDoador').modal('hide');

            Swal.fire({
                icon: 'success',
                title: 'Sucesso!',
                text: 'Doador atualizado com sucesso!'
            }).then(() => {
                // Recarregar a tabela para mostrar os dados atualizados
                pesquisarDoador();
            });
        },
        error: function(xhr, status, error) {
            Swal.fire({
                icon: 'error',
                title: 'Erro',
                text: 'Erro ao atualizar doador: ' + error
            });
        }
    });
}

function confirmarExclusao(id) {
    Swal.fire({
        title: 'Confirmar exclusão',
        text: "Tem certeza que deseja excluir este doador? Esta ação não pode ser desfeita.",
        icon: 'warning',
        showCancelButton: true,
        confirmButtonColor: '#d33',
        cancelButtonColor: '#3085d6',
        confirmButtonText: 'Sim, excluir!',
        cancelButtonText: 'Cancelar'
    }).then((result) => {
        if (result.isConfirmed) {
            excluirDoador(id);
        }
    });
}

function excluirDoador(id) {
    $.ajax({
        url: '/bancodesangue/excluir/' + id,
        type: 'DELETE',
        success: function(response) {
            Swal.fire({
                icon: 'success',
                title: 'Sucesso!',
                text: 'Doador excluído com sucesso!'
            }).then(() => {
                pesquisarDoador();
            });
        },
        error: function(xhr, status, error) {
            Swal.fire({
                icon: 'error',
                title: 'Erro',
                text: 'Erro ao excluir doador: ' + error
            });
        }
    });
}