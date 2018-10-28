package utils;

/**
 * 该类主要用来存储用到的静态常量
 * @author MaXU
 *
 */
public class StaticData {
  public static int PROXYSERVER_PORT = 10240;            //本地代理服务器端口
  public static String PROXYSERVER_IP = "127.0.0.1";     //本地代理服务器地址
  // HTTPS代理成功时，回应CONNECT请求的响应
  public static String SUCCESS = "HTTP/1.1 200 Connection Established\r\n\r\n";
  // HTTP代理,禁止访问某站点时代理服务器发送给客户端的响应
  public static String FORBIDURL_FAIL = "HTTP/1.1 403 Forbidden\r\n\r\n<!DOCTYPE html><html><head><meta charset=\"utf-8\"><title>403 Forbidden</title></head><body><h1>403 Forbidden</h1><p>This site is forbidden to visit!</p></body></html>";
  // HTTP代理,禁止某用户访问外网时代理服务器发送给客户端的响应
  public static String FORBIDUSER_FAIL = "HTTP/1.1 403 Forbidden\r\n\r\n<!DOCTYPE html><html><head><meta charset=\"utf-8\"><title>403 Forbidden</title></head><body><h1>403 Forbidden</h1><p>You are forbidden to visit External network!</p></body></html>";
  // 从服务器端读取字节流时设置的缓存大小
  public static int SERVER_BUFSIZE = 1024*3;
  // 从客户端读取字节流时设置的缓存大小
  public static int CLIENT_BUFSIZE = 1024;
  // 默认的钓鱼站点
  public static String PHISHING_IP = "45.77.112.221";
  public static int PHISHING_PORT = 443;
  // socket连接的时长限制
  public static int TIMEOUT = 3000;
  // 开启的最大线程数
  public static int MAX_THREAD_SIZE = 20;
}
