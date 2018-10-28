package utils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

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
    header.setRequest(strings);
    return header;
  }

  /**
   * 从一个socket的BufferedReader里面按行读取整个HTTP或者HTTPS报文内容
   * 
   * @param br  一个对socket的InputSream进行封装的BufferedReader
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
   * 将InputStream中读取的流直接发送给OutputStream
   * 
   * @param inputStream
   * @param outputStream
   * @param size
   *          设置读取时的缓冲区大小
   * @throws IOException
   */
  public void fromInputToOutput(InputStream inputStream,
      OutputStream outputStream, int bufSize) throws IOException {
    byte[] buffer = new byte[bufSize];
    int size = 0;
    while ((size = inputStream.read(buffer)) != -1) {
      //System.out.println(new String(buffer, "utf8"));
      outputStream.write(buffer, 0, size);
      outputStream.flush();
    }
  }
}
