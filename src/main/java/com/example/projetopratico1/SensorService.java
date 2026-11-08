package com.example.projetopratico1;

import org.springframework.stereotype.Service;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbEnhancedClient;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbIndex;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbTable;
import software.amazon.awssdk.enhanced.dynamodb.Expression;
import software.amazon.awssdk.enhanced.dynamodb.Key;
import software.amazon.awssdk.enhanced.dynamodb.TableSchema;
import software.amazon.awssdk.enhanced.dynamodb.model.QueryConditional;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;

import java.util.List;

@Service
public class SensorService {

    private final DynamoDbTable<LeituraSensor> table;

    public SensorService(DynamoDbEnhancedClient dynamoDbEnhancedClient) {
        this.table = dynamoDbEnhancedClient.table("LeituraSensor", TableSchema.fromBean(LeituraSensor.class));
    }

    public LeituraSensor salvar(LeituraSensor leitura) {
        table.putItem(leitura);
        return leitura;
    }

    public LeituraSensor atualizar(LeituraSensor leitura) {
        return table.updateItem(leitura);
    }

    public LeituraSensor deletar(String sensorId, String timestamp) {
        return table.deleteItem(Key.builder().partitionValue(sensorId).sortValue(timestamp).build());
    }

    public LeituraSensor obterPorSensorIdETimestamp(String sensorId, String timestamp) {
        return table.getItem(Key.builder().partitionValue(sensorId).sortValue(timestamp).build());
    }

    public List<LeituraSensor> listarPorSensorId(String sensorId) {
        QueryConditional queryConditional = QueryConditional.keyEqualTo(
                Key.builder().partitionValue(sensorId).build()
        );
        return table.query(r -> r.queryConditional(queryConditional))
                .items()
                .stream()
                .toList();
    }

    public List<LeituraSensor> listarPorLocalizacao(String localizacao) {
        DynamoDbIndex<LeituraSensor> index = table.index("LocalizacaoIndex");
        QueryConditional queryConditional = QueryConditional.keyEqualTo(
                Key.builder().partitionValue(localizacao).build()
        );
        return index.query(r -> r.queryConditional(queryConditional))
                .stream()
                .flatMap(page -> page.items().stream())
                .toList();
    }

    public List<LeituraSensor> listarPorSensorIdELocalizacao(String sensorId, String localizacao) {
        QueryConditional queryConditional = QueryConditional.keyEqualTo(
                Key.builder().partitionValue(sensorId).build()
        );
        Expression filterExpression = Expression.builder()
                .expression("localizacao = :loc")
                .putExpressionValue(":loc", AttributeValue.builder().s(localizacao).build())
                .build();
        return table.query(r -> r.queryConditional(queryConditional).filterExpression(filterExpression))
                .items()
                .stream()
                .toList();
    }

    public List<LeituraSensor> listarTodos() {
        return table.scan()
                .items()
                .stream()
                .toList();
    }
}
