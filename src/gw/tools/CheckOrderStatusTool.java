package gw.tools;

import gw.database.DataBaseOperation;

/**
 *Class CheckOrderStatusTool.java
 *@author Ziheng Sun
 *@time Sep 2, 2015 4:54:22 PM
 *Original aim is to support CyberConnector.
 */
@Deprecated
public class CheckOrderStatusTool {
	String on = null;
	/**
	 * Construction function
	 * @param ordernumber
	 */
	public CheckOrderStatusTool(String ordernumber){
		on = ordernumber;
	}
	/**
	 * 
	 * @return
	 */
	public String check(){
		return DataBaseOperation.checkOrderStatus(on);
	}
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		CheckOrderStatusTool tool = new CheckOrderStatusTool("kkw9vsh7dani6e7cl5");
		System.out.println(tool.check());
	}

}
