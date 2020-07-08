package gw.ws.server;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpSession;
import javax.websocket.EndpointConfig;
import javax.websocket.OnClose;
import javax.websocket.OnError;
import javax.websocket.OnMessage;
import javax.websocket.OnOpen;
import javax.websocket.Session;
import javax.websocket.server.PathParam;
import javax.websocket.server.ServerEndpoint;

import org.apache.log4j.Logger;
import org.apache.tomcat.websocket.WsSession;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

import gw.ssh.SSHSession;
import gw.utils.BaseTool;
import gw.web.GeoweaverController;

/**
 * 
 * This works and will be used as the only websocket channel for transferring all the SSH related message
 * 
 * @author JensenSun
 *
 */
//ws://localhost:8080/geoweaver-shell-socket
@ServerEndpoint(value = "/command-socket")
public class CommandServlet {
	
	Logger logger = Logger.getLogger(CommandServlet.class);
	
	private Session wsSession;
	
	private List<String> logoutCommands = Arrays.asList(new String[]{"logout", "quit"});
    
    static Map<String, Session> peers =new HashMap();
	
//    private HttpSession httpSession;
	
	@OnOpen
    public void open(Session session, EndpointConfig config) {
		
		try {
			
			logger.info("websocket channel openned");
			
			this.wsSession = session;
			
			WsSession wss = (WsSession) session;
			
			System.out.println("Web Socket Session ID:" + wss.getHttpSessionId());
			
			peers.put(wss.getHttpSessionId(), session);
			
		} catch (Exception e) {
			
			e.printStackTrace();
			
		}
		
    }

    @OnError
    public void error(final Session session, final Throwable throwable) throws Throwable {
        
    	logger.error("websocket channel error" + throwable.getLocalizedMessage());
    	
    	throw throwable;
    	
    }

    @OnMessage
    public void echo(String message, Session session) {
        
    	try {
    		
			logger.info("Received message: " + message);
        	
        	logger.info(" Session ID: " + session.getQueryString());
        	
        	logger.info("Transfer message to Jupyter Notebook server..");
        	
//        	session.getBasicRemote().sendText("Message received and Geoweaver Shell Socket Send back: " + message);
        	
            SSHSession sshSession = GeoweaverController.sshSessionManager.sshSessionByToken.get(session.getId());
            
            if (sshSession == null) {
                
            	logger.info("linking " + session.getId() + message);
                
                // TODO is there a better way to do this?
                // Can the client send the websocket session id and username in a REST call to link them up?
                sshSession = GeoweaverController.sshSessionManager.sshSessionByToken.get(message);
                
//                if(sshSession!=null&&sshSession.getSSHInput().ready()) {
                if(sshSession!=null) {
                	
//                	sshSession.setWebSocketSession(session);
                    
                	GeoweaverController.sshSessionManager.sshSessionByToken.put(session.getId(), sshSession);
                	
//                	GeoweaverController.sshSessionManager.sshSessionByToken.remove(messageText); //remove session, a token can only be used once
                    
                }else {
                	
                	session.getBasicRemote().sendText("No SSH connection is active");
                	
//                	session.close();
                	
                }
                
            } else {
            	
                logger.debug("message in " + session.getId() + message);
                
                sshSession.getSSHOutput().write((message + '\n').getBytes());
                
                sshSession.getSSHOutput().flush();
                
//    			//send Ctrl + C command to the SSH to close the connection
//    			
//    			cmd.getOutputStream().write(3);
//    			
//    		    cmd.getOutputStream().flush();
                
                // if we receive a valid logout command, then close the websocket session.
                // the system will logout and tidy itself up...
                
                if (logoutCommands.contains(message.trim().toLowerCase())) {
                    
                	logger.info("valid logout command received " +  message);
                	
                	sshSession.logout();
                	
//                	session.close(); //close WebSocket session. Notice: the SSHSession will continue to run.
                	
                }
                
            }
            
        	
//        	SSHSession sshSession = GeoweaverController.sshSessionManager.sessionsByWebsocketID.get(session.getId());
//            
//            if (sshSession == null) {
//                
//            	logger.info("linking {}:{}" +  session.getId() + message);
//                
//                // TODO is there a better way to do this?
//                // Can the client send the websocket session id and username in a REST call to link them up?
//                sshSession = GeoweaverController.sshSessionManager.sshSessionByToken.get(message);
//                
////                if(sshSession!=null&&sshSession.getSSHInput().ready()) {
//                if(sshSession!=null) {
//                	
////                	sshSession.setWebSocketSession(session);
//                    
//                	GeoweaverController.sshSessionManager.sessionsByWebsocketID.put(session.getId(), sshSession);
//                	
////                	GeoweaverController.sshSessionManager.sshSessionByToken.remove(messageText); //remove session, a token can only be used once
//                    
//                }else {
//                	
////                	session.sendMessage(new TextMessage("No SSH connection is active"));
//                	
//                	session.close();
//                	
//                }
//                
//            } else {
//            	
//            	logger.debug("message in {}:{}" + session.getId() + message);
//                
//                sshSession.getSSHOutput().write((message + '\n').getBytes());
//                
//                sshSession.getSSHOutput().flush();
//                
////    			//send Ctrl + C command to the SSH to close the connection
////    			
////    			cmd.getOutputStream().write(3);
////    			
////    		    cmd.getOutputStream().flush();
//                
//                // if we receive a valid logout command, then close the websocket session.
//                // the system will logout and tidy itself up...
//                
//                if (logoutCommands.contains(message.trim().toLowerCase())) {
//                    
//                	logger.info("valid logout command received: {}" + message);
//                	
//                	sshSession.logout();
//                	
//                	session.close(); //close WebSocket session. Notice: the SSHSession will continue to run.
//                	
//                }
//                
//            }
            
        	
    	}catch(Exception e) {
    		
    		e.printStackTrace();
    		
    	}
    	
    }

    @OnClose
    public void close(final Session session) {
    	
		try {
			
    		logger.error("Geoweaver Shell Channel closed.");
    		
    		logger.debug("websocket session closed:" + session.getId());
    		
            //close SSH session
            if(GeoweaverController.sshSessionManager!=null) {
            	
            	SSHSession sshSession = GeoweaverController.sshSessionManager.sshSessionByToken.get(session.getId());
                if (sshSession != null && sshSession.isTerminal()) { //only close when it is shell
                    sshSession.logout();
                }
                GeoweaverController.sshSessionManager.sshSessionByToken.remove(session.getId());
            	
            }
            peers.remove(session.getId());
        	
		} catch (Exception e) {
			
			e.printStackTrace();
			
		}
    	
    }
    
    
    public static javax.websocket.Session findSessionById(String sessionid) {
        if (peers.containsKey(sessionid)) {
            return peers.get(sessionid);
        }
        return null;
    }

}
