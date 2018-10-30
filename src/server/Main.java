package server;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;

import utils.Forbid;

/**
 * 主函数
 * @author MaXU
 *
 */
public class Main {
  public static void main(String[] args) throws IOException, URISyntaxException {
    Forbid forbid = new Forbid();
    // forbid.addForbidHost("today.hit.edu.cn");
    // forbid.addForbidUser(
    // "_gid=GA1.3.1776937492.1540651529; _ga=GA1.3.279415519.1540651529");
    ProxyServer proxyServer = new ProxyServer(forbid);
    //proxyServer.setOpenPushingSite(true);
    proxyServer.load();
    
//    URL url = new URL("http://today.hit.edu.cn/html/");
//    System.out.println(url.getFile());
//    System.out.println(url.getHost());
//    System.out.println(url.getPath());
//    Path path = Paths.get(url.getHost(),url.getPath());
//    System.err.println(path.getParent());
//    System.err.println(path.getFileName());
//    System.err.println(path);
        
  }
}
