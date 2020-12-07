package it.unibz.consumer;
import java.sql.*;
import java.util.ArrayList;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.rabbitmq.client.*;

public class AppMain {
	
	private final static String QUEUE_NAME = "db";
	private final static String url = "jdbc:postgresql://db:5432/animals";
    private final static String user = "admin";
    private final static String password = "mysecretpassword";
   

	  //public static void main(String[] argv) throws Exception {
	  public void run() throws Exception {

	    ConnectionFactory factory = new ConnectionFactory();
	    factory.setHost("rabbitmq");
	    
	    com.rabbitmq.client.Connection connection = factory.newConnection();
	    Channel channel = connection.createChannel();
	    channel.queueDeclare(QUEUE_NAME, false, false, false, null);
	   
	    
	    DeliverCallback deliverCallback = (consumerTag, delivery) -> {
	        String message = new String(delivery.getBody(), "UTF-8");
	        System.out.println(" [x] Received '" + message + "'from'" + delivery.getProperties().getReplyTo());
	        
	        switch (message) {
            case "send data":
                try {
                    String data = connect();
                    String queueToPublish = delivery.getProperties().getReplyTo();

                    com.rabbitmq.client.Connection connectionToPublish = factory.newConnection();
                    Channel channelToPublish = connectionToPublish.createChannel();
                    channelToPublish.queueDeclare(queueToPublish, false, false, false, null);
                    channelToPublish.basicPublish("", queueToPublish, null, data.getBytes());

                    System.out.println("Answered '" + data + "' to " + queueToPublish);
                } catch (Exception ex) {
                    ex.printStackTrace();
                }

                break;
        }
	        
	    };
	    channel.basicConsume(QUEUE_NAME, true, deliverCallback, consumerTag -> { });
	    System.out.println(" [*] Waiting for messages. To exit press CTRL+C");

	  }
	    private String connect() throws SQLException {
	    			java.sql.Connection con = DriverManager.getConnection(url, user, password);
	                 Statement pst = con.createStatement();
	                 ResultSet rs = pst.executeQuery("SELECT * FROM animalstype");
	                 
	    		 ArrayList<Pair<Long, String>> result = new ArrayList<>();
	    		 //System.out.print("ID Name\n");
	             while (rs.next()) {
	            	 result.add(new Pair<Long, String>(rs.getLong(1), rs.getString(2)));
	                // System.out.print(rs.getInt(1));
	                 //System.out.print(": ");
	                 //System.out.println(rs.getString(2));
	             } 	
	             GsonBuilder builder = new GsonBuilder();
	             Gson gson = builder.create();
	             return gson.toJson(result);
	             
	             /*Gson gson = new Gson();
	             String json = gson.toJson(result);
	             return json;*/
	         } /*catch (SQLException e) {
	        	 e.printStackTrace();
	         }*/
	    	
	    
	    public static void main(String[] args) throws Exception {
	        System.out.println("Starting consumer...");

	        AppMain a = new AppMain();
	        a.run();
	    }
}

