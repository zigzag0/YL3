package ee.ttu.idu0080.raamatupood.client;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

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
import ee.ttu.idu0080.raamatupood.types.Tellimus;
import ee.ttu.idu0080.raamatupood.types.TellimuseRida;
import ee.ttu.idu0080.raamatupood.types.Toode;

//JMS sõnumite tarbija. ühendub broker-i urlile

public class Raamatupood {
	private static final Logger log = Logger.getLogger(Raamatupood.class);
	private String SUBJECT = "Tekstide.saatmine";
	private String SUBJECT2 = "Tellimuse.vastus";
	private String user = ActiveMQConnection.DEFAULT_USER;
	private String password = ActiveMQConnection.DEFAULT_PASSWORD;
	private String url = EmbeddedBroker.URL;

	long sleepTime = 1000;
	private long timeToLive = 1000000;

	public static void main(String[] args) {
		Raamatupood consumerTool = new Raamatupood();
		consumerTool.run();
	}

	public void run() {
		Connection connection = null;
		try {
			log.info("Connecting to URL: " + url);
			log.info("Consuming queue : " + SUBJECT);

			// 1. Loome ühenduse
			ActiveMQConnectionFactory connectionFactory = new ActiveMQConnectionFactory(
					user, password, url);
			connection = connectionFactory.createConnection();

			// Kui ühendus kaob, lõpetatakse Consumeri töö veateatega.
			connection.setExceptionListener(new ExceptionListenerImpl());

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

			// 3. Teadete vastuvõtja
			MessageConsumer consumer = session.createConsumer(destination);

			// Kui teade vastu võetakse käivitatakse onMessage()
			consumer.setMessageListener(new MessageListenerImpl());

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void sendAnswer(int kogus, BigDecimal summa) {

		Connection connection = null;
		int TotalKogus = kogus;
		BigDecimal kogusumma = summa;
		try {
			log.info("Consuming queue : " + SUBJECT2);
			// 1. Loome ühenduse
			ActiveMQConnectionFactory connectionFactory = new ActiveMQConnectionFactory(
					user, password, url);
			connection = connectionFactory.createConnection();
			// Käivitame yhenduse
			connection.start();

			// 2. Loome sessiooni
			/*
			 * createSession võtab 2 argumenti: 1. kas saame kasutada
			 * transaktsioone 2. automaatne kinnitamine
			 */
			Session session = connection.createSession(false,
					Session.AUTO_ACKNOWLEDGE);

			// Loome teadete sihtkoha (järjekorra). Parameetriks järjekorra nimi
			Destination destination = session.createQueue(SUBJECT2);

			// 3. Loome teadete saatja
			MessageProducer producer = session.createProducer(destination);

			// producer.setDeliveryMode(DeliveryMode.PERSISTENT);
			producer.setTimeToLive(timeToLive);

			// 4. teadete saatmine
			answer(session, producer, TotalKogus, kogusumma);

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	protected void answer(Session session, MessageProducer producer, int kogus,
			BigDecimal summa) throws Exception {

		TextMessage message = session.createTextMessage("Todete kogus: "
				+ kogus + ", summa: " + summa + ". Sent at: "
				+ (new Date()).toString());
		log.debug("Sending message: " + message.getText());
		producer.send(message);

		// ootab 1 sekundi
		Thread.sleep(sleepTime);
	}

	/**
	 * Käivitatakse, kui tuleb sõnum
	 */
	class MessageListenerImpl implements javax.jms.MessageListener {

		public void onMessage(Message message) { // sõnumi töötlus
			/*
			 * try { if (message instanceof TextMessage) { TextMessage txtMsg =
			 * (TextMessage) message; String msg = txtMsg.getText();
			 * log.info("Received: " + msg); } else if (message instanceof
			 * ObjectMessage) { ObjectMessage objectMessage = (ObjectMessage)
			 * message; String msg = objectMessage.getObject().toString();
			 * log.info("Received: " + msg);
			 */

			BigDecimal kogusumma = new BigDecimal(0);
			int totalKogus = 0;
			if (message instanceof ObjectMessage) {
				ObjectMessage tellimusObject = (ObjectMessage) message;
				Tellimus tellimus = null;
				try {
					tellimus = (Tellimus) tellimusObject.getObject();
				} catch (JMSException e) {

					log.info(e);
				}
				List<TellimuseRida> tellimuseRead = tellimus.getTellimuseRead();
				for (int i = 0; i < tellimuseRead.size(); i++) {
					Toode toode = tellimuseRead.get(i).getToode();
					long kogus = tellimuseRead.get(i).getKogus();
					String nimetus = toode.getNimetus();
					int kood = toode.getKood();
					BigDecimal hind = toode.getHind();
					kogusumma = kogusumma.add(hind.multiply(new BigDecimal(
							kogus)));
					totalKogus += kogus;
					log.info("\n Saadud raamat: " + nimetus + "\n Kogus: "
							+ kogus + "\n id: " + kood + "\n hind: " + hind);
				}

			} else {
				log.info("Received: " + message); // log.info("Saadud informatsioon ei ole tellimus!");
			}
			/*
			 * } catch (JMSException e) { log.warn("Caught: " + e);
			 * e.printStackTrace(); } } }
			 */

			sendAnswer(totalKogus, kogusumma);
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
}