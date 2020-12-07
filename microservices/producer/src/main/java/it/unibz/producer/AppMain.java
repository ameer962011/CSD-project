package it.unibz.producer;

import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.DeliverCallback;

public class AppMain {
	private final static String QUEUE_NAME = "db";
    private static final String QUEUE_TO_CONSUME = "producer";

	//public static void main(String[] args) throws Exception {
		//System.out.println("Hello, world");
    
	public void run() throws Exception {
		ConnectionFactory factory = new ConnectionFactory();
		factory.setHost("rabbitmq");
		
		waitForResults(factory);
        sendMessage(factory);  
	}
		/*try (Connection connection = factory.newConnection(); Channel channel = connection.createChannel()) {

			channel.queueDeclare(QUEUE_NAME, false, false, false, null);
			String message = "send data";
			channel.basicPublish("", QUEUE_NAME, null, message.getBytes());
			System.out.println(" [x] Sent '" + message + "'");

		}*/
        private void waitForResults(ConnectionFactory factory) throws Exception {
            Connection connectionToConsume = factory.newConnection();
            Channel channelToConsume = connectionToConsume.createChannel();
            channelToConsume.queueDeclare(QUEUE_TO_CONSUME, false, false, false, null);
            
            DeliverCallback deliverCallback = (consumerTag, delivery) -> {
                String response = new String(delivery.getBody(), "UTF-8");
                System.out.println("Received '" + response);
            };

            System.out.println("Waiting for messages. To exit press CTRL+C");
            channelToConsume.basicConsume(QUEUE_TO_CONSUME, true, deliverCallback, consumerTag -> {
            });
        }

        private void sendMessage(ConnectionFactory factory) throws Exception {
            Connection connectionToPublish = factory.newConnection();
            Channel channelToPublish = connectionToPublish.createChannel();
            channelToPublish.queueDeclare(QUEUE_NAME, false, false, false, null);

            String message = "send data";

            channelToPublish.basicPublish("", QUEUE_NAME,
                    new AMQP.BasicProperties.Builder().replyTo(QUEUE_TO_CONSUME).build(), message.getBytes());

            System.out.println("Sent '" + message + "'");
        }

        public static void main(String[] args) throws Exception {
            System.out.println("Starting producer...");

            AppMain a = new AppMain();
            a.run();
	}

}
