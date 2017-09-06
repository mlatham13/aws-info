package com.latham.info.function;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Map;

import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClientBuilder;
import com.amazonaws.services.dynamodbv2.model.AttributeValue;
import com.amazonaws.services.dynamodbv2.model.ScanRequest;
import com.amazonaws.services.dynamodbv2.model.ScanResult;
import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.latham.info.model.ServerlessInput;
import com.latham.info.model.ServerlessOutput;
import com.latham.info.table.TableLathamInfo;


/**
 * Lambda function that triggered by the API Gateway event "GET /titles". Reads query parameter "title" for the information title filter,
 * retrieves the content from the table, and returns the content as the payload of the HTTP Response. The content will be
 * a JSON list of titles.
 */
public class GetInfoList implements RequestHandler<ServerlessInput, ServerlessOutput> {

	// DynamoDB client interface
    static AmazonDynamoDB client = AmazonDynamoDBClientBuilder.standard().build();

    // DynamoDB table attribute name for storing info title.
    private static final String INFO_TABLE = TableLathamInfo.TABLE_NAME;

    // DynamoDB table attribute name for storing info title.
    private static final String INFO_TABLE_TITLE = "title";

	@Override
	public ServerlessOutput handleRequest(ServerlessInput serverlessInput, Context context) {
        ServerlessOutput output = new ServerlessOutput();
        
        try {            
            String title = "";
            if (serverlessInput.getQueryStringParameters() != null && serverlessInput.getQueryStringParameters().get(INFO_TABLE_TITLE) != null) {
                    title = serverlessInput.getQueryStringParameters().get(INFO_TABLE_TITLE);
            }
            

            String projExp = "id,title,info";
            ScanRequest infoRequest = new ScanRequest().withTableName(INFO_TABLE).withProjectionExpression(projExp);
            ScanResult result = client.scan(infoRequest);
            
            String content = "";
            for (Map<String, AttributeValue> item : result.getItems()) {
            	if (!title.isEmpty()) {
            		AttributeValue av = item.get("title");
            		String titleVal = av.getS();
            		if (!titleVal.toLowerCase().contains(title.toLowerCase())) {
            			continue;
            		}
            	}
            	TableLathamInfo tblItem = new TableLathamInfo();
            	tblItem.setId(item.get("id").getS());
            	tblItem.setTitle(item.get("title").getS());
            	tblItem.setInfo(item.get("info").getS());
            	if (content.isEmpty()) {
            		content += tblItem.toString();
            	} else {
                    content += "," + tblItem.toString();
            	}
            }
            
        	String body = "{\"items\" : [" + content + "]}";            
            output.setStatusCode(200);
            output.setBody(body);
        } catch (Exception e) {
            output.setStatusCode(500);
            StringWriter sw = new StringWriter();
            e.printStackTrace(new PrintWriter(sw));
            output.setBody(sw.toString());
        }

        return output;
	}

}
