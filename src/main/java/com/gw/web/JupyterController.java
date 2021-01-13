package com.gw.web;

import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLDecoder;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.MultiThreadedHttpConnectionManager;
import org.apache.commons.httpclient.methods.GetMethod;
import org.json.simple.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Scope;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.RequestEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.context.request.WebRequest;

import com.gw.jpa.Host;
import com.gw.tools.HistoryTool;
import com.gw.tools.HostTool;
import com.gw.utils.BaseTool;

@Controller 
//@RequestMapping("/Geoweaver/web")
public class JupyterController {
	
	Logger logger = LoggerFactory.getLogger(getClass());
	
	private String scheme = "http";
//	private String server = "192.168.0.80";
	private String server = "localhost";
	private int port = 8888;
	
	@Autowired
	RestTemplate restTemplate;
	
	HttpHeaders headers = new HttpHeaders();
	
	@Autowired
	HistoryTool history_tool;
	
	@Autowired
	BaseTool bt;
	
	@Autowired
	HostTool ht;
	
	int TIMEOUT = 30000;
	
	public JupyterController(RestTemplateBuilder builder) {
		
//		restTemplate = new RestTemplate();
		restTemplate = builder.build();
		
		HttpComponentsClientHttpRequestFactory requestFactory = new HttpComponentsClientHttpRequestFactory();
		requestFactory.setConnectTimeout(TIMEOUT);
		requestFactory.setReadTimeout(TIMEOUT);
		
		restTemplate.setRequestFactory(requestFactory);
		
	}
	
	@Bean(name = "restTemplate")
	@Scope("prototype")
    public RestTemplate getRestTemplate() {
		
		RestTemplate restTemplate1 = new RestTemplate();
//		restTemplate = builder.build();
		
		HttpComponentsClientHttpRequestFactory requestFactory = new HttpComponentsClientHttpRequestFactory();
		requestFactory.setConnectTimeout(TIMEOUT);
		requestFactory.setReadTimeout(TIMEOUT);
		
		restTemplate1.setRequestFactory(requestFactory);
		
        return restTemplate1;
    }
	
//	@Bean
//	public RestTemplate restTemplate(RestTemplateBuilder builder) {
//		
//		RestTemplate restTemplate = builder.build();
//	    
//	    HttpComponentsClientHttpRequestFactory requestFactory = new HttpComponentsClientHttpRequestFactory();
//		requestFactory.setConnectTimeout(TIMEOUT);
//		requestFactory.setReadTimeout(TIMEOUT);
//		
//		
//
//		restTemplate.setRequestFactory(requestFactory);
//		
//		return restTemplate;
//	}
	
