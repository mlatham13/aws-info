package com.latham.info.function;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClientBuilder;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapper;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBQueryExpression;
import com.amazonaws.services.dynamodbv2.model.AttributeValue;
import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.latham.info.model.ServerlessInput;
import com.latham.info.model.ServerlessOutput;
import com.latham.info.table.TableLathamInfo;

/**
 * Lambda function that triggered by the API Gateway event "GET /". Reads query parameter "id" for the information id, retrieves
 * the content from the table, and returns the content as the payload of the HTTP Response.
 */
public class GetInfo implements RequestHandler<ServerlessInput, ServerlessOutput> {
	// DynamoDB client interface
    static AmazonDynamoDB client = AmazonDynamoDBClientBuilder.standard().build();

    // DynamoDB table attribute name for storing info title.
    private static final String INFO_TABLE_ID = "id";
    
    @Override
    public ServerlessOutput handleRequest(ServerlessInput serverlessInput, Context context) {
        ServerlessOutput output = new ServerlessOutput();
        DynamoDBMapper mapper = new DynamoDBMapper(client);
        
        try {
            if (serverlessInput.getQueryStringParameters() == null || serverlessInput.getQueryStringParameters().get(INFO_TABLE_ID) == null) {
                    throw new Exception("Parameter " + INFO_TABLE_ID + " in query must be provided.");
            }
            String id = serverlessInput.getQueryStringParameters().get(INFO_TABLE_ID);
            
            Map<String, AttributeValue> eav = new HashMap<String, AttributeValue>();
            eav.put(":val1", new AttributeValue().withS(id));

            DynamoDBQueryExpression<TableLathamInfo> queryExpression = new DynamoDBQueryExpression<TableLathamInfo>()
                .withKeyConditionExpression("id = :val1").withExpressionAttributeValues(eav);

            List<TableLathamInfo> infoList = mapper.query(TableLathamInfo.class, queryExpression);
            if (infoList == null || infoList.size() == 0) {
            	throw new Exception("No information found for id " + id);
            }
            if (infoList.size() > 1) {
            	throw new Exception("More than one entry found for id " + id);
            }
            
            String content = infoList.get(0).toString();
            output.setStatusCode(200);
            output.setBody(content);
        } catch (Exception e) {
            output.setStatusCode(500);
            StringWriter sw = new StringWriter();
            e.printStackTrace(new PrintWriter(sw));
            output.setBody(sw.toString());
        }

        return output;
    }
}