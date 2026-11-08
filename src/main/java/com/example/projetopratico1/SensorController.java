package com.example.projetopratico1;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/sensores")
public class SensorController {

    private final SensorService sensorService;

    public SensorController(SensorService sensorService) {
        this.sensorService = sensorService;
    }

    @PostMapping
    public ResponseEntity<LeituraSensor> salvar(@RequestBody LeituraSensor leitura) {
        LeituraSensor salvo = sensorService.salvar(leitura);
        return ResponseEntity.status(HttpStatus.CREATED).body(salvo);
    }

    @PutMapping
    public ResponseEntity<LeituraSensor> atualizar(@RequestBody LeituraSensor leitura) {
        LeituraSensor atualizado = sensorService.atualizar(leitura);
        return ResponseEntity.ok(atualizado);
    }

    @DeleteMapping("/{sensorId}/{timestamp}")
    public ResponseEntity<LeituraSensor> deletar(
            @PathVariable String sensorId,
            @PathVariable String timestamp) {
        LeituraSensor deletado = sensorService.deletar(sensorId, timestamp);
        return ResponseEntity.ok(deletado);
    }

    @GetMapping("/{sensorId}/{timestamp}")
    public ResponseEntity<LeituraSensor> obterPorSensorIdETimestamp(
            @PathVariable String sensorId,
            @PathVariable String timestamp) {
        LeituraSensor leitura = sensorService.obterPorSensorIdETimestamp(sensorId, timestamp);
        if (leitura == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(leitura);
    }

    @GetMapping("/sensor/{sensorId}")
    public ResponseEntity<List<LeituraSensor>> listarPorSensorId(
            @PathVariable String sensorId,
            @RequestParam(required = false) String localizacao) {
        if (localizacao != null && !localizacao.isBlank()) {
            return ResponseEntity.ok(sensorService.listarPorSensorIdELocalizacao(sensorId, localizacao));
        }
        return ResponseEntity.ok(sensorService.listarPorSensorId(sensorId));
    }

    @GetMapping("/sensor/{sensorId}/localizacao/{localizacao}")
    public ResponseEntity<List<LeituraSensor>> listarPorSensorIdELocalizacao(
            @PathVariable String sensorId,
            @PathVariable String localizacao) {
        return ResponseEntity.ok(sensorService.listarPorSensorIdELocalizacao(sensorId, localizacao));
    }

    @GetMapping("/localizacao/{localizacao}")
    public ResponseEntity<List<LeituraSensor>> listarPorLocalizacao(@PathVariable String localizacao) {
        return ResponseEntity.ok(sensorService.listarPorLocalizacao(localizacao));
    }

    @GetMapping
    public ResponseEntity<List<LeituraSensor>> listarTodos() {
        return ResponseEntity.ok(sensorService.listarTodos());
    }
}
