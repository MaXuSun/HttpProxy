package utils;

public class HttpHeader {
  private String host;
  private int port;
  private String method;
  private String cookie;
  private String url;
  private String[] request;
  public String getHost() {
    return host;
  }
  public void setHost(String host) {
    this.host = host;
  }
  public int getPort() {
    return port;
  }
  public void setPort(int port) {
    this.port = port;
  }
  public String getMethod() {
    return method;
  }
  public void setMethod(String method) {
    this.method = method;
  }
  public String getCookie() {
    return cookie;
  }
  public void setCookie(String cookie) {
    this.cookie = cookie;
  }
  public String getUrl() {
    return url;
  }
  public void setUrl(String url) {
    this.url = url;
  }
  public String[] getRequest() {
    return request;
  }
  public void setRequest(String[] reque) {
    this.request = reque;
  }
}
