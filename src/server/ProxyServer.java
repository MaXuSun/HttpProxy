package server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import utils.Forbid;
import utils.StaticData;

/**
 * 多线程代理服务器，能够实现HTTP的代理并支持以下内容 网站过滤：允许/不允许访问某些网站； 用户过滤：支持/不支持某些用户访问外部网站；
 * 网站引导：将用户对某个网站的访问引导至一个模拟网站（钓 鱼）。
 * 
 * 支持HTTPS的简单代理
 * 
 * @author MaXU
 *
 */
public class ProxyServer extends ServerSocket {
  // 使用线程池管理连接时的socket线程
  ExecutorService threadPool = Executors
      .newFixedThreadPool(StaticData.MAX_THREAD_SIZE);
  // 用于网站过滤和用户过滤
  Forbid forbid = null;
  
  private boolean openPushingSite = false;  //是否开启钓鱼站点设置
  
  public ProxyServer(Forbid forbid) throws IOException {
    super(StaticData.PROXYSERVER_PORT);
    System.out.println("代理服务器开启等待连接……");
    this.forbid = forbid;
    //load();
  }

  /**
   * 代理服务器加载函数
   * @throws IOException
   */
  public void load() throws IOException {
    while (true) {
      Socket socket = this.accept();
      threadPool.execute(new ProxyHandle(socket, this.forbid,openPushingSite));
    }
  }

  public boolean isOpenPushingSite() {
    return openPushingSite;
  }

  public void setOpenPushingSite(boolean openPushingSite) {
    this.openPushingSite = openPushingSite;
  }

  public Forbid getForbid() {
    return forbid;
  }

  public void setForbid(Forbid forbid) {
    this.forbid = forbid;
  }
}
