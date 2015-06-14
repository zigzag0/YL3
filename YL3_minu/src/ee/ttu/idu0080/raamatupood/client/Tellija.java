package ee.ttu.idu0080.raamatupood.client;

import java.math.BigDecimal;
import javax.jms.Connection;
import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageConsumer;
import javax.jms.MessageProducer;
import javax.jms.ObjectMessage;
import javax.jms.Session;
import javax.jms.TextMessage;

import org.apache.activemq.ActiveMQConnection;
import org.apache.activemq.ActiveMQConnectionFactory;
import org.apache.log4j.Logger;

import ee.ttu.idu0080.raamatupood.server.EmbeddedBroker;
//import ee.ttu.idu0080.raamatupood.types.Car;
import ee.ttu.idu0080.raamatupood.types.Tellimus;
import ee.ttu.idu0080.raamatupood.types.TellimuseRida;
import ee.ttu.idu0080.raamatupood.types.Toode;

// JMS sõnumite tootja. Ühendub brokeri url-ile





public class Tellija {
	private static final Logger log = Logger.getLogger(Tellija.class);
	public static final String SUBJECT = "Tekstide.saatmine"; // järjekorra nimi
	public static final String SUBJECT2 = "Tellimuse.vastus";
	private String user = ActiveMQConnection.DEFAULT_USER;// brokeri jaoks vaja
	private String password = ActiveMQConnection.DEFAULT_PASSWORD;

	long sleepTime = 1000; // 1000ms

	private int messageCount = 10;
	private long timeToLive = 1000000;
	private String url = EmbeddedBroker.URL;	// JMS serveriasukoht

	public static void main(String[] args) {
		Tellija producerTool = new Tellija();
		producerTool.run();		// Sõnumi saatmine
	}

	public void run() {
		Connection connection = null;
		try {
			log.info("Connecting to URL: " + url);
			log.debug("Sleeping between publish " + sleepTime + " ms");
			if (timeToLive != 0) {
				log.debug("Messages time to live " + timeToLive + " ms");
			}

			// 1. Loome Ühenduse
			ActiveMQConnectionFactory connectionFactory = new ActiveMQConnectionFactory(
					user, password, url);
			connection = connectionFactory.createConnection();
			// Käivitame ühenduse
			connection.start();

			// 2. Loome sessiooni
			/*
			 * createSession võtab 2 argumenti: 1. kas saame kasutada
			 * transaktsioone 2. automaatne kinnitamine
			 */
			Session session = connection.createSession(false,
					Session.AUTO_ACKNOWLEDGE);

			// Loome teadete sihtkoha (järjekorra). Parameetriks järjekorra nimi
			Destination destination = session.createQueue(SUBJECT);

			// 3. Loome teadete saatja
			MessageProducer producer = session.createProducer(destination);

			// producer.setDeliveryMode(DeliveryMode.PERSISTENT);
			producer.setTimeToLive(timeToLive);

			// 4. teadete saatmine
			sendTellimus(session, producer);
/*			sendLoop(session, producer);
			TextMessage message = session.createTextMessage("Tere SUVII!");
			log.debug("Saadan sõnumi: " + message.getText());
			producer.send(message);
			*/

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

//	protected void sendLoop(Session session, MessageProducer producer)

	public void getAnswer(Session session){
		try {

			log.info("Consuming queue : " + SUBJECT2);

			// Loome teadete sihtkoha (järjekorra). Parameetriks järjekorra nimi
			Destination destination = session.createQueue(SUBJECT2);

			// 3. Teadete vastuvõtja
			MessageConsumer consumer = session.createConsumer(destination);

			// Kui teade vastu võetakse käivitatakse onMessage()
			consumer.setMessageListener(new MessageListenerImpl());

		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	/**
	 * Käivitatakse, kui tuleb sõnum
	 */
	class MessageListenerImpl implements javax.jms.MessageListener {

		public void onMessage(Message message) {
			try {
				if (message instanceof TextMessage) {
					TextMessage txtMsg = (TextMessage) message;
					String msg = txtMsg.getText();
					log.info("Received: " + msg);
				} else if (message instanceof ObjectMessage) {
					ObjectMessage objectMessage = (ObjectMessage) message;
					String msg = objectMessage.getObject().toString();
					log.info("Received: " + msg);

				} else {
					log.info("Received: " + message);
				}

			} catch (JMSException e) {
				log.warn("Caught: " + e);
				e.printStackTrace();
			}
		}
	}
	/**
	 * Käivitatakse, kui tuleb viga.
	 */
	class ExceptionListenerImpl implements javax.jms.ExceptionListener {

		public synchronized void onException(JMSException ex) {
			log.error("JMS Exception occured. Shutting down client.");
			ex.printStackTrace();
		}
	}
	protected void sendTellimus(Session session, MessageProducer producer)
			throws Exception {
/*
		for (int i = 0; i < messageCount || messageCount == 0; i++) {
			ObjectMessage objectMessage = session.createObjectMessage();
			objectMessage.setObject(new Car(5)); // peab olema Serializable
*/

			ObjectMessage objectMessage = session.createObjectMessage();
			BigDecimal hind1 = new BigDecimal("20.65");
			Toode toode1=new Toode(1,"Saunajooga",hind1);
			BigDecimal hind2=new BigDecimal("26.67");
			Toode toode2=new Toode(2,"Looduslik toit. Ehe ja tervendav.",hind2);
			TellimuseRida tellimuserida1=new TellimuseRida(toode1,4);
			TellimuseRida tellimuserida2=new TellimuseRida(toode2,2);
			Tellimus tellimus= new Tellimus();
			tellimus.addTellimuseRida(tellimuserida1);
			tellimus.addTellimuseRida(tellimuserida2);
			objectMessage.setObject(tellimus); // peab olema Serializable
			producer.send(objectMessage);
			log.debug("Saadan tellimuse andmed");
			getAnswer(session);



/*
			TextMessage message = session
					.createTextMessage(createMessageText(i));
			log.debug("Sending message: " + message.getText());
			producer.send(message);
*/
			// ootab 1 sekundi
			Thread.sleep(sleepTime);
		}
	}
/*
	private String createMessageText(int index) {
		return "Message: " + index + " sent at: " + (new Date()).toString();
	}
*/



