package heartbeat;

import javax.naming.*;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import javax.jms.*; 

//Implementing messageListener to read WeatherRadar heartbeat

public class FaultDetectionSystem implements MessageListener {
	
	private TopicConnectionFactory tcf;
	private TopicConnection tcon;
	private TopicSession subSession;
	private String topicName = "jms/WeatherRadarTopic";
	private Topic top;
	private TopicSubscriber tSub;
	//public static int n = 1;
	public boolean insideMsg = false;
	public String received = "";
	
	
	//Constructor
	public FaultDetectionSystem () throws NamingException,JMSException
	{

	    InitialContext initialCon = new InitialContext();
	    
	    // Lookup the Connection Factory and the Topic
	    tcf = (TopicConnectionFactory)
	    		initialCon.lookup("jms/WeatherRadarConnection");
	    top = (Topic)initialCon.lookup(topicName);

	    // Create a connection using the Factory
	    tcon = tcf.createTopicConnection();

	    //Create Topic Sessions using the connection
	    subSession = tcon.createTopicSession
	                (false,Session.AUTO_ACKNOWLEDGE);

	    //Create TopicPublisher
	    tSub = subSession.createSubscriber(top);

	    //  Associating a MessageListener to this subscriber
	    tSub.setMessageListener(this);

	    tcon.start();
		
	}

	//Message parsing from Topic
	@Override
	public void onMessage(Message msg) {
		 //n++;
		 insideMsg = true;
		 //System.out.println(n);
	      try {
	          //  Extracting the TextMessage object from the Message
	          TextMessage txtMsg = (TextMessage)msg;

	          //  From the TextMessage object, extract the message
	          String text = txtMsg.getText();
	          received = text;
	          /*  
	           *  If "exit" detected on console close connection and exit
	           */
	          if(!text.equalsIgnoreCase("exit")) {
	                System.out.println("FDS received: "+text);
	          } else {
	                System.out.println("Good");
	          }
	      } catch(JMSException je) {
	    	  System.out.println("Exception");
	            je.printStackTrace();
	      }
	}
	
	   //   Exit module where in connection object is closed
	   public void exit() {
	      try {
	          tcon.close();
	      } catch(JMSException je) {
	          System.out.println("JMSExceptionred");
	      }
	      System.exit(0);
	   }
	   
	   public static void main(String[] args) 
		          throws NamingException,JMSException, InterruptedException, IOException {
		    //String username = null, password = null;
			//BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
//			if(args.length == 2) {
//			    username = args[0];
//			    password = args[1];
//			} else {
//			    System.out.println("No parameters");
//			    System.exit(0);
//			}

		    // create the FaultDetectionSystem object as Subscriber
			FaultDetectionSystem sub = new FaultDetectionSystem();
			
			//Fault Detection mechanism
			//int i;
			//int temp = 0;
			//temp = sub.n;
			//sub.insideMsg = true;
			boolean b = true;
			boolean a = false;
			WeatherRadar.main(args);
			WeatherRadar.sendHeartBeat();
			while(true)
			{
				//i = sub.n;
				//Delay of 7 seconds
				Thread.sleep(7000);
				//System.out.println(i);
				if(sub.insideMsg)
				{
					if(sub.received == "Process 1 is alive")
					{
						System.out.println("");
					}else {
						if(b==true)
						{
							WeatherRadarBackup.sendHeartBeat();
							b = false;
						}
						if(a==false)
						{
							WeatherRadar.main(args);
							a = true;
						}
					
						if(sub.received=="Process 2 is alive")
						{
							System.out.println("");
						}
						else{
							if(a==true)
							{
								a = false;
								//WeatherRadar.main(args);
								WeatherRadar.sendHeartBeat();
								
							}
							if(b==false)
							{
								WeatherRadarBackup.main(args);
								b = true;
							}
						}
					}		
				}else{
					System.out.println("Both processes are dead");
				}
			}
		    }//End of main
}// End of class
