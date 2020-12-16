package me.potato.demo;

import io.quarkus.test.common.http.TestHTTPResource;
import io.quarkus.test.junit.QuarkusTest;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import javax.websocket.*;
import java.net.URI;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.TimeUnit;

@Slf4j
@QuarkusTest
class ChatSocketTest {
  private static final LinkedBlockingDeque<String> MESSAGES=new LinkedBlockingDeque<>();

  @TestHTTPResource("/chat/stu")
  URI uri;

  @Test
  public void testWebsocketChat() throws Exception {
    Endpoint             endpoint;
    ClientEndpointConfig clientEndpointConfiguration;
    try(Session session=ContainerProvider.getWebSocketContainer()
                                         .connectToServer(Client.class, uri)) {
      Assertions.assertEquals("CONNECT", MESSAGES.poll(10, TimeUnit.SECONDS));
      Assertions.assertEquals("User stu joined", MESSAGES.poll(10, TimeUnit.SECONDS));
      Assertions.assertEquals(">> stu: _ready_", MESSAGES.poll(10, TimeUnit.SECONDS));
      session.getAsyncRemote()
             .sendText("Hello World");
      Assertions.assertEquals(">> stu: Hello World", MESSAGES.poll(10, TimeUnit.SECONDS));
    }
  }

  @ClientEndpoint
  public static class Client {
    @OnOpen
    public void open(Session session) {
      MESSAGES.add("CONNECT");
      session.getAsyncRemote()
             .sendText("_ready_");
    }

    @OnMessage
    void message(String msg) {
      log.info(msg);
      MESSAGES.add(msg);
    }
  }
}