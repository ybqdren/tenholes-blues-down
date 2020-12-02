package com.zw.kq.main;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import org.apache.http.Consts;
import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.ParseException;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import com.zw.kq.utils.RourceParseUtil;

/**
 * Created by ZhaoWen on 2020年11月28日
 * http://www.tenholes.com/ 蓝调口琴网
 */

public class SpiderMain{
	static Properties prop = null;
	static String rootPath = null;
	static File rootDir = null;
	static Logger logger = null;
	
	static{
		try {
			 invildConfig("userConfig.properties");
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public static void main(String[] args) throws IOException, ParseException, InterruptedException {
		String url = "http://m.tenholes.com/tabs/catelist?id=1";
		run(url);
	}
	
	/**
	 * 单线程-运行爬虫
	 * @param url
	 * @param flag
	 * @return
	 * @throws ParseException
	 * @throws IOException
	 * @throws InterruptedException
	 */
	public static void run(String url) throws ParseException, IOException, InterruptedException{
		HttpClient httpClient = HttpClients.custom().build();
		String content = null;
		List<String>urList = null; 
		int num = 10;
		int page = 1;
		int count = 0;
		HttpGet httpGet = new HttpGet(url);
		httpGet.setHeader("Cookie", prop.getProperty("Cookie"));
		httpGet.setHeader("User-Agent", prop.getProperty("User-Agent"));
		
		String recContent = EntityUtils.toString(httpClient.execute(httpGet).getEntity(),"UTF-8");			
		while (page<21) {
			url = "http://m.tenholes.com/tabs/clist/";
			content = getSecondPagePage(recContent,httpClient,url,num,page);
			urList = RourceParseUtil.getPageUrlList(content);
			for (String u : urList) {
				count++;
				httpGet = new HttpGet(u);
				httpGet.setHeader("Cookie", prop.getProperty("Cookie"));
				httpGet.setHeader("User-Agent", prop.getProperty("User-Agent"));
				if("200".equals(downFile(httpClient, httpGet))){
					logger.info("("+count+"/"+"210)："+u+"下载成功");
				}
				Thread.sleep(2000);
			}
			page++;
		}
	}
	
	/**
	 * 20页
	 * @param content
	 * @return
	 * @throws IOException 
	 * @throws ClientProtocolException 
	 */
	public static String getSecondPagePage(String content,HttpClient httpClient,String url,int num,int page) throws ClientProtocolException, IOException{
		String crsf = null;
		String username = null;
		String cid = "1";
		List<NameValuePair> postInfo = new ArrayList();
		Document document = Jsoup.parse(content);
		Elements metaElments = document.select("html head meta");
		for (Element element : metaElments) {
			if("csrf-token".equals(element.attr("name"))){
				crsf = element.attr("content");
			}
		}
		username = document.select("input#userName").attr("value");
		
		postInfo.add(new BasicNameValuePair("_csrf-frontend",crsf));
		postInfo.add(new BasicNameValuePair("num", String.valueOf(num)));
		postInfo.add(new BasicNameValuePair("page", String.valueOf(page)));
		postInfo.add(new BasicNameValuePair("cid", cid));
		postInfo.add(new BasicNameValuePair("username",username));
		
		UrlEncodedFormEntity entity = new UrlEncodedFormEntity(postInfo,Consts.UTF_8);
		HttpPost httpPost = new HttpPost(url);
		httpPost.setEntity(entity);
		httpPost.setHeader("Cookie", prop.getProperty("Cookie"));
		httpPost.setHeader("User-Agent",prop.getProperty("User-Agent"));
		HttpResponse response = httpClient.execute(httpPost);
		HttpEntity httpEntity = response.getEntity();
		String result = EntityUtils.toString(httpEntity,Charset.forName("UTF-8"));
		EntityUtils.consume(httpEntity);
		return result;
	}
	
	
	/**
	 * 测试功能
	 * @param httpClient
	 * @param httpGet
	 * @param code 方法代号
	 * @throws IOException 
	 * @throws ClientProtocolException 
	 */
	public static void test(HttpClient httpClient,HttpGet httpGet,String code) throws ClientProtocolException, IOException{
		if("1".equals(code)){
			HttpResponse response = httpClient.execute(httpGet);
			if(response.getStatusLine().getStatusCode() == 200){
				HttpEntity httpEntity = response.getEntity();
				String entiry = EntityUtils.toString(httpEntity, Charset.forName("utf-8"));
				Map<String,String> map = RourceParseUtil.getInfo(entiry);
				Set key = map.keySet();
				for (Object k : key) {
					System.out.println(k+"-->"+map.get(k));
				}
			}
		}else if("2".equals(code)){
			HttpResponse response = httpClient.execute(httpGet);
			Header[] headers = response.getAllHeaders();
			for (int i = 0; i < headers.length; i++) {
				if("Content-Disposition".equals(headers[i].getName())){
					String[] temp = headers[i].getValue().trim().split("=");
					System.out.println(temp[1].replaceAll("\"", "").split(".mp3")[0]);
				}
			}
			
		}
	}
	
	/**
	 * 下载图片（口琴谱）和音频
	 * @param response 网页响应对象
	 * @return 状态码 200表示一次操作完成
	 * @throws IOException 
	 * @throws ParseException 
	 */
	public static String downFile(HttpClient httpClient,HttpGet httpGet) throws ParseException, IOException{
		HttpResponse response = httpClient.execute(httpGet);
		HttpEntity httpEntity = null;
		String content = null;
		Map<String, String> imageMap = null;
		File file_totural = null;
		File file_img = null;
		File file_audio = null;
		String url = null;
		
		httpEntity = response.getEntity();
		content = EntityUtils.toString(httpEntity,Charset.forName("utf-8"));
		imageMap = RourceParseUtil.getInfo(content);
		content = RourceParseUtil.getPageTotural(content);
        if(imageMap.get("id")==null || "".equals(imageMap.get("id"))){
        	EntityUtils.consume(httpEntity);
            return "0";
        }else{
    		String name = imageMap.get("id")+"-"+(imageMap.get("name").contains("/")?imageMap.get("name").replaceAll("/+", "|"):imageMap.get("name"));
    		
    		// 创建文件根目录
    		File rootDir = new File(rootPath+"/"+name+"/");
    		if(!rootDir.exists()){
    			rootDir.mkdir();
    		}
    		
//    		//保存教程到文件
//      		file_totural = new File(rootDir+"/"+name+".html");
//    		if(file_totural.exists()){
//    			file_totural.delete();
//    		}
//    		BufferedOutputStream bw_totural = new BufferedOutputStream(new FileOutputStream(file_totural,true));
//    		byte[] byt = EntityUtils.toByteArray(httpEntity);
//    		bw_totural.write(byt);
//    		bw_totural.close();
    		
    		// 获取到当前图片的地址 并重新发送请求
    		url = imageMap.get("url");
    		httpGet = new HttpGet(url);
    		response = httpClient.execute(httpGet);
    		httpEntity = response.getEntity();
    		file_img = new File(rootDir+"/"+name+".png");
    		if(file_img.exists()){
    			file_img.delete();
    		}
    		BufferedOutputStream bw_img = new BufferedOutputStream(new FileOutputStream(file_img,true));
    		byte[] byt = EntityUtils.toByteArray(httpEntity);
    		bw_img.write(byt);
    		bw_img.close();
    		
    		// 获取当前音频的地址 进行请求                                                                           
    		url = imageMap.get("audio");
    		URL url_instance = new URL(url);
    		HttpURLConnection  httpsURLConn = (HttpURLConnection) url_instance.openConnection();
    		httpsURLConn.setRequestProperty("Cookie", prop.getProperty("Cookie"));
    		httpsURLConn.setRequestProperty("User-Agent", prop.getProperty("User-Agent"));
    		httpsURLConn.connect();
    		file_audio = new File(rootDir+"/"+name+".mp3");
    		if(file_audio.exists()){
    			file_audio.delete();
    		}
    		BufferedInputStream bw_audio = new BufferedInputStream(httpsURLConn.getInputStream());
    		FileOutputStream fos = new FileOutputStream(file_audio);
    		byt = new byte[1024*8];
    		int size = 0;
    		while ((size = bw_audio.read(byt))!=-1) {
    			fos.write(byt,0,size);
    		}
    		fos.flush();
    		fos.close();
    		bw_audio.close();
    		httpsURLConn.disconnect();	
        }
		EntityUtils.consume(httpEntity);
		return "200";
	}
	
	/**
	 * 获取文件中的信息
	 * @param fileName 文件的名字
	 * @return	Properties 配置文件对象
	 * @throws IOException 
	 */
	public static Properties getFileContent(String fileName) throws IOException{
		Properties properties = new Properties();
		BufferedInputStream inputStream = (BufferedInputStream) ClassLoader.getSystemResourceAsStream("userConfig.properties");
		properties.load(inputStream);
		inputStream.close();
		return properties;
	}
	
	/**
	 * 爬虫初始化：读入配置文件 创建项目文件夹
	 * @param configFileName 配置文件路径
	 * @throws IOException
	 */
	public static void invildConfig(String configFileName) throws IOException{
		// 加载用户配置文件
		prop = getFileContent(configFileName);
		
		// 创建根目录
		rootPath = prop.getProperty("rootPath");
		rootDir = new File(rootPath);
		if(!rootDir.exists()){
			rootDir.mkdir();
		}
		
		logger = LogManager.getLogger(SpiderMain.class);
	}
}
