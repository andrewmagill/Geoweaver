package com.gw.tasks;

import java.io.IOException;
import java.util.Date;
import java.util.List;
import java.util.Map;

import javax.websocket.Session;

import org.apache.log4j.Logger;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

import com.gw.database.DataBaseOperation;
import com.gw.ssh.SSHSession;
import com.gw.ssh.SSHSessionImpl;
import com.gw.tools.HistoryTool;
import com.gw.tools.HostTool;
import com.gw.tools.ProcessTool;
import com.gw.jpa.History;
import com.gw.jpa.Workflow;
import com.gw.tools.WorkflowTool;
import com.gw.utils.BaseTool;
import com.gw.utils.RandomString;
import com.gw.utils.STATUS;
import com.gw.web.GeoweaverController;

@Service
public class GeoweaverWorkflowTask extends Task {
	
	Logger log = Logger.getLogger(this.getClass());
	
	@Autowired
	ProcessTool pt;
	
	@Autowired
	BaseTool bt;
	
	@Autowired
	HistoryTool hist;
	
	@Autowired
	WorkflowTool wt;
	
	String name;
	
	String wid, mode;
	
	String[] hosts;
	
	String[] pswds;
	
	String token;
	
	Session monitor = null;
	
	/**********************************************/
    /** section of the geoweaver history records **/
    /**********************************************/
	
//	Map 			 pid2hid = new HashMap();
	
    String			 history_input;
    
    String			 history_output;
    
    Date			 history_begin_time;
    
    Date			 history_end_time;
    
    String			 history_process;
    
    String			 history_id;
    
    /**********************************************/
    /** end of history section **/
    /**********************************************/
    
    public GeoweaverWorkflowTask() {
    	
    	//for spring
    	
    }
	
	public GeoweaverWorkflowTask(String name) {
		
		this.name = name;
		
		this.history_id = new RandomString(11).nextString();
		
	}
	
	public String getHistory_id() {
		return history_id;
	}
	


	@Override
	public void initialize() {
//		setChanged();
//		notifyObservers(this);
	}
	
	public void initialize(String wid, String mode, String[] hosts, String[] pswds, String token) {
		
		this.wid = wid;
		
		this.mode = mode;
		
		this.hosts = hosts;
		
		this.pswds = pswds;
		
		this.token = token;
		
		this.name = "GW-Workflow-Run-" + token;
		
		this.history_id = new RandomString(11).nextString();
		
	}
	
	
	public void saveWorkflowHistory() {
		
		this.history_end_time = bt.getCurrentSQLDate();
		
		this.history_end_time = bt.getCurrentSQLDate();
		
		History history = hist.getHistoryById(this.history_id);
		
		history.setHistory_begin_time(this.history_begin_time);
		
		history.setHistory_end_time(this.history_end_time);
		
		history.setHistory_process(this.history_process);
		
		history.setHistory_input(this.history_input);
		
		history.setHistory_output(this.history_output);
		
		history.setHost_id(bt.array2String(hosts, ";"));
		
		hist.saveHistory(history);
    	
//    	StringBuffer sql = new StringBuffer("insert into history (id, process, begin_time, end_time, input, output, host) values ('");
//    	
//    	sql.append(this.history_id).append("','");
//    	
//    	sql.append(this.history_process).append("','");
//    	
//    	sql.append(this.history_begin_time).append("','");
//    	
//    	sql.append(this.history_end_time).append("',?, ?,'");
//    	
//    	sql.append(bt.array2String(hosts, ";")).append("' )");
//    	
//    	DataBaseOperation.preexecute(sql.toString(), new String[] {this.history_input, this.history_output});
		
	}
	
	/**
	 * Start the monitoring of the task
	 * @param socketsession
	 */
	public void startMonitor(Session socketsession) {
		
		monitor = socketsession;
		
		wt.token2ws.put(token, socketsession.getId());
		
	}
	
	/**
	 * Send status message back to websocket end
	 * @param nodes
	 * @param flags
	 */
	public void sendStatus(JSONArray nodes, STATUS[] flags) {
		
		try {
			
			if(monitor!=null) {
				
				JSONArray array = new JSONArray();
				
				for(int i=0;i<nodes.size();i++) {
					
					String id = (String)((JSONObject)nodes.get(i)).get("id");
					
					JSONObject obj = new JSONObject();
					
					obj.put("id", id);
					
					obj.put("status", flags[i].toString());
					
					array.add(obj);
					
				}
				
//				monitor.sendMessage(new TextMessage(array.toJSONString()));
				monitor.getAsyncRemote().sendText(array.toJSONString());
				
			}
			
		} catch (Exception e) {

			e.printStackTrace();
			
		}
		
	}
	
	/**
	 * Stop the monitoring of the task
	 */
	public void stopMonitor() {
		
		try {
			log.info("close the websocket session from server side");
			
			if(!bt.isNull(monitor))
				monitor.close();
			
			wt.token2ws.remove(token);
			
		} catch (IOException e) {
			
			e.printStackTrace();
			
		}
		
	}

