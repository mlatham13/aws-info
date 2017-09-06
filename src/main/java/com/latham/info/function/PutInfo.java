package com.latham.info.function;

import java.io.PrintWriter;
import java.io.StringWriter;

import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClientBuilder;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapper;
import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.util.json.Jackson;
import com.latham.info.model.ServerlessInput;
import com.latham.info.model.ServerlessOutput;
import com.latham.info.table.TableLathamInfo;

/**
 * Lambda function that triggered by the API Gateway event "POST /".
 */
public class PutInfo implements RequestHandler<ServerlessInput, ServerlessOutput> {

	// DynamoDB client interface
    static AmazonDynamoDB client = AmazonDynamoDBClientBuilder.standard().build();

    // DynamoDB table attribute name for storing info title. Required post parameter
    private static final String INFO_TABLE_ID = "id";
        
    /**
     * Create a new 'info' entry in the table, where the 'id' is passed as a URL parameter
     * and the body is a JSON string with 'title' and 'info'.
     * 	id		: URL parameter
     *  title	: Parsed from the body
     *  info	: Parsed from the body
     */
    @Override
    public ServerlessOutput handleRequest(ServerlessInput serverlessInput, Context context) {
            ServerlessOutput output = new ServerlessOutput();
            DynamoDBMapper mapper = new DynamoDBMapper(client);

            try {
                if (serverlessInput.getQueryStringParameters() == null 
                		|| serverlessInput.getQueryStringParameters().get(INFO_TABLE_ID) == null
                		|| serverlessInput.getQueryStringParameters().get(INFO_TABLE_ID).isEmpty()) {
                    throw new Exception("Information title must be provided. (Parameter " + INFO_TABLE_ID + ")");
                }
                String id = serverlessInput.getQueryStringParameters().get(INFO_TABLE_ID);

                String content = serverlessInput.getBody();
                if (content == null || content.isEmpty()) {
                	throw new Exception("Message body is empty. Some information must be provided.");
                }
                TableLathamInfo infoItem = Jackson.fromJsonString(content, TableLathamInfo.class);
                if (infoItem == null) {
                	throw new Exception("Unable to convert JSON to TableLathamInfo: " + content);
                }
                if (infoItem.getTitle() == null || infoItem.getTitle().isEmpty()
                		|| infoItem.getInfo() == null || infoItem.getTitle().isEmpty()) {
                	throw new Exception("Missing title or info: " + content);
                }
                infoItem.setId(id);
                mapper.save(infoItem);
               
                output.setStatusCode(200);
                output.setBody("Successfully inserted new information.");
            } catch (Exception e) {
                output.setStatusCode(500);
                StringWriter sw = new StringWriter();
                e.printStackTrace(new PrintWriter(sw));
                output.setBody(sw.toString());
            }
            
        return output;
    }
}