package server;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import utils.StaticData;
import utils.Utils;

/**
 * 新开的线程用于在代理HTTPS时同步将服务器端数据转发给客户端
 * @author MaXU
 *
 */
public class HttpsTask implements Runnable{
  InputStream inputStream;
  OutputStream outputStream;
  Utils utils = new Utils();
  boolean done = false;

  public HttpsTask(InputStream inputStream, OutputStream outputStream) {
    this.inputStream = inputStream;
    this.outputStream = outputStream;
  }
  public boolean getDone() {
    return this.done;
  }

  @Override
  public void run() {
    try {
      System.out.println("fasfasdfasdgagadg");
      utils.fromInputToOutput(inputStream, outputStream,
          StaticData.CLIENT_BUFSIZE);
      done = true;
    } catch (IOException e) {
    }
  }
}
