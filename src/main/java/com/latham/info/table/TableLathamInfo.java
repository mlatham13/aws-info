package com.latham.info.table;

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBAttribute;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBHashKey;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBTable;
import com.amazonaws.util.json.Jackson;

@DynamoDBTable(tableName="LathamInfo")
public class TableLathamInfo {
    public static final String TABLE_NAME = "LathamInfo";
    
    private String id;
    private String title;
    private String info;
    
    @DynamoDBHashKey(attributeName="id")  
    public String getId() { return id;}
    public void setId(String id) {this.id = id;}
    
    @DynamoDBAttribute(attributeName="title")  
    public String getTitle() {return title; }
    public void setTitle(String title) { this.title = title; }
    
    @DynamoDBAttribute(attributeName="info")  
    public String getInfo() {return info; }
    public void setInfo(String info) { this.info = info; }
    
    @Override
    public String toString() {return Jackson.toJsonString(this);}

}