package com.zw.kq.utils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.zw.kq.model.PageUrlModel;
import org.apache.http.Header;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

/**
 * Created by ZhaoWen on 2020年11月29日
 * http://www.tenholes.com/ 网页资源处理工具类
 */
public class RourceParseUtil {
	
	/**
	 * 提取页面中的教程源码
	 * @param content
	 * @return
	 */
	public static String getPageTotural(String content){
		Document document = Jsoup.parse(content);
		String result = document.select("mt-con").text();
		return result;
	}


	/**
	 * 提取页面中的所有曲谱连接
	 * @param content 页面内容
	 * @return
	 * @throws ClientProtocolException
	 * @throws IOException
	 */
	public static List<String> getPageUrlList(String content) throws ClientProtocolException, IOException{
		List<String> urls = new ArrayList();
		Document document = Jsoup.parse(content);
		Elements url = document.select("div.mc-item a");
		for(Element u : url){
			urls.add("http://m.tenholes.com"+u.attr("href"));
		}
		return urls;
	}
	
	/**
	 * 解析音频名称
	 * @param response 一次响应
	 * @return 音频名称（去后缀）
	 */
	public static String getAudioName(HttpResponse response){
		Header[] headers = response.getAllHeaders();
		String audioName = "";
		for (int i = 0; i < headers.length; i++) {
			if("Content-Disposition".equals(headers[i].getName())){
				String[] temp = headers[i].getValue().trim().split("=");
				temp = temp[1].replaceAll("\"", "").split(".mp3");
				audioName = temp[0];
			}
		}
		return audioName;
	}
	
	/**
	 * 获取指定名称的曲谱信息（每次只返回一个）
	 * @param content
	 * @param key
	 * @return
	 */
	public static Map<String, String> getInfo(String content,String key){
		return null;
	}
	
	/**
	 * 获取口琴谱图片地址和口琴谱名 口琴伴奏音频地址
	 * @param content 整个网页的内容
	 * @return Map<name,url,audio> 图片名称:图片地址:伴奏地址
	 */
	public static Map<String, String> getInfo(String content){
		Elements id = null;
		Elements imageUrl = null;
		Elements imageName = null;
		Elements category = null;
		Elements audio = null;
		Map<String,String> imageMap = new HashMap();
		PageUrlModel pageUrlModel = new PageUrlModel();
		Document document = Jsoup.parse(content);
		
		//获取曲谱编号
		id = document.select("div.audio-opera div span.audio-switch");
		// 获取图片地址
		imageUrl = document.select("div.mc-info-img img");
		// 获取名称
		category = document.getElementsByClass("mci-left").select("div.mci-tag span");
		imageName = document.getElementsByClass("mci-left").select("h3.ellipsis");
		// 获取音频伴奏地址
		audio = document.select("div.audio-opera a");

		pageUrlModel.setId(id.attr("data-specId"));
		pageUrlModel.setName(category.text()+"-"+imageName.text());
		pageUrlModel.setUrl(imageUrl.attr("src"));
		pageUrlModel.setAudioUrl("http://m.tenholes.com"+audio.attr("href"));


		imageMap.put("id", id.attr("data-specId"));
		imageMap.put("name",category.text()+"-"+imageName.text());
		imageMap.put("url", imageUrl.attr("src"));
		imageMap.put("audio", "http://m.tenholes.com"+audio.attr("href"));
		return imageMap;
	}
}
