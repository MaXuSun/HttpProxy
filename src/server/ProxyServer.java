package server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import utils.CommonData;
import utils.HttpHeader;
import utils.Utils;

public class ProxyServer extends ServerSocket {
  ExecutorService threadPool = Executors.newFixedThreadPool(20);
  public ProxyServer() throws IOException {
    super(CommonData.PROXYSERVER_PORT);
    System.out.println("代理服务器开启等待连接……");
    load();
  }

  private void load() throws IOException {
    while (true) {
      Socket socket = this.accept();
      threadPool.execute(new SocketHandle(socket));
    }
  }

  public static void main(String[] args) throws IOException {
    new ProxyServer();
  }
}
class SocketHandle implements Runnable {
  Socket socket;
  boolean keeplive = true;
  Utils utils = new Utils();
  public SocketHandle(Socket socket) {
    this.socket = socket;
  }
  @Override
  public void run() {
    String request = null;
    HttpHeader header = null;
    OutputStream clientOutputStream = null;
    InputStream clientInputStream = null;
    Socket proxySocket = null;
    OutputStream proxyOutputStream = null;
    InputStream proxyInputStream = null;
    try {
      clientInputStream = socket.getInputStream(); // 得到客户端输入流
      clientOutputStream = socket.getOutputStream(); // 得到客户端输出流

      request = utils.readAllContent( // 从客户端读取请求内容
          new BufferedReader(new InputStreamReader(clientInputStream)));
      // System.out.println("request\n"+request);
      header = utils.parseHeader(request);
      // 建立与服务器端的连接
      if (header.getHost() == null || header.getHost().isEmpty()) {
        return;
      }
      if (!header.getHost().equalsIgnoreCase("today.hit.edu.cn")) {
        return;
      }

      System.out.println(
          header.getHost() + "   " + header.getPort() + "  " + header.getUrl());
      System.out.println("request:\n" + request);
      proxySocket = new Socket("202.118.254.117", header.getPort());
      System.out.println("header.getHost:\n" + header.getHost());
      proxyInputStream = proxySocket.getInputStream();
      proxyOutputStream = proxySocket.getOutputStream();

      // 第一次打通隧道
      if ("CONNECT".equalsIgnoreCase(header.getMethod())) {
        clientOutputStream.write(CommonData.SUCCESS.getBytes());
        clientOutputStream.flush();
        ProcessHttps(clientInputStream, proxyInputStream, clientOutputStream,
            proxyOutputStream);
      } else {
        System.out.println("处理HTTP请求");
        ProcessHttp(clientInputStream, proxyInputStream, clientOutputStream,
            proxyOutputStream, request);
      }

    } catch (Exception e) {
      e.printStackTrace();
    } finally {
      try {
        this.socket.close();
        clientOutputStream.close();
        clientInputStream.close();
      } catch (IOException e2) {
        System.out.println("客户端socke及相关流未正常关闭");
      }

      try {
        if (proxySocket != null) {
          proxySocket.close();
          proxyInputStream.close();
          proxyOutputStream.close();
        }
      } catch (IOException e1) {
        System.out.println("服务端socket及相关流未正常关闭");
      }
    }
  }

  private void ProcessHttps(InputStream clientInputStream,
      InputStream proxyInputStream, OutputStream clientOutputStream,
      OutputStream proxyOutputStream) throws IOException {

    // 将服务器的数据读下来发送给客户端
    Task task = new Task(proxyInputStream, clientOutputStream);
    new Thread(task).start();

    // 将客户端的数据读下来发送给服务器端
    utils.fromInputToOutput(clientInputStream, proxyOutputStream, 1024);

    System.out.println("dfasdfasdf");
    // 用来阻塞，等待Task线程执行完毕，关闭Soc
    while (true) {
      if (task.getDone()) {
        System.out.println("关闭socket");
        break;
      }
    }

  }

  private void ProcessHttp(InputStream clientInputStream,
      InputStream proxyInputStream, OutputStream clientOutputStream,
      OutputStream proxyOutputStream, String request) throws IOException {
    System.out.println("retuest:\n" + request);
    // 将从服务器中读取的数据发送给客户端
    // 将客户端http请求报文发送给服务器端
    try {
      proxyOutputStream.write(request.getBytes());
      proxyOutputStream.flush();
    } catch (Exception e) {
      System.out.println("向服务端发送数据出错");
    }

    // 将客户端发送的请求返回给
    byte[] buff = new byte[1024*3];
    int len = -1;
    while((len=proxyInputStream.read(buff))!=-1) {
        clientOutputStream.write(buff, 0, len);
        System.out.println(new String(buff));
        clientOutputStream.flush();
    }
  }

}

class Task implements Runnable {
  InputStream inputStream;
  OutputStream outputStream;
  boolean done = false;

  public Task(InputStream inputStream, OutputStream outputStream) {
    this.inputStream = inputStream;
    this.outputStream = outputStream;
  }
  public boolean getDone() {
    return this.done;
  }

  @Override
  public void run() {
    byte[] buffer = new byte[1024 * 10];
    int size = 0;
    try {
    System.out.println("fasfasdfasdgagadg");
     while ((size = inputStream.read(buffer)) != -1) {
     System.out.println("ontout");
     System.out.println(new String(buffer));
     outputStream.write(buffer, 0, size);
     outputStream.flush();
     }

    // System.out.println(new Utils().readAllContent(new BufferedReader(new
    // InputStreamReader(inputStream))));
    done = true;
     } catch (IOException e) {
     }
  }
}