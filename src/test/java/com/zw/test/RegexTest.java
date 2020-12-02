package com.zw.test;

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.junit.Test;

/**
 * Created by ZhaoWen on 2020年11月29日
 */

public class RegexTest {
	@Test
	public void testPageName(){
		String teString = "《Desperado/亡命之徒》from 老鹰乐队";
		String name = teString.contains("/")?teString.replaceAll("/+", "-"):teString;
		System.out.print(name);
	}
	
	@Test
	public void testRegex01() throws IOException{
		BufferedInputStream in = (BufferedInputStream) ClassLoader.getSystemResourceAsStream("test.txt");
		byte[] buffer = new byte[1024];
		int byteRead = 0;
		
		String test = "<title>《看海的街道/更替的四季》 - 曲谱详情 - 蓝调口琴网</title>";
		String regex = "(mc-info-img\">\\s+<img src=\")(http:[/\\w.]+)(?=\">)";
		String nameRegex = "(<title>《)(.*)(》.*</title>)";
		
		while ((byteRead = in.read(buffer)) != -1) {
			String line = new String(buffer,0,byteRead);
			Pattern namePattern = Pattern.compile("(http://cache1.tenholes.com/\\d+/\\d*.jpg)");
			Matcher name = namePattern.matcher(line);
			while (name.find()) {
				System.out.println(name.group(1));
				break;
			}
		}
//		Pattern namePattern = Pattern.compile(nameRegex);
//		Matcher name = namePattern.matcher(test);
//		while (name.find()) {
//			System.out.print(name.group(2));
//		}
	}
}
