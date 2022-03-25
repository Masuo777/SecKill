package com.marshio.redisdemo.utils;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.marshio.redisdemo.pojo.User;
import com.marshio.redisdemo.vo.ResponseBean;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * 生成用户工具类
 * <p>
 * @author zhoubin
 * @since 1.0.0
 */
public class UserUtil {
	private static void createUser(int count) throws Exception {
		List<User> users = new ArrayList<>(count);
		//生成用户
		for (int i = 0; i < count; i++) {
			User user = new User();
			user.setId(15100000000L + i);
			user.setLoginCount(1);
			user.setNickname("user_" + i);
			user.setRegisterDate(new Date());
			user.setSlat("marshio106");
			user.setPassword(MD5Util.inputPassToDbPass("111111", user.getSlat()));
			users.add(user);
		}
		System.out.println("create user");
		// 插入数据库
		// Connection conn = getConn();
		// String sql = "insert into t_user(login_count, nickname, register_date, slat, password, id)values(?,?,?,?,?,?)";
		// PreparedStatement preparedStatement = conn.prepareStatement(sql);
		// for (User user : users) {
		// 	preparedStatement.setInt(1, user.getLoginCount());
		// 	preparedStatement.setString(2, user.getNickname());
		// 	preparedStatement.setTimestamp(3, new Timestamp(user.getRegisterDate().getTime()));
		// 	preparedStatement.setString(4, user.getSlat());
		// 	preparedStatement.setString(5, user.getPassword());
		// 	preparedStatement.setLong(6, user.getId());
		// 	preparedStatement.addBatch();
		// }
		// preparedStatement.executeBatch();
		// preparedStatement.close();
		// conn.close();
		System.out.println("insert to db done.");
		//登录，生成userTicket
		String urlString = "http://localhost:8080/login/doLogin";
		File file = new File("D:\\code\\redis-demo\\src\\main\\resources\\static\\user\\config.txt");
		if (file.exists()) {
			file.delete();
		}
		RandomAccessFile raf = new RandomAccessFile(file, "rw");
		file.createNewFile();
		raf.seek(0);
		for (User user : users) {

			String params = "mobile=" + user.getId() + "&password=" + MD5Util.inputPassToFormPass("111111");
			// String s = doLogin(urlString, params);


			URL url = new URL(urlString);
			HttpURLConnection co = (HttpURLConnection) url.openConnection();
			co.setRequestMethod("POST");
			co.setDoOutput(true);
			OutputStream out = co.getOutputStream();

			out.write(params.getBytes());
			out.flush();
			InputStream inputStream = co.getInputStream();
			ByteArrayOutputStream bout = new ByteArrayOutputStream();
			byte []buff = new byte[1024];
			int len;
			while ((len = inputStream.read(buff)) >= 0) {
				bout.write(buff, 0, len);
			}
			inputStream.close();
			bout.close();
			String response = new String(bout.toByteArray());
			ObjectMapper mapper = new ObjectMapper();
			ResponseBean respBean = mapper.readValue(response, ResponseBean.class);
			String userTicket = ((String) respBean.getObjects());
			System.out.println("create userTicket : " + user.getId());

			String row = user.getId() + "," + userTicket;
			raf.seek(raf.length());
			raf.write(row.getBytes());
			raf.write("\r\n".getBytes());
			System.out.println("write to file : " + userTicket);
		}
		raf.close();

		System.out.println("over");
	}

	private static Connection getConn() throws Exception {
		String url = "jdbc:mysql://192.168.64.136:3308/seckill?useUnicode=true&characterEncoding=UTF-8&serverTimezone=Asia/Shanghai";
		String username = "query_1";
		String password = "query_root";
		String driver = "com.mysql.cj.jdbc.Driver";
		Class.forName(driver);
		return DriverManager.getConnection(url, username, password);
	}

	public static void main(String[] args) throws Exception {
		createUser(5000);
	}

	public static String doLogin(String url, String param) {
		PrintWriter out = null;
		BufferedReader in = null;
		StringBuilder result = new StringBuilder();
		try {
			URL realUrl = new URL(url);
			// 打开和URL之间的连接
			URLConnection conn = realUrl.openConnection();
			// 设置通用的请求属性
			conn.setRequestProperty("accept", "*/*");
			conn.setRequestProperty("connection", "Keep-Alive");
			conn.setRequestProperty("Cookie", "cookie的参数");
			conn.setRequestProperty("user-agent",
					"Mozilla/4.0 (compatible; MSIE 6.0; Windows NT 5.1;SV1)");

			// 发送POST请求必须设置如下两行
			conn.setDoOutput(true);
			conn.setDoInput(true);
			// 获取URLConnection对象对应的输出流
			out = new PrintWriter(conn.getOutputStream());
			// 发送请求参数
			out.print(param);
			// flush输出流的缓冲
			out.flush();
			// 定义BufferedReader输入流来读取URL的响应
			in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
			String line;
			while ((line = in.readLine()) != null) {
				System.out.println(line);
				result.append(line);
			}
		} catch (Exception e) {
			System.out.println("发送 POST 请求出现异常！"+e);
			e.printStackTrace();
		}
		//使用finally块来关闭输出流、输入流
		finally{
			try{
				if(out!=null){
					out.close();
				}
				if(in!=null){
					in.close();
				}
			}
			catch(IOException ex){
				ex.printStackTrace();
			}
		}
		return result.toString();
	}
}
