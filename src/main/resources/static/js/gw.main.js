/**
*
* Geoweaver main function
* 
* @author Ziheng Sun
* 
* @date 20 Aug 2018
* 
*/

GW.main = {
		
	getJSessionId: function (){
		
	    var jsId = document.cookie.match(/JSESSIONID=[^;]+/);
	    
	    if(jsId != null) {
	        if (jsId instanceof Array)
	            jsId = jsId[0].substring(11);
	        else
	            jsId = jsId.substring(11);
	    }
	    
	    return jsId;
	},
	
	test_websocket: function(){
	
		var test1_url = GW.ssh.getWsPrefixURL() + "test-socket/xxxx/user/zsun/api/kernels/2eee232rsdfsweroeiwr/channels"
		
		var test1 = new WebSocket(test1_url);
		
		test1.onopen = function(e) { GW.ssh.ws_onopen(e) };
        
		test1.onclose = function(e) { GW.ssh.ws_onclose(e) };
        
		test1.onmessage = function(e) { GW.ssh.ws_onmessage(e) };
        
		test1.onerror = function(e) { GW.ssh.ws_onerror(e) };
		
		var test2_url = GW.ssh.getWsPrefixURL() + "test-socket"
		
		var test2 = new WebSocket(test2_url);
		
		test2.onopen = function(e) { GW.ssh.ws_onopen(e) };
        
		test2.onclose = function(e) { GW.ssh.ws_onclose(e) };
        
		test2.onmessage = function(e) { GW.ssh.ws_onmessage(e) };
        
		test2.onerror = function(e) { GW.ssh.ws_onerror(e) };
		
		
		
	},
	
	init: function(){
		
		$("#menuheader").val("Geoweaver v" + GW.version);
		
		GW.workspace.init();
		
		GW.toolbar.init();
		
		GW.general.init();
		
		GW.menu.init();
		
		GW.ssh.startLogSocket(GW.main.getJSessionId());
		
		introJs().start();
		
//		this.test_websocket()
		
	}
		
};

GW.main.init();