	/**
	 * Decode the url if it has spaces or other special characters
	 * @param referurl
	 * @return
	 */
	public String getRealTargetURL(String referurl) {
		
		String targeturl = referurl;
		try {
			targeturl = URLDecoder.decode(referurl,"UTF-8");
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		
		return targeturl;
		
	}
	
	/**
	 * Add URL Proxy
	 * @param resp
	 * @param hostid
	 * @return
	 */
	String addURLProxy(String resp, String hostid) {
		
		if(!bt.isNull(resp))
			resp = resp
				//for jupyter notebook
//				.replaceAll(scheme + "://" + server + ":" + port, replacement)
				.replace("\"/static/", "\"/Geoweaver/jupyter-proxy/"+hostid+"/static/")
				.replace("\"/custom/custom.css\"", "\"/Geoweaver/jupyter-proxy/"+hostid+"/custom/custom.css\"")
				.replace("\"/login", "\"/Geoweaver/jupyter-proxy/"+hostid+"/login")
				.replace("\"/tree", "\"/Geoweaver/jupyter-proxy/"+hostid+"/tree")
//				.replace("'contents': 'services/contents',", "'contents': 'Geoweaver/web/jupyter-proxy/services/contents',")
				.replace("/static/base/images/logo.png", "/Geoweaver/jupyter-proxy/"+hostid+"/static/base/images/logo.png")
				.replace("baseUrl: '/static/',", "baseUrl: '/Geoweaver/jupyter-proxy/"+hostid+"/static/',")
				
				.replace("url_path_join(this.base_url, 'api/config',", "url_path_join('/Geoweaver/jupyter-proxy/"+hostid+"/', 'api/config',")
				.replace("this.base_url,", "'/Geoweaver/jupyter-proxy/"+hostid+"/',")
				.replace("that.base_url,", "'/Geoweaver/jupyter-proxy/"+hostid+"/',")
				
				.replace("'/Geoweaver/jupyter-proxy/"+hostid+"/', \"api/kernels\"", "'/Geoweaver/jupyter-socket/"+hostid+"/', \"api/kernels\"")
//				.replace("requirejs(['custom/custom'], function() {});", "requirejs(['Geoweaver/web/jupyter-proxy/"+hostid+"/custom/custom'], function() {});")
				.replace("src=\"/files/", "src=\"/Geoweaver/jupyter-proxy/"+hostid+"/files/")
				.replace("this.notebook.base_url,", "'/Geoweaver/jupyter-proxy/"+hostid+"/',")
				.replace("nbextensions : '/nbextensions'", "nbextensions : '../nbextensions'")
				.replace("custom : '/custom',", "custom : '../custom',")
				.replace("kernelspecs : '/kernelspecs',", "kernelspecs : '../kernelspecs',")
//				.replace("\"nbextensions/\"", "\"Geoweaver/web/jupyter-proxy/nbextensions/\"")
//				.replace("this.base_url", "'/Geoweaver/web/jupyter-proxy/'")
//				.replace("static/base/images/logo.png", "Geoweaver/web/jupyter-proxy/static/base/images/logo.png")
//				.replace("static/services/contents", "Geoweaver/web/jupyter-proxy/static/services/contents")
//				.replace("favicon.ico", "/Geoweaver/web/jupyter-proxy/favicon.ico")
				
				//for jupyterhub
				.replace("\"/hub", "\"/Geoweaver/jupyter-proxy/"+hostid+"/hub")
				.replace("baseUrl: '/hub/static/js'", "baseUrl: '/Geoweaver/jupyter-proxy/"+hostid+"/hub/static/js'")
				
				;
		
		return resp;
		
	}
	
	/**
	 * Error Control
	 * @param message
	 * @param hostid
	 * @return
	 */
	private ResponseEntity errorControl(String message, String hostid) {
		
		logger.error(message);
		
		HttpHeaders headers = new HttpHeaders();
		
		headers.add("Content-Length", "0");
		
		ResponseEntity resp = null;
		
		if(message.indexOf("403 Forbidden")!=-1) {
			
			resp = new ResponseEntity<String>(
					message, 
		    		updateHeader(headers, message, hostid), 
		    		HttpStatus.FORBIDDEN);
			
		}else if(message.indexOf("404 Not Found")!=-1) {
			
			resp = new ResponseEntity<String>(
					message, 
		    		updateHeader(headers, message, hostid), 
		    		HttpStatus.NOT_FOUND);
			
		}else if(message.indexOf("400 Bad Request")!=-1) {
			
			resp = new ResponseEntity<String>(
					message, 
		    		updateHeader(headers, message, hostid), 
		    		HttpStatus.BAD_REQUEST);
			
		}else {
			
			resp = new ResponseEntity<String>(
					message, 
		    		updateHeader(headers, message, hostid), 
		    		HttpStatus.INTERNAL_SERVER_ERROR);
			
		}
		
		return resp;
		
	}
	
	/**
	 * Get real request uri
	 * @param requesturi
	 * @return
	 */
	private String getRealRequestURL(String requesturi) {
		
		String realurl =  requesturi.substring(requesturi.indexOf("jupyter-proxy") + 20);// /Geoweaver/web/jupyter-proxy/test
		
		return realurl;
		
	}
	
	/**
	 * Update Header Referers
	 * @param oldheaders
	 * @param h
	 * @param realurl
	 * @param querystr
	 * @return
	 */
	private HttpHeaders updateHeaderReferer(HttpHeaders oldheaders, Host h, String realurl, String querystr) {
		
		HttpHeaders newheaders = oldheaders;
		
		try {
		
			String[] ss = bt.parseJupyterURL(h.getUrl());
			
			URI uri = new URI(ss[0], null, ss[1], Integer.parseInt(ss[2]), realurl, querystr, null);
			
			String hosturl = ss[0] + "://" + ss[1] + ":" + ss[2];
			
			newheaders =  HttpHeaders.writableHttpHeaders(oldheaders);
			
			newheaders.set("host", ss[1] + ":" + ss[2]);
			
			newheaders.set("origin", hosturl);
			
			newheaders.set("referer", URLDecoder.decode(uri.toString(), "utf-8"));
			
		} catch (Exception e) {
			
			e.printStackTrace();
			
		}
		
		return newheaders;
		
	}
	
	/**
	 * General Processing Function
	 * @param entity
	 * @param method
	 * @param request
	 * @param hostid
	 * @return
	 */
	private ResponseEntity processUtil(HttpEntity entity, HttpMethod method, HttpServletRequest request, String hostid) {
		
		ResponseEntity resp = null;
		
		try {
			
			logger.debug("==============");
			
//			logger.debug("Ensuring Unicode to UTF");
			
			logger.debug("Request URI: " + request.getRequestURI());
			
//			logger.info("Request Headers: " + entity.getHeaders());
			
//			logger.info("Query String: " + request.getQueryString());
			
//			String realurl =  this.getRealRequestURL(request.getRequestURI());
//			
//			Host h = HostTool.getHostById(hostid);
//			
//			logger.info("HTTP Method: " + method.toString());
//			
//			restTemplate.setRequestFactory(new HttpComponentsClientHttpRequestFactory());
//			
//			HttpHeaders newheaders = this.updateHeaderReferer(entity.getHeaders(), h, realurl, request.getQueryString());
			
			HttpHeaders newheaders = getHeaders(entity.getHeaders(), method, request, hostid);
			
			HttpEntity newentity = new HttpEntity(entity.getBody(), newheaders);
			
//			logger.debug("URL: " + newheaders.get("referer").get(0));
			
		    ResponseEntity<String> responseEntity = restTemplate.exchange(getRealTargetURL(newheaders.get("referer").get(0)), method, newentity, String.class);
		    
		    String newbody = addURLProxy(responseEntity.getBody(), hostid);
	    	
		    resp = new ResponseEntity<String>(
		    		newbody,  
		    		updateHeader(responseEntity.getHeaders(), newbody, hostid), 
		    		responseEntity.getStatusCode());
		    
//		    if(method.equals(HttpMethod.GET)) {
//		    	
//		    	String newbody = addURLProxy(responseEntity.getBody(), hostid);
//		    	
//			    resp = new ResponseEntity<String>(
//			    		newbody,  
//			    		updateHeader(responseEntity.getHeaders(), newbody, hostid), 
//			    		responseEntity.getStatusCode());
//		    	
//		    }else {
//		    	
//		    	resp = new ResponseEntity(
//			    		addURLProxy(responseEntity.getBody(), hostid), 
//			    		responseEntity.getHeaders(), 
//			    		responseEntity.getStatusCode());
//		    	
//		    }
		    
		}catch(Exception e) {
			
			e.printStackTrace();
			
			resp = errorControl(e.getLocalizedMessage(), hostid);
			
		}
		
	    return resp;
		
	}
	
	/**
	 * Get content type from headers
	 * @param headers
	 * @return
	 */
	private String getHeaderProperty(HttpHeaders headers, String key) {
		
		String contenttype = null;
    	
		List<String> cts = headers.get(key);
    	
    	if(!bt.isNull(cts))
    		contenttype = cts.get(0);
    	
//    	logger.debug(key + " : " + contenttype);
    	
    	return contenttype;
		
	}
	
	
	private HttpHeaders getHeaders(HttpHeaders headers, HttpMethod method, HttpServletRequest request, String hostid) throws NumberFormatException, URISyntaxException {
		
		HttpHeaders newheaders = headers;
		
		try {

			String realurl =  this.getRealRequestURL(request.getRequestURI());
			
			Host h = ht.getHostById(hostid);
			
			newheaders = this.updateHeaderReferer(headers, h, realurl, request.getQueryString());
			
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
		return newheaders;
		
	}
	
	
	
	/**
	 * Process Patch
	 * @param entity
	 * @param method
	 * @param request
	 * @param hostid
	 * @return
	 */
	private ResponseEntity processPatch(HttpEntity entity, HttpMethod method, HttpServletRequest request, String hostid) {
		
//		return processUtil(entity, method, request, hostid);
		
		ResponseEntity resp = null;
		
		try {
			
			logger.debug("==============");
			
			logger.debug("Request URI: " + request.getRequestURI());
			
//			logger.debug("Request Headers: " + entity.getHeaders());
			
//			logger.info("Query String: " + request.getQueryString());
			
//			String realurl =  this.getRealRequestURL(request.getRequestURI());
//			
//			Host h = HostTool.getHostById(hostid);
//			
//			logger.info("HTTP Method: " + method.toString());
//			
//			restTemplate.setRequestFactory(new HttpComponentsClientHttpRequestFactory());
//			
//			HttpHeaders newheaders = this.updateHeaderReferer(entity.getHeaders(), h, realurl, request.getQueryString());
			
			HttpHeaders newheaders = getHeaders(entity.getHeaders(), method, request, hostid);
			
			HttpEntity newentity = new HttpEntity(entity.getBody(), newheaders);
			
//			logger.debug("URL: " + newheaders.get("referer").get(0));
			
		    ResponseEntity<String> responseEntity = restTemplate.exchange(getRealTargetURL(newheaders.get("referer").get(0)), method, newentity, String.class);
		    
		    resp = new ResponseEntity(
		    		addURLProxy(responseEntity.getBody(), hostid), 
		    		responseEntity.getHeaders(), 
		    		responseEntity.getStatusCode());
		    
		}catch(Exception e) {
			
			e.printStackTrace();
			
			resp = errorControl(e.getLocalizedMessage(), hostid);
			
		}
		
	    return resp;
		
	}
	
	/**
	 * Process PUT request
	 * @param entity
	 * @param method
	 * @param request
	 * @param hostid
	 * @return
	 */
	private ResponseEntity processPut(HttpEntity entity, HttpMethod method, HttpServletRequest request, String hostid) {
		
		ResponseEntity resp = null;
		
		try {
			
			logger.debug("==============");
			
			logger.debug("Request URI: " + request.getRequestURI());
			
//			logger.debug("Query String: " + request.getQueryString());
			
//			String body = IOUtils.toString(request.getInputStream(), StandardCharsets.UTF_8);
			
			HttpHeaders newheaders = getHeaders(entity.getHeaders(), method, request, hostid);
			
			HttpEntity newentity = new HttpEntity(entity.getBody(), newheaders);
			
			//only save the content when the request content is jupyter notebook
			if("notebook".equals(((Map)entity.getBody()).get("type"))) {
				
				String jsonString = new JSONObject((Map)entity.getBody()).toString();
				
				history_tool.saveJupyterCheckpoints(hostid, jsonString, newheaders);
				
			}
			
		    ResponseEntity<String> responseEntity = restTemplate.exchange(getRealTargetURL(newheaders.get("referer").get(0)), method, newentity, String.class);
		    
		    resp = new ResponseEntity(
		    		addURLProxy(responseEntity.getBody(), hostid), 
		    		responseEntity.getHeaders(), 
		    		responseEntity.getStatusCode());
		    
		}catch(Exception e) {
			
			e.printStackTrace();
			
			resp = errorControl(e.getLocalizedMessage(), hostid);
			
		}
		
	    return resp;
		
	}
	
	/**
	 * Process DELETE request
	 * @param headers
	 * @param method
	 * @param request
	 * @param hostid
	 * @return
	 */
	private ResponseEntity processDelete(HttpEntity entity, HttpMethod method, HttpServletRequest request, String hostid) {
		
//		return processUtil(entity, method, request, hostid);
		
		ResponseEntity resp = null;
		
		try {
			
			logger.debug("==============");
			
			logger.debug("Request URI: " + request.getRequestURI());
			
//			logger.debug("Query String: " + request.getQueryString());
			
//			String realurl =  this.getRealRequestURL(request.getRequestURI());
//			
//			Host h = HostTool.getHostById(hostid);
//			
//			String[] ss = h.parseJupyterURL();
//			
//			URI uri = new URI(ss[0], null, ss[1], Integer.parseInt(ss[2]), realurl, request.getQueryString(), null);
//			
//			logger.info("URL: " + uri.toString());
//			
//			logger.info("HTTP Method: " + method.toString());
//			
////			HttpEntity entity = new HttpEntity(headers);
//			
//			HttpHeaders newheaders = this.updateHeaderReferer(entity.getHeaders(), h, realurl, request.getQueryString());
			
			HttpHeaders newheaders = getHeaders(entity.getHeaders(), method, request, hostid);
			
			HttpEntity newentity = new HttpEntity(entity.getBody(), newheaders);
			
		    ResponseEntity<String> responseEntity = restTemplate.exchange(getRealTargetURL(newheaders.get("referer").get(0)), method, newentity, String.class);
		    
		    resp = new ResponseEntity(
		    		addURLProxy(responseEntity.getBody(), hostid), 
		    		responseEntity.getHeaders(), 
		    		responseEntity.getStatusCode());
		    
		}catch(Exception e) {
			
			e.printStackTrace();
			
			resp = errorControl(e.getLocalizedMessage(), hostid);
			
		}
		
	    return resp;
		
	}
	
	/**
	 * Process POST Request
	 * @param reqentity
	 * @param method
	 * @param request
	 * @param hostid
	 * @return
	 * @throws URISyntaxException
	 */
	private ResponseEntity processPost(RequestEntity reqentity, HttpMethod method, HttpServletRequest request, String hostid) throws URISyntaxException
	{
		
//		return processUtil(reqentity, method, request, hostid);
		
		ResponseEntity resp = null;
		
		try {
			
			logger.debug("==============");
			
			logger.debug("Request URI: " + request.getRequestURI());
			
//			logger.info("Query String: " + request.getQueryString());
			
//			String realurl =  this.getRealRequestURL(request.getRequestURI());
//			
//			Host h = HostTool.getHostById(hostid);
//			
//			String[] ss = h.parseJupyterURL();
//			
//			URI uri = new URI(ss[0], null, ss[1], Integer.parseInt(ss[2]), realurl, request.getQueryString(), null);
//			
//			logger.info("URL: " + uri.toString());
//			
//			logger.info("HTTP Method: " + method.toString());
//			
//			HttpHeaders newheaders = this.updateHeaderReferer(reqentity.getHeaders(), h, realurl, request.getQueryString());
			
			HttpHeaders newheaders = getHeaders(reqentity.getHeaders(), method, request, hostid);
			
			HttpEntity newentity = new HttpEntity(reqentity.getBody(), newheaders);
			
		    ResponseEntity<String> responseEntity = restTemplate.exchange(getRealTargetURL(newheaders.get("referer").get(0)), method, newentity, String.class);
		    
//		    if(realurl.indexOf("auth")!=-1)
//		    
//		    	logger.info("Response Body: " + responseEntity.getBody());
		    
		    resp = new ResponseEntity(
		    		addURLProxy(responseEntity.getBody(), hostid), 
		    		responseEntity.getHeaders(), 
		    		responseEntity.getStatusCode());
		    
		}catch(Exception e) {
			
			e.printStackTrace();
			
			resp = errorControl(e.getLocalizedMessage(), hostid);
			
		}
		
	    return resp;
	    
	}
	
	/**
	 * Process GET Request
	 * @param <T>
	 * @param headers
	 * @param method
	 * @param request
	 * @param hostid
	 * @return
	 * @throws URISyntaxException
	 */
	private <T> ResponseEntity<T> processGET(RequestEntity reqentity, HttpMethod method, HttpServletRequest request, String hostid) throws URISyntaxException
	{
		
//		return processUtil(reqentity, method, request, hostid);
		
		ResponseEntity resp = null;
		
		try {
			
			logger.debug("==============");
			
//			logger.debug("This is a GET request...");
			
			logger.debug("Request URI: " + request.getRequestURI());
			
			boolean ishub = false;
			
			if(request.getRequestURI().contains("user")) ishub = true;
			
//			logger.info("Query String: " + request.getQueryString());
			
//			String realurl =  this.getRealRequestURL(request.getRequestURI());
//			
//			Host h = HostTool.getHostById(hostid);
//			
//			String[] ss = h.parseJupyterURL();
//			
//			URI uri = new URI(ss[0], null, ss[1], Integer.parseInt(ss[2]), realurl, request.getQueryString(), null);
//			
//			logger.info("URL: " + uri.toString());
//			
//			logger.info("HTTP Method: " + method.toString());
//			
			
//			
////			HttpEntity entity = new HttpEntity(headers);
//			
//			HttpHeaders newheaders = this.updateHeaderReferer(reqentity.getHeaders(), h, realurl, request.getQueryString());
			
			if(ishub)logger.info("Old HTTP Headers: " + reqentity.getHeaders().toString());
			
			HttpHeaders newheaders = getHeaders(reqentity.getHeaders(), method, request, hostid);
			
			HttpEntity newentity = new HttpEntity(reqentity.getBody(), newheaders);
			
			String targeturl = getRealTargetURL(newheaders.get("referer").get(0));
			
			if(ishub)logger.info("New HTTP Headers: " + newheaders.toString());
			
//			String sec_fetch_type = getHeaderProperty(reqentity.getHeaders(), "Sec-Fetch-Dest");
			
//			logger.debug(URLDecoder.decode(newheaders.get("referer").get(0),"UTF-8"));
			
//			((SimpleClientHttpRequestFactory)restTemplate.getRequestFactory()).setConnectTimeout(TIMEOUT);
			
			if(targeturl.contains(".png") || targeturl.contains(".woff")) {
				
				ResponseEntity<byte[]> responseEntity = restTemplate.exchange(targeturl, method, newentity, byte[].class);
//				
//				String newbody = new String(responseEntity.getBody());
//				
//				HttpHeaders headers = updateHeader(responseEntity.getHeaders(), newbody, hostid);
//			    
//				resp = new ResponseEntity<byte[]>(
//						responseEntity.getBody(), 
//			    		headers, 
//			    		responseEntity.getStatusCode());;
				
				resp = responseEntity;
				
			}else {
				
				ResponseEntity<String> responseEntity = restTemplate.exchange(targeturl, method, newentity, String.class);
			    
		    	if(ishub)logger.debug("Response Header: " + responseEntity.getHeaders());
//		    	
		    	if(ishub)logger.debug("Response HTTP Code: " + responseEntity.getStatusCode());
		    	
		    	String newbody = responseEntity.getBody();
		    	
		    	String contenttype = getHeaderProperty(responseEntity.getHeaders(), "Content-Type");
		    	
//			    	
		    	if(bt.isNull(newbody)||(!bt.isNull(contenttype)&&(contenttype.contains("image")||contenttype.contains("font")))) {
		    		
//			    	HttpHeaders headers = updateHeader(responseEntity.getHeaders(), newbody, hostid);
//			    	
//			    	resp = new ResponseEntity<byte[]>(
//							bt.isNull(newbody)?null:newbody.getBytes("UTF-8"), 
//				    		headers, 
//				    		responseEntity.getStatusCode());;
		    		
		    		resp = restTemplate.exchange(targeturl, method, newentity, byte[].class);
		    		
//		    		resp = responseEntity;
		    		
		    	}else {
		    		
			    	newbody = addURLProxy(responseEntity.getBody(), hostid);

			    	if(ishub) logger.debug("Response Body: " + newbody);
			    	
			    		
//				    	if(responseEntity.getHeaders().getContentType().equals(MediaType.TEXT_HTML)
//				    			|| responseEntity.getHeaders().getContentType())
		    		
			    	
			    	HttpHeaders headers = updateHeader(responseEntity.getHeaders(), newbody, hostid);
		    		
//					if(targeturl.contains("static/tree/js")) {
						
						
//						logger.info("Response Body: " + newbody);
						
						
						
//						headers.set("Content-Length", String.valueOf(newbody.getBytes().length));
						
//					}
		    		
		    		resp = new ResponseEntity<byte[]>(
				    		newbody.getBytes("UTF-8"), 
				    		headers, 
				    		responseEntity.getStatusCode());
	    		
		    	}
//		    	
	    		
				
			}
			
		    
		    
		}catch(Exception e) {
			
			e.printStackTrace();
			
			resp = errorControl(e.getLocalizedMessage(), hostid);
			
		}
		
	    return resp;
	    
	}
	
	/**
	 * Update Header Length and Origin and location
	 * @param oldheaders
	 * @param returnbody
	 * @param hostid
	 * @return
	 */
	HttpHeaders updateHeader(HttpHeaders oldheaders, String returnbody, String hostid) {
		
		HttpHeaders newheaders = new HttpHeaders();
		
		
		oldheaders.forEach((key, value) -> {
	    	
		    	if(key.toLowerCase().equals("location")) {
		    		
		    		newheaders.set(key, "/Geoweaver/jupyter-proxy/" + hostid + value.get(0));
		    		
		    	}else if (key.toLowerCase().equals("content-length")){
		    		
//		    		logger.debug("Old Content Length: " + value);
		    		
		    		if(!bt.isNull(returnbody))
						try {
							newheaders.set(key, String.valueOf(returnbody.getBytes("UTF-8").length));
						} catch (UnsupportedEncodingException e) {
							e.printStackTrace();
						}
		    		
		    	}else {
		    		
		    		newheaders.set(key, value.get(0));
		    		
		    	}
	    	
	    });
		
		
		return newheaders;
		
	}
	
	@RequestMapping(value="/jupyter-proxy/{hostid}/hub/login", method = RequestMethod.POST)
	public ResponseEntity jupyterhub_login( HttpMethod method, @PathVariable("hostid") String hostid, 
			@RequestHeader HttpHeaders httpheaders, HttpServletRequest request) throws URISyntaxException
	{
//		ResponseEntity resp = processPost(reqentity, method, request);
		
		ResponseEntity resp = null;
		
		
//		resp = processUtil(reqentity, method, request, hostid);
		
		try {
			
//			URI uri = new URI("https", null, server, port, request.getRequestURI(), request.getQueryString(), null);
			
			logger.debug("==============");
			
			logger.debug("Login attempt starts...");
			
			logger.debug("Request URI: " + request.getRequestURI());
			
			logger.info("Query String: " + request.getQueryString());
			
			logger.info("Original Request String: " + request.getParameterMap());
			
//			String realurl =  this.getRealRequestURL(request.getRequestURI());
//			
//			Host h = HostTool.getHostById(hostid);
//			
//			String[] ss = h.parseJupyterURL();
//			
//			int current_port = Integer.parseInt(ss[2]);
//			
//			URI uri = new URI(ss[0], null, ss[1], current_port, realurl, request.getQueryString(), null);
//			
//			logger.info("URL: " + uri.toString());
//			
//			logger.info("HTTP Method: " + method.toString());
			
			HttpHeaders newheaders = getHeaders(httpheaders, method, request, hostid);
			
			MultiValueMap<String, String> map= new LinkedMultiValueMap<String, String>();
			
			Iterator hmIterator = request.getParameterMap().entrySet().iterator(); 
			  
	        // Iterate through the hashmap 
			
			StringBuffer reqstr = new StringBuffer();
	  
	        while (hmIterator.hasNext()) { 
	            
	        	Map.Entry mapElement = (Map.Entry)hmIterator.next(); 
	            
	            map.add((String)mapElement.getKey(), ((String[])(mapElement.getValue()))[0]);
	            
	            if(!bt.isNull(reqstr.toString())) {
	            	
	            	reqstr.append("&");
	            	
	            }
	            
	            reqstr.append((String)mapElement.getKey()).append("=").append(((String[])(mapElement.getValue()))[0]);
	            
	        }

			
//			HttpHeaders newheaders = this.updateHeaderReferer(httpheaders, h, realurl, request.getQueryString());
			
			HttpEntity requestentity = new HttpEntity(reqstr.toString(), newheaders);
			
			logger.info("Body: " + requestentity.getBody());
			
			logger.info("Headers: " + requestentity.getHeaders());
			
		    ResponseEntity<String> responseEntity = restTemplate.exchange(getRealTargetURL(newheaders.get("referer").get(0)), method, requestentity, String.class);
		    
		    HttpHeaders respheaders = responseEntity.getHeaders();
		    
		    if(responseEntity.getStatusCode()==HttpStatus.FOUND) {
		    	
//		    	MultiValueMap<String, String> headers =new LinkedMultiValueMap<String, String>();
		    	
		    	HttpHeaders newresponseheaders = new HttpHeaders();
		    	
//		    	logger.info("Redirection: " + newresponseheaders);
//			    
//			    logger.info("Response: " + responseEntity.getBody());
			    
//			    responseEntity = restTemplate.exchange(uri, method, requestentity, String.class);
			    
//			    responseEntity.getHeaders().compute("Location", (k, v) -> {v.clear(); v.add("/Geoweaver/web/jupyter-proxy/tree?");});
			    
//			    responseEntity.getHeaders().set("Location", "/Geoweaver/web/jupyter-proxy/tree?");
			    
//			    respheaders.set("Location", "/Geoweaver/web/jupyter-proxy/tree?");
			    
//			    respheaders.setLocation(new URI("/Geoweaver/web/jupyter-proxy/tree?"));
			    
//			    respheaders.add("Test", "Test Value");
			    
			    respheaders.forEach((key, value) -> {
			    	
			    	if(key.toLowerCase().equals("location")) {
			    		
			    		newresponseheaders.set(key, "/Geoweaver/jupyter-proxy/" + hostid + value.get(0));
			    		
			    	}else {
			    		
			    		newresponseheaders.set(key, value.get(0));
			    		
			    	}
			    	
			    });
			    
			    respheaders = newresponseheaders;
			    
//			    Set ent = respheaders.entrySet();
			    
			    logger.info(respheaders.toString());
		    	
		    }else if(responseEntity.getStatusCode()==HttpStatus.UNAUTHORIZED) {
		    	
		    	logger.error("Login Unauthorized");
		    	
		    }
		    
//		    resp = new ResponseEntity(null, respheaders, resp.getStatusCode());
		    
		    resp = new ResponseEntity(
		    		responseEntity.getBody(), 
		    		respheaders, 
		    		responseEntity.getStatusCode());
		    
			
		}catch(Exception e) {
			
			e.printStackTrace();
			
			resp = errorControl(e.getLocalizedMessage(), hostid);
			
		}
		
	    return resp;
	    
	}
	
	@RequestMapping(value="/jupyter-proxy/{hostid}/login", method = RequestMethod.POST)
	public ResponseEntity jupyter_login( HttpMethod method, @PathVariable("hostid") String hostid, 
			@RequestHeader HttpHeaders httpheaders, HttpServletRequest request) throws URISyntaxException
	{
//		ResponseEntity resp = processPost(reqentity, method, request);
		
		ResponseEntity resp = null;
		
		
//		resp = processUtil(reqentity, method, request, hostid);
		
		try {
			
//			URI uri = new URI("https", null, server, port, request.getRequestURI(), request.getQueryString(), null);
			
			logger.debug("==============");
			
			logger.debug("Login attempt starts...");
			
			logger.debug("Request URI: " + request.getRequestURI());
			
//			logger.info("Query String: " + request.getQueryString());
			
//			logger.info("Original Request String: " + request.getParameterMap());
			
			logger.info("Old Headers: " + httpheaders);
			
//			String realurl =  this.getRealRequestURL(request.getRequestURI());
//			
//			Host h = HostTool.getHostById(hostid);
//			
//			String[] ss = h.parseJupyterURL();
//			
//			int current_port = Integer.parseInt(ss[2]);
//			
//			URI uri = new URI(ss[0], null, ss[1], current_port, realurl, request.getQueryString(), null);
//			
//			logger.info("URL: " + uri.toString());
//			
//			logger.info("HTTP Method: " + method.toString());
			
			HttpHeaders newheaders = getHeaders(httpheaders, method, request, hostid);
			
			MultiValueMap<String, String> map= new LinkedMultiValueMap<String, String>();
			
			Iterator hmIterator = request.getParameterMap().entrySet().iterator(); 
			  
	        // Iterate through the hashmap 
			
			StringBuffer reqstr = new StringBuffer();
	  
	        while (hmIterator.hasNext()) { 
	            
	        	Map.Entry mapElement = (Map.Entry)hmIterator.next(); 
	            
	            map.add((String)mapElement.getKey(), ((String[])(mapElement.getValue()))[0]);
	            
	            if(!bt.isNull(reqstr.toString())) {
	            	
	            	reqstr.append("&");
	            	
	            }
	            
	            reqstr.append((String)mapElement.getKey()).append("=").append(((String[])(mapElement.getValue()))[0]);
	            
	        }

			
//			HttpHeaders newheaders = this.updateHeaderReferer(httpheaders, h, realurl, request.getQueryString());
			
			HttpEntity requestentity = new HttpEntity(reqstr.toString(), newheaders);
			
			logger.info("Body: " + requestentity.getBody());
			
			logger.info("New Headers: " + requestentity.getHeaders());
			
		    ResponseEntity<String> responseEntity = restTemplate.exchange(getRealTargetURL(newheaders.get("referer").get(0)), method, requestentity, String.class);
		    
		    HttpHeaders respheaders = responseEntity.getHeaders();
		    
		    if(responseEntity.getStatusCode()==HttpStatus.FOUND) {
		    	
//		    	MultiValueMap<String, String> headers =new LinkedMultiValueMap<String, String>();
		    	
		    	HttpHeaders newresponseheaders = new HttpHeaders();
		    	
//		    	logger.info("Redirection: " + newresponseheaders);
//			    
//			    logger.info("Response: " + responseEntity.getBody());
			    
//			    responseEntity = restTemplate.exchange(uri, method, requestentity, String.class);
			    
//			    responseEntity.getHeaders().compute("Location", (k, v) -> {v.clear(); v.add("/Geoweaver/web/jupyter-proxy/tree?");});
			    
//			    responseEntity.getHeaders().set("Location", "/Geoweaver/web/jupyter-proxy/tree?");
			    
//			    respheaders.set("Location", "/Geoweaver/web/jupyter-proxy/tree?");
			    
//			    respheaders.setLocation(new URI("/Geoweaver/web/jupyter-proxy/tree?"));
			    
//			    respheaders.add("Test", "Test Value");
			    
			    respheaders.forEach((key, value) -> {
			    	
			    	if(key.toLowerCase().equals("location")) {
			    		
			    		newresponseheaders.set(key, "/Geoweaver/jupyter-proxy/" + hostid + value.get(0));
			    		
			    	}else {
			    		
			    		newresponseheaders.set(key, value.get(0));
			    		
			    	}
			    	
			    });
			    
			    respheaders = newresponseheaders;
			    
//			    Set ent = respheaders.entrySet();
			    
//			    logger.info(respheaders.toString());
		    	
		    }else if(responseEntity.getStatusCode()==HttpStatus.UNAUTHORIZED) {
		    	
		    	logger.error("Login Unauthorized");
		    	
		    }
		    
//		    resp = new ResponseEntity(null, respheaders, resp.getStatusCode());
		    
		    resp = new ResponseEntity(
		    		responseEntity.getBody(), 
		    		respheaders, 
		    		responseEntity.getStatusCode());
		    
			
		}catch(Exception e) {
			
			e.printStackTrace();
			
			resp = errorControl(e.getLocalizedMessage(), hostid);
			
		}
		
	    return resp;
	    
	}
	
	@RequestMapping(value="/jupyter-proxy/{hostid}/**", method = RequestMethod.DELETE)
	public ResponseEntity proxydelete( RequestEntity reqentity, @PathVariable("hostid") String hostid, HttpMethod method, HttpServletRequest request) throws URISyntaxException
	{
		ResponseEntity resp = processDelete(reqentity, method, request, hostid);
		
	    return resp;
	    
	}
	
	@RequestMapping(value="/jupyter-proxy/{hostid}/**", method = RequestMethod.PATCH)
	public ResponseEntity proxypatch( RequestEntity reqentity, @PathVariable("hostid") String hostid, HttpMethod method, HttpServletRequest request) throws URISyntaxException
	{
		ResponseEntity resp = processPatch(reqentity, method, request, hostid);
		
	    return resp;
	    
	}
	
	@RequestMapping(value="/jupyter-proxy/{hostid}/**", method = RequestMethod.PUT)
	public ResponseEntity proxyput( RequestEntity reqentity, @PathVariable("hostid") String hostid, HttpMethod method, HttpServletRequest request) throws URISyntaxException
	{
		ResponseEntity resp = processPut(reqentity, method, request, hostid);
		
	    return resp;
	    
	}
	
	@RequestMapping(value="/jupyter-proxy/{hostid}/**", method = RequestMethod.POST,
			consumes = MediaType.ALL_VALUE,
			produces = MediaType.ALL_VALUE)
	public ResponseEntity proxypost( RequestEntity reqentity, @PathVariable("hostid") String hostid, HttpMethod method, HttpServletRequest request) throws URISyntaxException
	{
		ResponseEntity resp = processPost(reqentity, method, request, hostid);
		
	    return resp;
	    
	}
	
	@RequestMapping(value="/jupyter-proxy/{hostid}/**", method = RequestMethod.GET,
			consumes = MediaType.ALL_VALUE,
			produces = MediaType.ALL_VALUE)
	public ResponseEntity proxyget(RequestEntity reqentity, HttpMethod method, @PathVariable("hostid") String hostid, HttpServletRequest request) throws URISyntaxException
	{
		ResponseEntity resp = processGET( reqentity, method, request, hostid);
		
	    return resp;
	    
	}
	
	@RequestMapping(value="/jupyter-proxy/{hostid}", method = RequestMethod.GET,
			consumes = MediaType.ALL_VALUE,
			produces = MediaType.ALL_VALUE)
	public ResponseEntity proxyroot_get(HttpMethod method, @PathVariable("hostid") String hostid, RequestEntity reqentity, HttpServletRequest request) throws URISyntaxException
	{
		ResponseEntity resp = processGET(reqentity, method, request, hostid);
		
//		try {
//			
//			URI uri = new URI(scheme, null, server, port, null, request.getQueryString(), null);
//			
//			logger.info("URL: " + uri.toString());
//			
//		    ResponseEntity<String> responseEntity = restTemplate.exchange(uri, method, null, String.class);
//		    
//		    resp = replaceURLProxyHeader(responseEntity.getBody());
//			
//		}catch(Exception e) {
//			
//			e.printStackTrace();
//			
//		}
		
	    return resp;
	    
	}
	
	@RequestMapping(value = "/jupyter-http", method = RequestMethod.GET,
			consumes = MediaType.ALL_VALUE,
			produces = MediaType.ALL_VALUE)
    public @ResponseBody String jupyter_http(ModelMap model, WebRequest request){
		
		String resp = null;
		
		try {
			
			String targeturl = request.getParameter("url");
			
			HttpClient client = new HttpClient(new MultiThreadedHttpConnectionManager());
			
			client.getHttpConnectionManager().getParams().setConnectionTimeout(30000);
			
			GetMethod get = new GetMethod(targeturl);
            
			get.setFollowRedirects(true);
			
            int iGetResultCode = client.executeMethod(get);
			
			resp = get.getResponseBodyAsString();
            
		}catch(Exception e) {
			
			throw new RuntimeException("failed " + e.getLocalizedMessage());
			
		}
		
		return resp;
		
	}
	
	@RequestMapping(value = "/jupyter-https", method = RequestMethod.POST,
			consumes = MediaType.ALL_VALUE,
			produces = MediaType.ALL_VALUE)
    public @ResponseBody String jupyter_https(ModelMap model, WebRequest request){
		
		String resp = null;
		
		try {
			
			String targeturl = request.getParameter("url");
			
			HttpClient client = new HttpClient(new MultiThreadedHttpConnectionManager());
			
			client.getHttpConnectionManager().getParams().setConnectionTimeout(30000);
			
			GetMethod get = new GetMethod(targeturl);
            
			get.setFollowRedirects(true);
			
            int iGetResultCode = client.executeMethod(get);
			
			resp = get.getResponseBodyAsString();
            
            
			
		}catch(Exception e) {
			
			throw new RuntimeException("failed " + e.getLocalizedMessage());
			
		}
		
		return resp;
		
	}
	
	@RequestMapping(value = "/jupyter-websocket/{hostid}", method = RequestMethod.POST)
	public @ResponseBody String jupyter_websocket(ModelMap model, @PathVariable("hostid") String hostid, WebRequest request){
		
		String resp = null;
		
		try {
			
			
			
		}catch(Exception e) {
			
			throw new RuntimeException("failed " + e.getLocalizedMessage());
			
		}
		
		return resp;
		
	}

}
