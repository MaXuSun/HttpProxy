package server;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Path;

import javax.swing.text.Utilities;

import utils.Forbid;
import utils.Utils;

/**
 * 主函数
 * @author MaXU
 *
 */
public class Main {
  public static void main(String[] args) throws IOException, URISyntaxException {
    Forbid forbid = new Forbid();
    //forbid.addForbidHost("today.hit.edu.cn");
     //forbid.addForbidUser(
     //"_gid=GA1.3.1776937492.1540651529; _ga=GA1.3.279415519.1540651529");
    ProxyServer proxyServer = new ProxyServer(forbid);
    proxyServer.setOpenPushingSite(true);
    proxyServer.load();
  }
}
