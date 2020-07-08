package gw.tasks;

import java.util.ArrayList;
import java.util.List;

import org.springframework.web.socket.WebSocketSession;

import gw.utils.SysDir;
import gw.workers.Worker;
import gw.workers.WorkerManager;

/**
 *Class TaskManager.java
 *
 *updated on 11/17/2018
 *remove the observer and observable because they are deprecated in the latest JDK (>=9)
 *
 *@author ziheng
 *@time Aug 10, 2015 4:05:28 PM
 *Original aim is to support iGFDS.
 */
public class TaskManager {
	
	private static List<Task> waitinglist;
	private static List<Task> runninglist;
	private static RunningTaskObserver rto;
	private static WaitingTaskObserver wto;
	
	static{
		waitinglist = new ArrayList();
		runninglist = new ArrayList();
//		rto = new RunningTaskObserver();
//		wto = new WaitingTaskObserver();
	}
	/**
	 * Add a new task to the waiting list
	 */
	public static void addANewTask(Task t){
//		t.addObserver(wto);
		waitinglist.add(t);
		notifyWaitinglist();
//		t.initialize();
	}
	
	public static void runDirectly(Task t) throws InterruptedException {
		
		Worker w = WorkerManager.getMustWorker();
		
		w.setTask(t);
		
		w.join(7*24*60*60*1000); // 7 days maximum
		
		WorkerManager.removeMustWorker(w);
		
	}
	/**
	 * Execute a task
	 * @param t
	 * @return
	 */
	private static boolean executeATask(Task t){
		boolean is = false;
		if(WorkerManager.getCurrentWorkerNumber()<SysDir.worknumber){
//			t.addObserver(rto);
			WorkerManager.createANewWorker(t);
			runninglist.add(t);
			is = true;
		}else{
			System.out.println("!!!This function is not called by the method notifyWaitinglist.");
//			t.addObserver(wto);
			waitinglist.add(t);
		}
		return is;
	}
	
	public static Task searchByHistoryId(String historyid) {
		
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
	public static void monitorTask(String historyid, WebSocketSession session) {
		
		// search the task with the name in waitinglist and runninglist
		
		Task t = searchByHistoryId(historyid);
		
		t.startMonitor(session);
		
	}
	
	/**
	 * Notify the waiting list that there is at least an available worker
	 */
	public static synchronized void notifyWaitinglist(){
		System.out.println("notify waiting list to pay attention to the released worker");
		if(waitinglist.size()>0&&WorkerManager.getCurrentWorkerNumber()<SysDir.worknumber){
			Task newtask = waitinglist.get(0);
			waitinglist.remove(newtask);
//			newtask.deleteObserver(wto);
			TaskManager.executeATask(newtask);
		}
	}
	/**
	 * A task is done, being triggered to start doing another task.
	 * @param t
	 * The done task.
	 */
	public static void done(Task t){
//		t.deleteObserver(rto);
		runninglist.remove(t);
		notifyWaitinglist();
	}
	
	/**
	 * A new task arrives. Notify the task manager to take care of it.
	 */
	public static void arrive(Task t){
		notifyWaitinglist();
	}
	
}
