package utils;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;

public class Utils {

  /**
   * 从http请求中提取需要的HttpHeader内容
   * 
   * @param request
   * @return
   */
  public HttpHeader parseHeader(String request) {
    // System.out.println(request);
    HttpHeader header = new HttpHeader();
    String[] strings = request.split("\r\n");
    String[] firstLine = strings[0].split(" ");
    String[] map;
    header.setMethod(firstLine[0]);
    if (firstLine.length > 1) {
      header.setUrl(firstLine[1]);
      header.setPort(80);
    }
    for (int i = 0; i < strings.length; i++) {
      if (strings[i].contains("Host:")) {
        if (strings[i].contains(": ")) {
          map = strings[i].split(": ");
        } else {
          map = strings[i].split(":");
        }
        header.setHost(map[1]);
      } else if (strings[i].contains("Cookie:")) {
        if (strings[i].contains(": ")) {
          map = strings[i].split(": ");
        } else {
          map = strings[i].split(":");
        }
        header.setCookie(map[1]);
      }
    }
    if (header.getMethod().equalsIgnoreCase("CONNECT")) {
      map = header.getUrl().split(":");
      header.setPort(Integer.parseInt(map[1]));
      header.setHost(map[0]);
    }
    header.initRequest(strings);
    return header;
  }

  /**
   * 从一个socket的BufferedReader里面按行读取整个HTTP或者HTTPS报文内容
   * 
   * @param br
   *          一个对socket的InputSream进行封装的BufferedReader
   * @return
   * @throws IOException
   */
  public String readAllContent(BufferedReader br) {
    StringBuilder sb = new StringBuilder();
    String line;
    try {
      line = br.readLine();
      while (line != null && !line.isEmpty()) {
        sb.append(line).append("\r\n");
        line = br.readLine();
      }
      sb.append(line).append("\r\n");
    } catch (IOException e) {
      System.err.println("Error reading from this stream!");
    }
    return sb.toString();
  }

  /**
   * 如果最后一个参数为null就只将inputstream中读取到的内容传给第二个输出流outputStream,否则将从inputstream中读取的内容同时传给两个输出流
   * 
   * @param inputStream
   * @param outputStream
   * @param bufSize                使用的缓存大小
   * @param fileOutStream
   * @throws IOException
   */
  public void fromInputToOutput(InputStream inputStream,
      OutputStream outputStream, int bufSize, OutputStream fileOutStream)
      throws IOException {
    byte[] buffer = new byte[bufSize];
    int size = 0;
    while ((size = inputStream.read(buffer)) != -1) {
      if (fileOutStream != null) {
        fileOutStream.write(buffer, 0, size);
        fileOutStream.flush();
      }
      outputStream.write(buffer, 0, size);
      outputStream.flush();
    }
  }

  /**
   * 从一个String的URL中解析得到一个Path类
   * 
   * @param Strurl
   * @return
   * @throws MalformedURLException
   */
  public Path getPathFromURL(String Strurl, String father)
      throws MalformedURLException {
    URL url = new URL(Strurl);
    Path path = null;
    if (url.getPath().endsWith("/")) {
      path = Paths.get(father, url.getHost(), url.getPath(), "index.html");
    } else {
      path = Paths.get(father, url.getHost(), url.getPath());
    }
    return path;
  }

  /**
   * 根据传入的path路径，如果该文件不存在就分级创建文件，并返回该文件对象； 如果存在则直接返回该文件对象
   * 
   * @param path
   *          文件的path路径
   * @return
   * @throws IOException
   */
  public File createFile(Path path) throws IOException {
    File parent = path.getParent().toFile();
    File file = path.toFile();
    if (!parent.exists() || !parent.isDirectory()) {
      parent.mkdirs();
    }
    if (!file.exists() && file.isFile()) {
      file.createNewFile();
    }

    return file;
  }

  /**
   * 从某流中读取一行并返回其得到的字符串,该方法读取一行的判断条件是读取到了一行的结束字符'\n' 并将\n放到返回结果
   * 比如有'\r\n'结尾的字符串，则将\n前面包括\n全部读取并返回
   * 
   * @param iStream
   *          一个字节流
   * @return 一个字符串
   * @throws IOException
   */
  public String readLineByIs(InputStream iStream) throws IOException {
    StringBuilder sb = new StringBuilder();
    int i = 0;
    while ((i = iStream.read()) != -1) {
      sb.append((char) i);
      if (i == '\n') {
        break;
      }
    }
    return sb.toString();
  }
}
