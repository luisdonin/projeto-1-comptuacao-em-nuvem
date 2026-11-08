package com.example.projetopratico1;

import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.core.exception.SdkException;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbEnhancedClient;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbTable;
import software.amazon.awssdk.enhanced.dynamodb.TableSchema;
import software.amazon.awssdk.enhanced.dynamodb.model.CreateTableEnhancedRequest;
import software.amazon.awssdk.enhanced.dynamodb.model.EnhancedGlobalSecondaryIndex;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.model.Projection;
import software.amazon.awssdk.services.dynamodb.model.ProjectionType;
import software.amazon.awssdk.services.dynamodb.model.ProvisionedThroughput;
import software.amazon.awssdk.services.dynamodb.model.ResourceInUseException;
import software.amazon.awssdk.services.dynamodb.model.ResourceNotFoundException;

@Component
public class TableInitializer {

    private final DynamoDbClient dynamoDbClient;
    private final DynamoDbEnhancedClient dynamoDbEnhancedClient;

    public TableInitializer(DynamoDbClient dynamoDbClient, DynamoDbEnhancedClient dynamoDbEnhancedClient) {
        this.dynamoDbClient = dynamoDbClient;
        this.dynamoDbEnhancedClient = dynamoDbEnhancedClient;
    }

    @PostConstruct
    public void init() {
        try {
            dynamoDbClient.describeTable(builder -> builder.tableName("LeituraSensor"));
        } catch (ResourceNotFoundException e) {
            criarTabela();
        } catch (SdkException e) {
        }
    }

    private void criarTabela() {
        try {
            DynamoDbTable<LeituraSensor> table = dynamoDbEnhancedClient.table(
                    "LeituraSensor",
                    TableSchema.fromBean(LeituraSensor.class)
            );

            EnhancedGlobalSecondaryIndex localizacaoIndex = EnhancedGlobalSecondaryIndex.builder()
                    .indexName("LocalizacaoIndex")
                    .projection(Projection.builder().projectionType(ProjectionType.ALL).build())
                    .provisionedThroughput(ProvisionedThroughput.builder()
                            .readCapacityUnits(5L)
                            .writeCapacityUnits(5L)
                            .build())
                    .build();

            CreateTableEnhancedRequest request = CreateTableEnhancedRequest.builder()
                    .provisionedThroughput(ProvisionedThroughput.builder()
                            .readCapacityUnits(5L)
                            .writeCapacityUnits(5L)
                            .build())
                    .globalSecondaryIndices(localizacaoIndex)
                    .build();

            table.createTable(request);
        } catch (ResourceInUseException ex) {
        } catch (SdkException ex) {
        }
    }
}
