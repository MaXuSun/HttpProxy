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
  public String getRequest() {
    StringBuilder sBuilder = new StringBuilder();
    for(String i:this.request) {
      sBuilder.append(i).append("\r\n");
    }
    sBuilder.append("\r\n");
    return sBuilder.toString();
  }
  
  public void initRequest(String[] re) {
    this.request = re;
  }
  
  /**
   * 移除一个请求头中的某行
   * @param key
   */
  public void removeRequest(String key) {
    boolean in = false;
    for(int i = 0;i < request.length;i++) {
      if(request[i].contains(key)) {
        in = true;
        break;
      }
    }
    String[] temp = null;
    if(in) {
         temp = new String[request.length-1];
    }else {
        temp = new String[request.length];
    }
    int k = 0;
    for(int i = 0;i < request.length;i++) {
      if(request[i].contains(key)) {
        continue;
      }
      temp[k++] = request[i];
    }
    request = temp;
  }
  
  /**
   * 向请求头中添加某行
   * @param key
   * @param value
   */
  public void setRequest(String key,String value) {
    for(int i = 0;i < request.length;i++) {
      if(request[i].contains(key)) {
        String[] strings = request[i].split(":");
        strings[1] = value;
        request[i] = strings[0] +": "+strings[1];
        return;
      }
    }
    
    String[] temp= new String[request.length+1];
    for(int i = 0;i < request.length;i++) {
      temp[i] = request[i];
    }
    temp[temp.length-1] = key+": "+value; 
    this.request = temp;
  }
}
