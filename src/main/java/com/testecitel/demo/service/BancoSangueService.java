package com.testecitel.demo.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import com.testecitel.demo.model.BancoSangue;
import com.testecitel.demo.repository.BancoSangueDAO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

@Service
public class BancoSangueService {

    @Autowired
    private BancoSangueDAO bancoSangueDAO;

    /**
     * Listar todos os registros do banco de sangue
     * @return Lista de objetos BancoSangue
     */
    public List<BancoSangue> listarTodos() {
        Iterable<BancoSangue> iterable = bancoSangueDAO.findAll();
        return StreamSupport.stream(iterable.spliterator(), false)
                .collect(Collectors.toList());
    }

    /**
     * Buscar registro por ID
     * @param id ID do registro
     * @return Optional com o objeto BancoSangue, se encontrado
     */
    public Optional<BancoSangue> buscarPorId(Long id) {
        return bancoSangueDAO.findById(id);
    }

    /**
     * Processa o arquivo JSON enviado pelo usuário
     * @param file Arquivo JSON enviado
     * @return Resultado do processamento contendo registros importados e ignorados
     * @throws IOException Em caso de erro na leitura do arquivo
     */
    public Map<String, Object> processarArquivoJson(MultipartFile file) throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        InputStream inputStream = file.getInputStream();

        List<Map<String, Object>> dadosJson = mapper.readValue(inputStream, new TypeReference<List<Map<String, Object>>>() {});

        List<BancoSangue> dadosProcessados = new ArrayList<>();
        List<String> cpfsDuplicados = new ArrayList<>();

        for (Map<String, Object> item : dadosJson) {
            String cpf = (String) item.get("cpf");

            // Verifica se o CPF já existe no banco de dados
            if (cpf != null && !cpf.isEmpty() && bancoSangueDAO.existsByCpf(cpf)) {
                // CPF já existe, adiciona à lista de duplicados
                cpfsDuplicados.add(cpf);
                continue; // Pula para o próximo item
            }

            BancoSangue bancoSangue = new BancoSangue();

            bancoSangue.setNome((String) item.get("nome"));
            bancoSangue.setCpf(cpf);
            bancoSangue.setRg((String) item.get("rg"));

            String dataNasc = (String) item.get("data_nasc");
            bancoSangue.setDataNasc(converterFormatoData(dataNasc));

            bancoSangue.setSexo((String) item.get("sexo"));
            bancoSangue.setMae((String) item.get("mae"));
            bancoSangue.setPai((String) item.get("pai"));
            bancoSangue.setEmail((String) item.get("email"));
            bancoSangue.setCep((String) item.get("cep"));
            bancoSangue.setEndereco((String) item.get("endereco"));


            if (item.get("numero") != null) {
                if (item.get("numero") instanceof Integer) {
                    bancoSangue.setNumero((Integer) item.get("numero"));
                } else if (item.get("numero") instanceof Number) {
                    bancoSangue.setNumero(((Number) item.get("numero")).intValue());                        // Converter número para Integer
                } else {
                    try {
                        bancoSangue.setNumero(Integer.parseInt(item.get("numero").toString()));
                    } catch (NumberFormatException e) {
                        bancoSangue.setNumero(0);
                    }
                }
            }

            bancoSangue.setBairro((String) item.get("bairro"));
            bancoSangue.setCidade((String) item.get("cidade"));
            bancoSangue.setEstado((String) item.get("estado"));
            bancoSangue.setTelefoneFixo((String) item.get("telefone_fixo"));
            bancoSangue.setCelular((String) item.get("celular"));


            if (item.get("altura") != null) {
                if (item.get("altura") instanceof Double) {
                    bancoSangue.setAltura((Double) item.get("altura"));
                } else if (item.get("altura") instanceof Number) {
                    bancoSangue.setAltura(((Number) item.get("altura")).doubleValue());                       // Converter altura para Double
                } else {
                    try {
                        bancoSangue.setAltura(Double.parseDouble(item.get("altura").toString()));
                    } catch (NumberFormatException e) {
                        bancoSangue.setAltura(0.0);
                    }
                }
            }


            if (item.get("peso") != null) {
                if (item.get("peso") instanceof Double) {
                    bancoSangue.setPeso((Double) item.get("peso"));
                } else if (item.get("peso") instanceof Number) {
                    bancoSangue.setPeso(((Number) item.get("peso")).doubleValue());
                } else {
                    try {
                        bancoSangue.setPeso(Double.parseDouble(item.get("peso").toString()));                // Converter peso para Double
                    } catch (NumberFormatException e) {
                        bancoSangue.setPeso(0.0);
                    }
                }
            }

            bancoSangue.setTipoSanguineo((String) item.get("tipo_sanguineo"));

            dadosProcessados.add(bancoSangue);
        }

