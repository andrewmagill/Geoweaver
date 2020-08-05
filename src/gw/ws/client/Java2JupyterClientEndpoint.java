package gw.ws.client;

import java.io.IOException;
import java.net.URI;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.websocket.ClientEndpoint;
import javax.websocket.ClientEndpointConfig;
import javax.websocket.ClientEndpointConfig.Builder;
import javax.websocket.ClientEndpointConfig.Configurator;
import javax.websocket.CloseReason;
import javax.websocket.ContainerProvider;
import javax.websocket.Endpoint;
import javax.websocket.EndpointConfig;
import javax.websocket.HandshakeResponse;
import javax.websocket.MessageHandler;
import javax.websocket.OnClose;
import javax.websocket.OnMessage;
import javax.websocket.OnOpen;
import javax.websocket.Session;
import javax.websocket.WebSocketContainer;

import org.apache.log4j.Logger;

import gw.utils.BaseTool;

/**
 * 
 * @author JensenSun
 *
 */
//@ClientEndpoint
public class Java2JupyterClientEndpoint extends Endpoint 
{

	Session newjupyteression = null;
	
    private Logger logger = Logger.getLogger(this.getClass());
    
    private Java2JupyterClientDialog window;
    

    public Java2JupyterClientEndpoint(URI endpointURI, Session jssession, Map<String, List<String>> headers) {
    	
        try {
        	
        	this.newjupyteression = jssession;
            
        	WebSocketContainer container = ContainerProvider.getWebSocketContainer();
        	
        	//build ClientEndpointConfig
            Builder configBuilder = ClientEndpointConfig.Builder.create();
            
            configBuilder.configurator(new Configurator() {
            	
                @Override
                public void beforeRequest(Map<String, List<String>> nativeheaders) {
//                	headers.put("Cookie", Arrays.asList("JSESSIONID=" + sessionID));
                	
                	Map<String, List<String>> uppercaseheaders = new HashMap();
                	
                	Iterator hmIterator = nativeheaders.entrySet().iterator(); 
                	
                    while (hmIterator.hasNext()) {
                        Map.Entry<String, List<String>> mapElement = (Map.Entry)hmIterator.next(); 
                        
                        String newkey = mapElement.getKey();
                        
                        newkey = newkey.substring(0, 1).toUpperCase() + newkey.substring(1);
                        
                        List<String> values = mapElement.getValue();
                        
                        uppercaseheaders.put(newkey, values);
                        
                    } 
                    
                	nativeheaders.putAll(uppercaseheaders);
                	
                	logger.info("Native Headers Loggout: " + nativeheaders);
                	
                }
                
            });
            
            ClientEndpointConfig clientConfig = configBuilder.build();
            
//            if(!BaseTool.isNull(headers)) {
//            	
//            	Iterator<String> itr = headers.keySet().iterator();
//                
//                while (itr.hasNext())
//                {
//                	String key = itr.next();
//                	
//                	List<String> value = headers.get(key);
//
//                	System.out.println(key + "=" + value);
//                	
//                	clientConfig.getUserProperties().put(key, value.get(0));
//                	
//                }
//            	
//            }
            
//            ClientEndpointConfig.Configurator configurator = new ClientEndpointConfig.Configurator() {
//                public void beforeRequest(Map> headers) {
//                    headers.put("Authorization", asList("Basic " + DatatypeConverter.printBase64Binary("user:password".getBytes())));
//                }
//            };
//            ClientEndpointConfig clientConfig = ClientEndpointConfig.Builder.create()
//                    .configurator(configurator)
//                    .build();
            
//            clientConfig.getConfigurator().beforeRequest(headers);
//            clientConfig.getUserProperties().put("Sec-WebSocket-Extensions", "permessage-deflate; client_max_window_bits");
//            clientConfig.getUserProperties().put("Sec-WebSocket-Key", "JNOrKMA6YhDCRijp46/ofg==");
//            clientConfig.getUserProperties().put("Sec-WebSocket-Version", "13");
            
//            Sec-WebSocket-Extensions: permessage-deflate; client_max_window_bits
//            Sec-WebSocket-Key: JNOrKMA6YhDCRijp46/ofg==
//            Sec-WebSocket-Version: 13
            
            
            
            container.connectToServer(this, clientConfig, endpointURI);
            
            logger.info("The connection to Jupyter server is built");
//            container.connectToServer(this, endpointURI);
            
//            Session newjupytersession = container.connectToServer(this,  endpointURI);
            
//            newjupytersession.getAsyncRemote().sendText("sdfds");
            
        } catch (Exception e) {
        	
        	e.printStackTrace();
            throw new RuntimeException(e);
            
        }
        
    }
    
