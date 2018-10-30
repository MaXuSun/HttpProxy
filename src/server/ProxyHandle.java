package server;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.Socket;
import java.net.URL;
import java.nio.file.Path;
import java.text.SimpleDateFormat;
import java.util.Locale;

import utils.Forbid;
import utils.HttpHeader;
import utils.StaticData;
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
    FileOutputStream fileOutStream = null;

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

      // 建立与服务器端的连接
      serSocket = new Socket(header.getHost(), header.getPort());

      // 设置socket连接时长
      serSocket.setSoTimeout(StaticData.TIMEOUT);
      this.clisocket.setSoTimeout(StaticData.TIMEOUT);
      serInStream = serSocket.getInputStream();
      serOutStream = serSocket.getOutputStream();

      // 如果请求是HTTPS就进行简单转发，且HTTPs不支持缓存功能
      if ("CONNECT".equalsIgnoreCase(header.getMethod())) {
        cliOutStream.write(StaticData.SUCCESS.getBytes());
        cliOutStream.flush();
        ProcessHttps(cliInStream, serInStream, cliOutStream, serOutStream);
        return;
      }

      // 询问文件是否是最新版
      if (this.askNewFile(header.getUrl(), serInStream, serOutStream)) {
        this.cacheFile(header.getUrl(), cliOutStream);
        return;
      }
      // 如果需要缓存文件，就得到对应的文件流用来缓存
      Path path = utils.getPathFromURL(header.getUrl(),
          StaticData.CACHE_FILE_ROOT);
      File file = utils.createFile(path);
      fileOutStream = new FileOutputStream(file);
      System.out
          .println("Successfully connected remote server: " + header.getHost());
      System.out.println("Cookie: "+header.getCookie());

      // 处理HTTP请求
      ProcessHttp(cliInStream, serInStream, cliOutStream, serOutStream, request,
          fileOutStream);

    } catch (Exception e) {
      // e.printStackTrace();
    } finally {
      try {
        if (fileOutStream != null) {
          fileOutStream.close();
        }
      } catch (Exception e2) {
      }
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
    utils.fromInputToOutput(clientInputStream, csOutputStream, 1024, null);

    // 用来阻塞，等待Task线程执行完毕，关闭Socket
    while (true) {
      if (task.getDone()) {
        break;
      }
    }

  }

  /**
   * 处理 HTTP请求,交换客户端与服务器端之间的数据
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
      OutputStream csOutputStream, String request, FileOutputStream fos)
      throws IOException {

    // 将客户端http请求报文发送给服务器端
    try {
      csOutputStream.write(request.getBytes());
      csOutputStream.flush();
    } catch (Exception e) {
      System.err.println("Error sendng data to remote server");
    }

    // 将服务端的响应转发给客户端
    utils.fromInputToOutput(csInputStream, clientOutputStream,
        StaticData.SERVER_BUFSIZE, fos);
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

  /**
   * 询问某文件是否为最新资源，如果已经是最新资源则返回true，如果不是最新资源且从远程服务器获取到最新资源则返回true； 其他情况一律返回false1
   * 
   * @return
   * @throws Exception
   */
  private boolean askNewFile(String strUrl, InputStream inputStream,
      OutputStream outStream) throws Exception {
    Path path = utils.getPathFromURL(strUrl, StaticData.CACHE_FILE_ROOT);
    File file = path.toFile();
    // 如果文件不存在直接返回 false 知道该文件不存在
    // 否则构建一个条件请求头
    if (!file.exists()) {
      return false;
    } else {
      URL url = new URL(strUrl);
      // 获得文件修改的最后时间
      SimpleDateFormat format = new SimpleDateFormat("EEE, d MMM yyyy HH:mm:ss",
          Locale.ENGLISH);
      String lastModifyTime = format.format(file.lastModified());

      // 对于主页不进行询问，因为主页不确定是以.jsp,.html,或者其他类型的主页，直接返回false重新加载
      if (url.getPath().endsWith("/")) {
        return false;
      }

      // 构建条件请求头
      String request = "GET " + url.getPath() + " HTTP/1.1\r\n" + "Host: "
          + url.getHost() + "\r\n" + "If-modified-since: " + lastModifyTime
          + "\r\n\r\n";

      // 向远程服务器发送条件请求头
      outStream.write(request.getBytes());
      outStream.flush();

      // 从远程服务器接受反馈
      String get = utils.readLineByIs(inputStream);

      // 如果从远程服务器没有收到反馈消息就直接当作本地没有该文件处理
      if (get == null || get.isEmpty()) {
        return false;
      } else if (get.contains("304") || get.contains("200")) { // 如果包含200或者304就说明已经是最新的内容就返回true
        return true;
      } else { // 否则不是最新内容，就从远处获取最新内容
        FileOutputStream fileOutputStream = new FileOutputStream(file);
        fileOutputStream.write(get.getBytes());
        fileOutputStream.flush(); // 先将刚才读取的内容缓存到本地
        utils.fromInputToOutput(inputStream, fileOutputStream,
            StaticData.SERVER_BUFSIZE, null); // 再将流中剩下的内容缓存在本地
        fileOutputStream.close();
        return false;
      }
    }
  }

  /**
   * 缓存文件函数,判断url资源是否被缓存下来了，如果被缓存下来就将该文件 输出到 cliOutStrem 输出流中
   * 
   * @param Strurl
   *          url资源
   * @param outStream
   *          一个输出流
   * @return
   * @throws Exception
   */
  private boolean cacheFile(String Strurl, OutputStream outStream)
      throws Exception {

    Path path = utils.getPathFromURL(Strurl, StaticData.CACHE_FILE_ROOT);
    File file = path.toFile();
    if (file.exists()) {
      FileInputStream inputStream = new FileInputStream(file);
      utils.fromInputToOutput(inputStream, outStream, StaticData.SERVER_BUFSIZE,
          null);
      inputStream.close();
      return true;
    } else {
      return false;
    }
  }

}
