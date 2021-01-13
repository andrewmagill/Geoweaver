package com.gw.tasks;

import java.util.ArrayList;
import java.util.List;

import javax.websocket.Session;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.socket.WebSocketSession;

import com.gw.utils.SysDir;
import com.gw.workers.Worker;
import com.gw.workers.WorkerManager;

/**
 *Class TaskManager.java
 *
 *updated on 11/17/2018
 *remove the observer and observable because they are deprecated in the latest JDK (>=9)
 *
 *@author ziheng
 *@time Aug 10, 2015 4:05:28 PM
 */
@Service
public class TaskManager {
	
	private List<Task> waitinglist;
	private List<Task> runninglist;
	private RunningTaskObserver rto;
	private WaitingTaskObserver wto;
	
	Logger logger = Logger.getLogger(this.getClass());
	
	@Autowired
	WorkerManager wm;
	
	@Value("${geoweaver.workernumber}")
	int worknumber;
	
	{
		waitinglist = new ArrayList();
		runninglist = new ArrayList();
//		rto = new RunningTaskObserver();
//		wto = new WaitingTaskObserver();
	}
	/**
	 * Add a new task to the waiting list
	 */
	public void addANewTask(Task t){
//		t.addObserver(wto);
		waitinglist.add(t);
		notifyWaitinglist();
//		t.initialize();
	}
	
	public void runDirectly(Task t) throws InterruptedException {
		
		Worker w = wm.getMustWorker();
		
		w.setTask(t);
		
		w.join(7*24*60*60*1000); // 7 days maximum
		
		wm.removeMustWorker(w);
		
	}
	/**
	 * Execute a task
	 * @param t
	 * @return
	 */
	private boolean executeATask(Task t){
		boolean is = false;
		if(wm.getCurrentWorkerNumber()<worknumber){
//			t.addObserver(rto);
			wm.createANewWorker(t);
			runninglist.add(t);
			is = true;
		}else{
			logger.debug("!!!This function is not called by the method notifyWaitinglist.");
//			t.addObserver(wto);
			waitinglist.add(t);
		}
		return is;
	}
	
	public Task searchByHistoryId(String historyid) {
		
		Task t = null;
		
		for(int i=0;i<waitinglist.size();i++) {
			
			if(historyid.equals((waitinglist.get(i)).getHistory_id())) {
				
				t = waitinglist.get(i);
				
				break;
				
			}
			
		}
		
		if(t==null) {
			
			for(int i=0;i<runninglist.size();i++) {
				
				if(historyid.equals((runninglist.get(i)).getHistory_id())) {
					
					t = runninglist.get(i);
					
					break;
					
				}
				
			}
			
		}
		
		return t;
		
	}
	
	/**
	 * Monitor the status of a task
	 * @param sessionid
	 * @param taskname
	 */
	public void monitorTask(String historyid, Session session) {
		
		// search the task with the name in waitinglist and runninglist
		
		Task t = searchByHistoryId(historyid);
		
		t.startMonitor(session);
		
	}
	
	/**
	 * Notify the waiting list that there is at least an available worker
	 */
	public synchronized void notifyWaitinglist(){
		logger.debug("notify waiting list to pay attention to the released worker");
		if(waitinglist.size()>0&&wm.getCurrentWorkerNumber()<worknumber){
			Task newtask = waitinglist.get(0);
			waitinglist.remove(newtask);
//			newtask.deleteObserver(wto);
			executeATask(newtask);
		}
	}
	/**
	 * A task is done, being triggered to start doing another task.
	 * @param t
	 * The done task.
	 */
	public void done(Task t){
//		t.deleteObserver(rto);
		runninglist.remove(t);
		notifyWaitinglist();
	}
	
	/**
	 * A new task arrives. Notify the task manager to take care of it.
	 */
	public void arrive(Task t){
		notifyWaitinglist();
	}
	
}
