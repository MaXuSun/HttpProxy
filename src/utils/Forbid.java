package utils;

import java.util.LinkedList;
import java.util.List;

public class Forbid {
  private List<String> forbidUrl = new LinkedList<>();
  private List<String> forbidUser = new LinkedList<>();
  private List<String> forbidHost = new LinkedList<>();
  
  /**
   * 判断禁止的URL里面是否有url
   * @param Url 一个String 变量
   * @return 有的话就返回true,否则返回false
   */
  public boolean containUrl(String Url) {
    return (this.forbidUrl.contains(Url))?true:false;
  }
  /**
   * 将url添加到禁止访问的url中
   * @param Url
   */
  public void addForbidUrl(String Url) {
    this.forbidUrl.add(Url);
  }
  /**
   * 将url从禁止访问中移除
   * @param Url
   */
  public void removeForbidUrl(String Url) {
    this.forbidUrl.remove(Url);
  }
  
  /**
   * 判断禁止的User里面是否有user
   * @param user 一个String变量
   * @return 如果有就返回true,入宫没有就返回false
   */
  public boolean containUser(String user) {
    return (this.forbidUser.contains(user))?true:false;
  }
  
  /**
   * 将user添加到禁止访问的user中
   * @param user
   */
  public void addForbidUser(String user) {
    this.forbidUser.add(user);
  }
  /**
   * 将user从禁止访问的user中移除
   * @param user
   */
  public void removeForbidUser(String user) {
    this.forbidUser.remove(user);
  }
  
  /**
   * 判断禁止的Host里面是否有host
   * @param host 一个String变量
   * @return 如果有就返回true,入宫没有就返回false
   */
  public boolean containHost(String host) {
    return (this.forbidHost.contains(host))?true:false;
  }
  
  /**
   * 将host添加到禁止访问的host中
   * @param host
   */
  public void addForbidHost(String host) {
    this.forbidHost.add(host);
  }
  /**
   * 将host从禁止访问的host中移除
   * @param host
   */
  public void removeForbidHost(String host) {
    this.forbidHost.remove(host);
  }
}
