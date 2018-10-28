package server;

import java.io.IOException;

import utils.Forbid;

/**
 * 主函数
 * @author MaXU
 *
 */
public class Main {
  public static void main(String[] args) throws IOException {
    Forbid forbid = new Forbid();
    // forbid.addForbidHost("today.hit.edu.cn");
    // forbid.addForbidUser(
    // "_gid=GA1.3.1776937492.1540651529; _ga=GA1.3.279415519.1540651529");
    ProxyServer proxyServer = new ProxyServer(forbid);
    proxyServer.setOpenPushingSite(false);
  }
}