        // Salva apenas os registros não duplicados
        if (!dadosProcessados.isEmpty()) {
            bancoSangueDAO.saveAll(dadosProcessados);
        }

        // Prepara o resultado do processamento
        Map<String, Object> resultado = new HashMap<>();
        resultado.put("registrosImportados", dadosProcessados.size());
        resultado.put("registrosIgnorados", cpfsDuplicados.size());
        resultado.put("cpfsDuplicados", cpfsDuplicados);

        return resultado;
    }

    /**
     * Converte o formato da data de MM/dd/yyyy para dd/MM/yyyy
     * @param dataOriginal Data no formato original (MM/dd/yyyy)
     * @return Data no formato convertido (dd/MM/yyyy)
     */
    private String converterFormatoData(String dataOriginal) {
        if (dataOriginal == null || dataOriginal.isEmpty()) {
            return "";
        }

        try {
            if (dataOriginal.matches("\\d{2}/\\d{2}/\\d{4}")) {
                SimpleDateFormat formatoOriginal = new SimpleDateFormat("MM/dd/yyyy");
                SimpleDateFormat formatoDesejado = new SimpleDateFormat("dd/MM/yyyy");

                Date data = formatoOriginal.parse(dataOriginal);
                return formatoDesejado.format(data);
            } else {
                return dataOriginal;
            }
        } catch (ParseException e) {
            return dataOriginal;
        }
    }

    /**
     * Retorna a contagem de candidatos por estado
     * @return Mapa com estado como chave e contagem como valor
     */
    public Map<String, Long> contarCandidatosPorEstado() {
        Iterable<BancoSangue> todos = bancoSangueDAO.findAll();
        Map<String, Long> contagemPorEstado = new HashMap<>();

        for (BancoSangue pessoa : todos) {
            String estado = pessoa.getEstado();
            if (estado != null && !estado.isEmpty()) {
                contagemPorEstado.put(estado, contagemPorEstado.getOrDefault(estado, 0L) + 1);
            }
        }

        return contagemPorEstado;
    }

    /**
     * Calcula o IMC médio por faixa etária
     * @return Mapa com faixa etária como chave e IMC médio como valor
     */
    public Map<String, Double> calcularImcMedioPorFaixaEtaria() {
        Iterable<BancoSangue> todos = bancoSangueDAO.findAll();
        Map<String, List<Double>> imcsPorFaixaEtaria = new HashMap<>();

        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
        Calendar hoje = Calendar.getInstance();

        for (BancoSangue pessoa : todos) {
            try {

                String dataNascStr = pessoa.getDataNasc();
                if (dataNascStr == null || dataNascStr.isEmpty()) continue;             // Calcular idade

                Date dataNasc = sdf.parse(dataNascStr);
                Calendar nascimento = Calendar.getInstance();
                nascimento.setTime(dataNasc);

                int idade = hoje.get(Calendar.YEAR) - nascimento.get(Calendar.YEAR);
                if (hoje.get(Calendar.DAY_OF_YEAR) < nascimento.get(Calendar.DAY_OF_YEAR)) {
                    idade--;
                }


                String faixaEtaria;
                if (idade <= 10) faixaEtaria = "0 a 10";
                else if (idade <= 20) faixaEtaria = "11 a 20";       // Determinar faixa etária
                else if (idade <= 30) faixaEtaria = "21 a 30";
                else faixaEtaria = "31+";


                Double altura = pessoa.getAltura();
                Double peso = pessoa.getPeso();

                if (altura != null && peso != null && altura > 0) {
                    double imc = peso / (altura * altura);                          // Calcular IMC

                    if (!imcsPorFaixaEtaria.containsKey(faixaEtaria)) {
                        imcsPorFaixaEtaria.put(faixaEtaria, new ArrayList<>());
                    }
                    imcsPorFaixaEtaria.get(faixaEtaria).add(imc);
                }
            } catch (ParseException e) {
                continue;
            }
        }


        Map<String, Double> imcMedioPorFaixaEtaria = new HashMap<>();
        for (Map.Entry<String, List<Double>> entry : imcsPorFaixaEtaria.entrySet()) {
            List<Double> imcs = entry.getValue();
            if (!imcs.isEmpty()) {
                double soma = 0;                                                          // Calcular médias
                for (Double imc : imcs) {
                    soma += imc;
                }
                imcMedioPorFaixaEtaria.put(entry.getKey(), soma / imcs.size());
            }
        }

        return imcMedioPorFaixaEtaria;
    }

    /**
     * Calcula o percentual de obesos entre homens e mulheres
     * @return Mapa com sexo como chave e percentual de obesos como valor
     */
    public Map<String, Double> calcularPercentualObesosPorSexo() {
        Iterable<BancoSangue> todos = bancoSangueDAO.findAll();
        Map<String, Integer> totalPorSexo = new HashMap<>();
        Map<String, Integer> obesosPorSexo = new HashMap<>();

        for (BancoSangue pessoa : todos) {
            String sexo = pessoa.getSexo();
            if (sexo == null || sexo.isEmpty()) continue;

             /*Incrementar contagem total por sexo*/
            totalPorSexo.put(sexo, totalPorSexo.getOrDefault(sexo, 0) + 1);

             /*Verificar se é obeso (IMC > 30)*/
            Double altura = pessoa.getAltura();
            Double peso = pessoa.getPeso();

            if (altura != null && peso != null && altura > 0) {
                double imc = peso / (altura * altura);
                if (imc > 30) {
                    obesosPorSexo.put(sexo, obesosPorSexo.getOrDefault(sexo, 0) + 1);
                }
            }
        }


        Map<String, Double> percentualObesosPorSexo = new HashMap<>();
        for (String sexo : totalPorSexo.keySet()) {
            int total = totalPorSexo.get(sexo);
            int obesos = obesosPorSexo.getOrDefault(sexo, 0);            // Calcular percentuais

            if (total > 0) {
                double percentual = (double) obesos / total * 100;
                percentualObesosPorSexo.put(sexo, percentual);
            }
        }

        return percentualObesosPorSexo;
    }

    /**
     * Calcula a média de idade por tipo sanguíneo
     * @return Mapa com tipo sanguíneo como chave e média de idade como valor
     */
    public Map<String, Double> calcularMediaIdadePorTipoSanguineo() {
        Iterable<BancoSangue> todos = bancoSangueDAO.findAll();
        Map<String, List<Integer>> idadesPorTipoSanguineo = new HashMap<>();

        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
        Calendar hoje = Calendar.getInstance();

        for (BancoSangue pessoa : todos) {
            String tipoSanguineo = pessoa.getTipoSanguineo();
            if (tipoSanguineo == null || tipoSanguineo.isEmpty()) continue;

            try {
                String dataNascStr = pessoa.getDataNasc();
                if (dataNascStr == null || dataNascStr.isEmpty()) continue;

                Date dataNasc = sdf.parse(dataNascStr);
                Calendar nascimento = Calendar.getInstance();
                nascimento.setTime(dataNasc);

                int idade = hoje.get(Calendar.YEAR) - nascimento.get(Calendar.YEAR);
                if (hoje.get(Calendar.DAY_OF_YEAR) < nascimento.get(Calendar.DAY_OF_YEAR)) {
                    idade--;
                }

                if (!idadesPorTipoSanguineo.containsKey(tipoSanguineo)) {
                    idadesPorTipoSanguineo.put(tipoSanguineo, new ArrayList<>());
                }
                idadesPorTipoSanguineo.get(tipoSanguineo).add(idade);
            } catch (ParseException e) {
                continue;
            }
        }


        Map<String, Double> mediaIdadePorTipoSanguineo = new HashMap<>();
        for (Map.Entry<String, List<Integer>> entry : idadesPorTipoSanguineo.entrySet()) {
            List<Integer> idades = entry.getValue();                                                         // Calcular médias
            if (!idades.isEmpty()) {
                double soma = 0;
                for (Integer idade : idades) {
                    soma += idade;
                }
                mediaIdadePorTipoSanguineo.put(entry.getKey(), soma / idades.size());
            }
        }

        return mediaIdadePorTipoSanguineo;
    }

    /**
     * Conta a quantidade de possíveis doadores para cada tipo sanguíneo receptor
     * @return Mapa com tipo sanguíneo receptor como chave e quantidade de possíveis doadores como valor
     */
    public Map<String, Integer> contarPossiveisDoadores() {
        Iterable<BancoSangue> todos = bancoSangueDAO.findAll();
        List<BancoSangue> doadoresValidos = new ArrayList<>();

        /*Filtrar doadores válidos (idade entre 16 e 69 anos e peso > 50kg)*/
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
        Calendar hoje = Calendar.getInstance();

        for (BancoSangue pessoa : todos) {
            try {
                // Verificar idade
                String dataNascStr = pessoa.getDataNasc();
                if (dataNascStr == null || dataNascStr.isEmpty()) continue;

                Date dataNasc = sdf.parse(dataNascStr);
                Calendar nascimento = Calendar.getInstance();
                nascimento.setTime(dataNasc);

                int idade = hoje.get(Calendar.YEAR) - nascimento.get(Calendar.YEAR);
                if (hoje.get(Calendar.DAY_OF_YEAR) < nascimento.get(Calendar.DAY_OF_YEAR)) {
                    idade--;
                }

                // Verificar peso
                Double peso = pessoa.getPeso();

                if (idade >= 16 && idade <= 69 && peso != null && peso > 50) {
                    doadoresValidos.add(pessoa);
                }
            } catch (ParseException e) {
                // Ignorar registros com data inválida
                continue;
            }
        }

        // Mapear compatibilidade de tipos sanguíneos
        Map<String, List<String>> compatibilidade = new HashMap<>();
        compatibilidade.put("A+", Arrays.asList("A+", "A-", "O+", "O-"));
        compatibilidade.put("A-", Arrays.asList("A-", "O-"));
        compatibilidade.put("B+", Arrays.asList("B+", "B-", "O+", "O-"));
        compatibilidade.put("B-", Arrays.asList("B-", "O-"));
        compatibilidade.put("AB+", Arrays.asList("A+", "A-", "B+", "B-", "AB+", "AB-", "O+", "O-"));
        compatibilidade.put("AB-", Arrays.asList("A-", "B-", "AB-", "O-"));
        compatibilidade.put("O+", Arrays.asList("O+", "O-"));
        compatibilidade.put("O-", Arrays.asList("O-"));

        // Contar doadores por tipo sanguíneo receptor
        Map<String, Integer> doadoresPorReceptor = new HashMap<>();

        for (String tipoReceptor : compatibilidade.keySet()) {
            List<String> tiposCompativeis = compatibilidade.get(tipoReceptor);

            int count = 0;
            for (BancoSangue doador : doadoresValidos) {
                String tipoDoador = doador.getTipoSanguineo();
                if (tipoDoador != null && tiposCompativeis.contains(tipoDoador)) {
                    count++;
                }
            }

            doadoresPorReceptor.put(tipoReceptor, count);
        }

        return doadoresPorReceptor;
    }
}
