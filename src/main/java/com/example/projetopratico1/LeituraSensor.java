package com.example.projetopratico1;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbBean;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbPartitionKey;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbSecondaryPartitionKey;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbSecondarySortKey;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbSortKey;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@DynamoDbBean
public class LeituraSensor {

    private String sensorId;
    private String timestamp;
    private String localizacao;
    private Double valor;

    @DynamoDbPartitionKey
    public String getSensorId() {
        return sensorId;
    }

    @DynamoDbSortKey
    @DynamoDbSecondarySortKey(indexNames = "LocalizacaoIndex")
    public String getTimestamp() {
        return timestamp;
    }

    @DynamoDbSecondaryPartitionKey(indexNames = "LocalizacaoIndex")
    public String getLocalizacao() {
        return localizacao;
    }

    public Double getValor() {
        return valor;
    }
}