	@Override
	public void execute() {
		// TODO Auto-generated method stub
		
//		hid = new RandomString(15).nextString();
		
		log.debug(" + + + start Geoweaver workflow " + wid );
		
		try {
			
			//get the nodes and edges of the workflows
			
			this.history_process = wid;
			
			this.history_begin_time = bt.getCurrentSQLDate();
			
			this.history_input = "";
			
			this.history_output = "";
			
			Workflow w = wt.getById(wid);
			
			if(bt.isNull(w))
				throw new RuntimeException("no workflow is found");
			
			//execute the process in a while loop - for now. Improve this in future
			
			int executed_process = 0;
			
			JSONParser parser = new JSONParser();
			
			JSONArray edges = (JSONArray)parser.parse(w.getEdges());
			
			JSONArray nodes = (JSONArray)parser.parse(w.getNodes());
			
			STATUS[] flags = new STATUS[nodes.size()];
			
			for(int i=0;i<flags.length; i++ ) {
				
				flags [i] = STATUS.READY;
				
			}
			
			Map<String, List> node2condition = wt.getNodeConditionMap(nodes, edges);
			
			while(executed_process < (nodes.size())) {
				
				//find next process to execute - the id has two parts: process type id - process object id
				
				String[] idnum = wt.findNextProcess(node2condition, flags, nodes);
				
				String nextid = idnum[0];
				
				log.debug("this round is : " + nextid);
				
				STATUS stat = STATUS.RUNNING;
				
				wt.updateNodeStatus(nextid, flags, nodes, stat);
				
				sendStatus(nodes, flags);
				
				String processTypeId = nextid.split("-")[0];
				
				int num = Integer.parseInt(idnum[1]);
				
				String hid = mode.equals("one")?hosts[0]:hosts[num];
				
				String password = mode.equals("one")?pswds[0]:pswds[num];
				
				//nodes
//				[{"title":"download-landsat","id":"nhi96d-7VZhh","x":119,"y":279},{"title":"filter_cloud","id":"rh1u8q-4sCmg","x":286,"y":148},{"title":"filter_shadow","id":"rpnhlg-JZfyQ","x":455,"y":282},{"title":"match_cdl_landsat","id":"omop8l-1p5x1","x":624,"y":152}]
				
				//edges
//				[{"source":{"title":"sleep5s","id":"ac4724-jL0Ep","x":342.67081451416016,"y":268.8715720176697},"target":{"title":"testbash","id":"199vsg-Xr6FZ","x":465.2892303466797,"y":41.6651611328125}},{"source":{"title":"testbash","id":"199vsg-oAq2d","x":-7.481706619262695,"y":180.70700073242188},"target":{"title":"sleep5s","id":"ac4724-jL0Ep","x":342.67081451416016,"y":268.8715720176697}}]
				
				//get code of the process
				
//				String code = ProcessTool.getCodeById(processTypeId);
//				
//				log.info("Ready to run process : " + processTypeId);
//				
//				log.info(code);
//				
//				//establish SSH session and generate a token for it
//				
//				if(token == null) {
//					
//					token = new RandomString(12).nextString();
//					
//				}
//				
//				//If the mode is one, reuse the same SSHSession object for all processes. 
//				//If the mode is different, create new SSHSession object for each process
//				//see if SSHJ allows this operation
//				
//				String historyid = null;
//				
//				try {
//					
//					SSHSession session = new SSHSessionImpl();
//					
//					//get host ip, port, user name and password
//					
////					String[] hostdetails = HostTool.getHostDetailsById(hid);
////					
////					session.login(hostdetails[1], hostdetails[2], hostdetails[3], password, token, false);
//					
//					session.login(hid, password, token, false);
//					
//					GeoweaverController.sshSessionManager.sshSessionByToken.put(token, session);
//					
//					session.runBash(code, nextid, true);  //every task only has no more than one active SSH session at a time
//					
//					historyid = session.getHistory_id();
//					
//					stat = STATUS.DONE;
//					
//				}catch(Exception e) {
//					
//					stat = STATUS.FAILED;
//					
//				}
				
				String member_historyid = null;
				
				try {
					
					String resp = pt.execute(processTypeId, hid, password, token, true, null, null, null); //need update the null to be python environment
					
					JSONObject respobj = (JSONObject)new JSONParser().parse(resp);
					
					member_historyid = (String)respobj.get("history_id");
					
					stat = STATUS.DONE;
					
				}catch(Exception e) {
					
					stat = STATUS.FAILED;
					
					e.printStackTrace();
				}
				
				this.history_input += nextid + ";";
				
				this.history_output += member_historyid + ";";
				
//				pid2hid.put(nextid, historyid); //save the mapping between process id and history id
				
				wt.updateNodeStatus(nextid, flags, nodes, stat); //once the process is finished, updated its status
				
				executed_process++;
				
			}
			
			sendStatus(nodes, flags); //last message
			
			log.info("workflow execution is finished.");
			
			saveWorkflowHistory();
			
		} catch (Exception e) {
			
			e.printStackTrace();
			
		} finally {
			
			GeoweaverController.sessionManager.closeWebSocketByToken(token); //close ssh output transferring websocket at the end
			
			stopMonitor(); //shut down workflow status monitor websocket
			
		}

	}

	@Override
	public void responseCallback() {
		// TODO Auto-generated method stub
		//notify the task list observer
//		setChanged();
//		notifyObservers(this);
	}

	@Override
	public void failureCallback(Exception e) {
		// TODO Auto-generated method stub
		//notify the task list observer
//		setChanged();
//		notifyObservers(this);

	}

	@Override
	public String getName() {
		// TODO Auto-generated method stub
		return name;
	}

}
