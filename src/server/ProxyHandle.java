package server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.Socket;

import utils.StaticData;
import utils.Forbid;
import utils.HttpHeader;
import utils.Utils;

/**
 * 代理服务器对每个线程进行的主要操作
 * 
 * @author MaXU
 *
 */
public class ProxyHandle implements Runnable {
  Socket clisocket; // 客户端的socket
  Utils utils = new Utils(); // 工具类Utils
  Forbid forbid = null; // 禁止访问的站点或者用户
  boolean openPushingSite = false; // 是否开启钓鱼站点设置

  public ProxyHandle(Socket socket, Forbid forbid, boolean openPushing) {
    this.openPushingSite = openPushing;
    this.clisocket = socket;
    this.forbid = forbid;
  }

  @Override
  public void run() {
    String request = null; // 客户端发送过来的请求
    HttpHeader header = null; // 对客户端request进行解析得到
    OutputStream cliOutStream = null; // 客户端socket输出流
    InputStream cliInStream = null; // 客户端socket输入流
    Socket serSocket = null; // 服务端的socket
    OutputStream serOutStream = null; // 服务端socket的输出流
    InputStream serInStream = null; // 服务端socket的输入流

    try {
      cliInStream = clisocket.getInputStream(); // 得到客户端输入流
      cliOutStream = clisocket.getOutputStream(); // 得到客户端输出流

      request = utils.readAllContent( // 从客户端读取请求内容
          new BufferedReader(new InputStreamReader(cliInStream)));

      // 将从客户端得到的请求进行分解，然后存在header变量中
      header = utils.parseHeader(request);

      // 对于空解析直接返回，跳过
      if (header.getHost() == null || header.getHost().isEmpty()) {
        return;
      }
      // if (!header.getHost().equalsIgnoreCase("today.hit.edu.cn")) {
      // return;
      // }

      // 禁止对某些url或者主机访问
      if (forbid.containUrl(header.getUrl())
          || forbid.containHost(header.getHost())) {
        cliOutStream.write(StaticData.FORBIDURL_FAIL.getBytes());
        cliOutStream.flush();
        return;
      }
      // 禁止某些用户访问外部网络
      if (forbid.containUser(header.getCookie())
          || forbid.containUser(this.clisocket.getInetAddress().toString())) {
        cliOutStream.write(StaticData.FORBIDUSER_FAIL.getBytes());
        cliOutStream.flush();
        return;
      }

      // 设置钓鱼网站
      if (openPushingSite) {
        this.setPhishingSite(header);
      }

      System.out.println(
          header.getHost() + "   " + header.getPort() + "  " + header.getUrl());
      System.out.println("request:\n" + request);

      // 建立与服务器端的连接
      serSocket = new Socket(header.getHost(), header.getPort());

      // 设置socket连接时长
      serSocket.setSoTimeout(StaticData.TIMEOUT);
      this.clisocket.setSoTimeout(StaticData.TIMEOUT);
      serInStream = serSocket.getInputStream();
      serOutStream = serSocket.getOutputStream();
      System.out
          .println("Successfully connected remote server:" + header.getHost());

      System.out.println("header.getHost:\n" + header.getHost());

      // 根据情况分别对HTTPS和HTTP请求进行处理
      if ("CONNECT".equalsIgnoreCase(header.getMethod())) {
        cliOutStream.write(StaticData.SUCCESS.getBytes());
        cliOutStream.flush();
        ProcessHttps(cliInStream, serInStream, cliOutStream, serOutStream);
      } else {
        System.out.println("处理HTTP请求");
        ProcessHttp(cliInStream, serInStream, cliOutStream, serOutStream,
            request);
      }

    } catch (Exception e) {
      // e.printStackTrace();
    } finally {

      try {
        this.clisocket.close();
        cliOutStream.close();
        cliInStream.close();
      } catch (IOException e2) {
        System.err.println(
            "Client socket and related input/output stream not properly closed!");
      }

      try {
        if (serSocket != null) {
          serSocket.close();
          serInStream.close();
          serOutStream.close();
        }
      } catch (IOException e1) {
        System.out.println(
            "Server socket and related input/output stream not properly closed!");
      }
    }
  }

  /**
   * 处理HTTPS请求,完成客户端和服务端之间数据转发的简单代理
   * 
   * @param clientInputStream
   * @param csInputStream
   * @param clientOutputStream
   * @param csOutputStream
   * @throws IOException
   */
  private void ProcessHttps(InputStream clientInputStream,
      InputStream csInputStream, OutputStream clientOutputStream,
      OutputStream csOutputStream) throws IOException {

    // 将服务器的数据读下来发送给客户端
    HttpsTask task = new HttpsTask(csInputStream, clientOutputStream);
    new Thread(task).start();

    // 将客户端的数据读下来发送给服务器端
    utils.fromInputToOutput(clientInputStream, csOutputStream, 1024);

    // 用来阻塞，等待Task线程执行完毕，关闭Socket
    while (true) {
      if (task.getDone()) {
        System.out.println("关闭socket");
        break;
      }
    }

  }

  /**
   * 处理 HTTP请求,交换两者之间的数据
   * 
   * @param clientInputStream
   * @param csInputStream
   * @param clientOutputStream
   * @param csOutputStream
   * @param request
   * @throws IOException
   */
  private void ProcessHttp(InputStream clientInputStream,
      InputStream csInputStream, OutputStream clientOutputStream,
      OutputStream csOutputStream, String request) throws IOException {
    System.out.println("retuest:\n" + request);

    // 将客户端http请求报文发送给服务器端
    try {
      csOutputStream.write(request.getBytes());
      csOutputStream.flush();
    } catch (Exception e) {
      System.err.println("Error sendng data to remote server");
    }

    // 将服务端的响应转发给客户端
    utils.fromInputToOutput(csInputStream, clientOutputStream,
        StaticData.SERVER_BUFSIZE);
  }

  /**
   * 设置钓鱼站点,钓鱼站点默认存储在StaticData的PHISHING_IP中 如果该变量为null或者为空则不进行钓鱼站点的设置
   * 
   * @param header
   */
  private void setPhishingSite(HttpHeader header) {
    if (StaticData.PHISHING_IP == null || StaticData.PHISHING_IP.isEmpty()) {
      return;
    }
    header.setHost(StaticData.PHISHING_IP);
    header.setPort(StaticData.PHISHING_PORT);
    System.out.println("Set up phishing site successfully!");
  }

}
