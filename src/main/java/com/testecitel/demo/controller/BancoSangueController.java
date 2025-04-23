package com.testecitel.demo.controller;

import com.testecitel.demo.model.BancoSangue;
import com.testecitel.demo.service.BancoSangueService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.ModelAndView;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Controller
@RequestMapping(path = "/bancodesangue")
public class BancoSangueController {

    @Autowired
    BancoSangueService bancoSangueService;

    @RequestMapping
    public ModelAndView Lista() {
        ModelAndView mv = new ModelAndView("banco/list_banco");
        return mv;
    }

    @GetMapping("/estatisticas")
    public ModelAndView Estatisticas() {
        ModelAndView mv = new ModelAndView("banco/estatisticas_banco_sangue");
        return mv;
    }

    /**
     * Endpoint para listar todos os registros do banco de sangue
     * @return Lista de objetos BancoSangue
     */
    @GetMapping("/listar")
    @ResponseBody
    public List<BancoSangue> listarTodos() {
        return bancoSangueService.listarTodos();
    }

    /**
     * Endpoint para obter detalhes de um registro específico
     * @param id ID do registro
     * @return Objeto BancoSangue ou erro 404
     */
    @GetMapping("/detalhe/{id}")
    @ResponseBody
    public ResponseEntity<?> obterDetalhe(@PathVariable Long id) {
        Optional<BancoSangue> registro = bancoSangueService.buscarPorId(id);
        if (registro.isPresent()) {
            return ResponseEntity.ok(registro.get());
        } else {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "Registro não encontrado");
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
        }
    }

    /**
     * Endpoint para receber o upload do arquivo JSON
     * @param file Arquivo JSON enviado pelo usuário
     * @return ResponseEntity com mensagem de sucesso ou erro
     */
    @PostMapping("/upload")
    @ResponseBody
    public ResponseEntity<?> uploadJsonFile(@RequestParam("file") MultipartFile file) {
        Map<String, Object> response = new HashMap<>();

        try {
            if (file.isEmpty()) {
                response.put("success", false);
                response.put("message", "Arquivo vazio. Por favor, selecione um arquivo JSON válido.");
                return ResponseEntity.badRequest().body(response);
            }

            if (!file.getOriginalFilename().endsWith(".json")) {
                response.put("success", false);
                response.put("message", "Formato de arquivo inválido. Por favor, selecione um arquivo JSON.");
                return ResponseEntity.badRequest().body(response);
            }

            // Manda a lista do Json para o service fazer a validação e salvar na tabela
            List<BancoSangue> dadosImportados = bancoSangueService.processarArquivoJson(file);

            response.put("success", true);
            response.put("message", "Arquivo processado com sucesso. " + dadosImportados.size() + " registros importados.");
            response.put("totalRegistros", dadosImportados.size());

            return ResponseEntity.ok(response);

        } catch (IOException e) {
            response.put("success", false);
            response.put("message", "Erro ao processar o arquivo: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Erro inesperado: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    /**
     * Endpoint para obter estatísticas de candidatos por estado
     * @return ResponseEntity com os dados estatísticos
     */
    @GetMapping("/estatisticas/estados")
    @ResponseBody
    public ResponseEntity<?> getEstatisticasPorEstado() {
        try {
            Map<String, Long> estatisticas = bancoSangueService.contarCandidatosPorEstado();
            return ResponseEntity.ok(estatisticas);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "Erro ao obter estatísticas: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    /**
     * Endpoint para obter estatísticas de IMC médio por faixa etária
     * @return ResponseEntity com os dados estatísticos
     */
    @GetMapping("/estatisticas/imc")
    @ResponseBody
    public ResponseEntity<?> getEstatisticasImc() {
        try {
            Map<String, Double> estatisticas = bancoSangueService.calcularImcMedioPorFaixaEtaria();
            return ResponseEntity.ok(estatisticas);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "Erro ao obter estatísticas: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    /**
     * Endpoint para obter estatísticas de percentual de obesos por sexo
     * @return ResponseEntity com os dados estatísticos
     */
    @GetMapping("/estatisticas/obesos")
    @ResponseBody
    public ResponseEntity<?> getEstatisticasObesos() {
        try {
            Map<String, Double> estatisticas = bancoSangueService.calcularPercentualObesosPorSexo();
            return ResponseEntity.ok(estatisticas);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "Erro ao obter estatísticas: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    /**
     * Endpoint para obter estatísticas de média de idade por tipo sanguíneo
     * @return ResponseEntity com os dados estatísticos
     */
    @GetMapping("/estatisticas/idade")
    @ResponseBody
    public ResponseEntity<?> getEstatisticasIdade() {
        try {
            Map<String, Double> estatisticas = bancoSangueService.calcularMediaIdadePorTipoSanguineo();
            return ResponseEntity.ok(estatisticas);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "Erro ao obter estatísticas: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    /**
     * Endpoint para obter estatísticas de possíveis doadores por tipo sanguíneo
     * @return ResponseEntity com os dados estatísticos
     */
    @GetMapping("/estatisticas/doadores")
    @ResponseBody
    public ResponseEntity<?> getEstatisticasDoadores() {
        try {
            Map<String, Integer> estatisticas = bancoSangueService.contarPossiveisDoadores();
            return ResponseEntity.ok(estatisticas);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "Erro ao obter estatísticas: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    /**
     * Endpoint para obter todas as estatísticas em um único objeto
     * @return ResponseEntity com todos os dados estatísticos
     */
    @GetMapping("/estatisticas/todas")
    @ResponseBody
    public ResponseEntity<?> getTodasEstatisticas() {
        try {
            Map<String, Object> todasEstatisticas = new HashMap<>();

            todasEstatisticas.put("candidatosPorEstado", bancoSangueService.contarCandidatosPorEstado());
            todasEstatisticas.put("imcMedioPorFaixaEtaria", bancoSangueService.calcularImcMedioPorFaixaEtaria());
            todasEstatisticas.put("percentualObesosPorSexo", bancoSangueService.calcularPercentualObesosPorSexo());
            todasEstatisticas.put("mediaIdadePorTipoSanguineo", bancoSangueService.calcularMediaIdadePorTipoSanguineo());
            todasEstatisticas.put("possiveisDoadores", bancoSangueService.contarPossiveisDoadores());

            return ResponseEntity.ok(todasEstatisticas);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "Erro ao obter estatísticas: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
}
