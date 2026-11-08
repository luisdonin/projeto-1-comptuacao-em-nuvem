package com.example.projetopratico1;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SensorControllerTest {

    @Mock
    private SensorService sensorService;

    @InjectMocks
    private SensorController sensorController;

    private LeituraSensor leitura;

    @BeforeEach
    void setUp() {
        leitura = new LeituraSensor("sensor-1", "2026-11-08T10:00:00Z", "sala-1", 25.5);
    }

    @Test
    void testSalvar() {
        when(sensorService.salvar(any(LeituraSensor.class))).thenReturn(leitura);

        ResponseEntity<LeituraSensor> response = sensorController.salvar(leitura);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("sensor-1", response.getBody().getSensorId());
        verify(sensorService).salvar(leitura);
    }

    @Test
    void testAtualizar() {
        when(sensorService.atualizar(any(LeituraSensor.class))).thenReturn(leitura);

        ResponseEntity<LeituraSensor> response = sensorController.atualizar(leitura);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("sensor-1", response.getBody().getSensorId());
        verify(sensorService).atualizar(leitura);
    }

    @Test
    void testDeletar() {
        when(sensorService.deletar("sensor-1", "2026-11-08T10:00:00Z")).thenReturn(leitura);

        ResponseEntity<LeituraSensor> response = sensorController.deletar("sensor-1", "2026-11-08T10:00:00Z");

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("sensor-1", response.getBody().getSensorId());
        verify(sensorService).deletar("sensor-1", "2026-11-08T10:00:00Z");
    }

    @Test
    void testObterPorSensorIdETimestampSucesso() {
        when(sensorService.obterPorSensorIdETimestamp("sensor-1", "2026-11-08T10:00:00Z")).thenReturn(leitura);

        ResponseEntity<LeituraSensor> response = sensorController.obterPorSensorIdETimestamp("sensor-1", "2026-11-08T10:00:00Z");

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("sensor-1", response.getBody().getSensorId());
    }

    @Test
    void testObterPorSensorIdETimestampNaoEncontrado() {
        when(sensorService.obterPorSensorIdETimestamp("sensor-1", "2026-11-08T10:00:00Z")).thenReturn(null);

        ResponseEntity<LeituraSensor> response = sensorController.obterPorSensorIdETimestamp("sensor-1", "2026-11-08T10:00:00Z");

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }

    @Test
    void testListarPorSensorIdSemLocalizacao() {
        when(sensorService.listarPorSensorId("sensor-1")).thenReturn(List.of(leitura));

        ResponseEntity<List<LeituraSensor>> response = sensorController.listarPorSensorId("sensor-1", null);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(1, response.getBody().size());
        verify(sensorService).listarPorSensorId("sensor-1");
    }

    @Test
    void testListarPorSensorIdComLocalizacao() {
        when(sensorService.listarPorSensorIdELocalizacao("sensor-1", "sala-1")).thenReturn(List.of(leitura));

        ResponseEntity<List<LeituraSensor>> response = sensorController.listarPorSensorId("sensor-1", "sala-1");

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(1, response.getBody().size());
        verify(sensorService).listarPorSensorIdELocalizacao("sensor-1", "sala-1");
    }

    @Test
    void testListarPorSensorIdELocalizacaoPath() {
        when(sensorService.listarPorSensorIdELocalizacao("sensor-1", "sala-1")).thenReturn(List.of(leitura));

        ResponseEntity<List<LeituraSensor>> response = sensorController.listarPorSensorIdELocalizacao("sensor-1", "sala-1");

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(1, response.getBody().size());
        verify(sensorService).listarPorSensorIdELocalizacao("sensor-1", "sala-1");
    }

    @Test
    void testListarPorLocalizacao() {
        when(sensorService.listarPorLocalizacao("sala-1")).thenReturn(List.of(leitura));

        ResponseEntity<List<LeituraSensor>> response = sensorController.listarPorLocalizacao("sala-1");

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(1, response.getBody().size());
        verify(sensorService).listarPorLocalizacao("sala-1");
    }

    @Test
    void testListarTodos() {
        when(sensorService.listarTodos()).thenReturn(List.of(leitura));

        ResponseEntity<List<LeituraSensor>> response = sensorController.listarTodos();

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(1, response.getBody().size());
        verify(sensorService).listarTodos();
    }
}