    @Override
	public void onOpen(Session session, EndpointConfig config) {
    	
    	logger.info("Override The connection between Java and Jupyter server is established.");
    	
        this.newjupyteression = session;
        
        this.newjupyteression.addMessageHandler(new MessageHandler.Whole<String>() {
            @Override
            public void onMessage(String message) {
            	
                try {
                	
                	logger.info("Received message from remote Jupyter server: " + message);
                	
                	logger.info("send this message back to the client");
                	
//                	if(!BaseTool.isNull(this.newjupyteression)) this.newjupyteression.getAsyncRemote().sendText(message);
                	
                	if(!BaseTool.isNull(window)) {
                		
                		window.writeServerMessage(message);
                		
                	}
//                    session.getBasicRemote().sendText("Got message from " + session.getId() + "\n" + message);
                	
                } catch (Exception ex) {
                	logger.error("Fail to parse the returned message from Jupyter server" + ex.getLocalizedMessage());
                }
            }
        });
	}
    
    @Override
    public void onClose(Session session, CloseReason closeReason) {
		try {
			
	        logger.info("Peer " + session.getId() + " disconnected due to " + closeReason.getReasonPhrase());
	        this.newjupyteression = null;
	    	logger.info("The connection between Javascript and Geoweaver is closed. ");
			
			if(!BaseTool.isNull(this.newjupyteression))this.newjupyteression.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    }
    
    
    
    @Override
    public void onError(Session session, Throwable error) {
        logger.info("Error communicating with peer " + session.getId() + ". Detail: "+ error.getMessage());
    }
    
    public Session getNewjupyteression() {
		return newjupyteression;
	}


    /**
     * Send a message.
     * 
     * @param user
     * @param message
     */
    public void sendMessage(String message) {
        this.newjupyteression.getAsyncRemote().sendText(message);
    }



	public void setNewjupyteression(Session newjupyteression) {
		this.newjupyteression = newjupyteression;
	}



	public Java2JupyterClientDialog getWindow() {
		return window;
	}



	public void setWindow(Java2JupyterClientDialog window) {
		this.window = window;
	}
	
//	/**
//     * Callback hook for Connection open events.
//     * 
//     * @param userSession
//     *            the userSession which is opened.
//     */
//    @OnOpen
//    public void onOpen(Session userSession) {
//    	logger.info("Annotation The connection between Java and Jupyter server is established.");
//        this.newjupyteression = userSession;
//    }
	
	
    

//    /**
//     * Callback hook for Connection close events.
//     * 
//     * @param userSession
//     *            the userSession which is getting closed.
//     * @param reason
//     *            the reason for connection close
//     */
//    @OnClose
//    public void onClose(Session userSession, CloseReason reason) {
//        try {
//        	logger.info("The connection between Java and Jupyter is closed.");
//        	this.newjupyteression = null;
//        	logger.info("The connection between Javascript and Geoweaver is closed. ");
//			if(!BaseTool.isNull(this.newjupyteression)) this.newjupyteression.close();
//		} catch (IOException e) {
//			e.printStackTrace();
//		}
//    }
//
//    /**
//     * Callback hook for Message Events. This method will be invoked when a
//     * client send a message.
//     * 
//     * @param message
//     *            The text message
//     */
//    @OnMessage
//    public void onMessage(String message) {
//    	logger.info("Received message from remote Jupyter server: " + message);
//    	logger.info("send this message back to the client");
////    	if(!BaseTool.isNull(this.newjupyteression)) this.newjupyteression.getAsyncRemote().sendText(message);
//    	if(!BaseTool.isNull(window)) {
//    		
//    		window.writeServerMessage(message);
//    		
//    	}
//    	
//    }



	

	
}
