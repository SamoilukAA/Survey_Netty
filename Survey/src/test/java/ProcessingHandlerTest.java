import DataTypes.RequestData;
import DataTypes.ResponseData;
import Server.NettyServer;
import Server.ProcessingHandler;
import Server.RequestDataDecoder;
import Client.ResponseDataDecoder;
import io.netty.channel.embedded.EmbeddedChannel;
import org.junit.jupiter.api.Test;

import java.util.concurrent.ConcurrentHashMap;

import static org.junit.jupiter.api.Assertions.*;

public class ProcessingHandlerTest {
    @Test
    void testLoginCorrect() {
        EmbeddedChannel channel = new EmbeddedChannel(
                new RequestDataDecoder(),
                new ResponseDataDecoder(),
                new ProcessingHandler()
        );

        NettyServer.survey = new ConcurrentHashMap<>();
        RequestData requestData = new RequestData();
        ResponseData responseData;

        requestData.setRequest("login -u=nastya");
        channel.writeInbound(requestData);
        responseData = channel.readOutbound();
        assertEquals("You are in, nastya", responseData.getResponse());

        requestData.setRequest("login");
        channel.writeInbound(requestData);
        responseData = channel.readOutbound();
        assertEquals("Not a valid command", responseData.getResponse());
        channel.close();
    }
    @Test
    void testCannotRequestBeforeLogin() {
        EmbeddedChannel channel = new EmbeddedChannel(
                new RequestDataDecoder(),
                new ResponseDataDecoder(),
                new ProcessingHandler()
        );

        NettyServer.survey = new ConcurrentHashMap<>();
        RequestData requestData = new RequestData();
        ResponseData responseData;

        requestData.setRequest("view");
        channel.writeInbound(requestData);
        responseData = channel.readOutbound();
        assertEquals("Log in first", responseData.getResponse());
        channel.close();
    }

    @Test
    void testViewWhenNoTopics() {
        EmbeddedChannel channel = new EmbeddedChannel(
                new RequestDataDecoder(),
                new ResponseDataDecoder(),
                new ProcessingHandler()
        );

        NettyServer.survey = new ConcurrentHashMap<>();
        RequestData requestData = new RequestData();
        ResponseData responseData;

        requestData.setRequest("login -u=nastya");
        channel.writeInbound(requestData);
        channel.readOutbound();

        requestData.setRequest("view");
        channel.writeInbound(requestData);
        responseData = channel.readOutbound();
        assertEquals("There are no topics yet", responseData.getResponse());
        channel.close();
    }

    @Test
    void testCreateTopicCorrect() {
        EmbeddedChannel channel = new EmbeddedChannel(
                new RequestDataDecoder(),
                new ResponseDataDecoder(),
                new ProcessingHandler()
        );

        NettyServer.survey = new ConcurrentHashMap<>();
        RequestData requestData = new RequestData();
        ResponseData responseData;

        requestData.setRequest("login -u=nastya");
        channel.writeInbound(requestData);
        channel.readOutbound();

        requestData.setRequest("create topic -n=Work");
        channel.writeInbound(requestData);
        responseData = channel.readOutbound();
        assertEquals("The topic is created", responseData.getResponse());
        channel.close();
    }

    @Test
    void testNotCreateSameTopic() {
        EmbeddedChannel channel = new EmbeddedChannel(
                new RequestDataDecoder(),
                new ResponseDataDecoder(),
                new ProcessingHandler()
        );

        NettyServer.survey = new ConcurrentHashMap<>();
        RequestData requestData = new RequestData();
        ResponseData responseData;

        requestData.setRequest("login -u=nastya");
        channel.writeInbound(requestData);
        channel.readOutbound();

        requestData.setRequest("create topic -n=Animals");
        channel.writeInbound(requestData);
        channel.readOutbound();

        requestData.setRequest("create topic -n=Animals");
        channel.writeInbound(requestData);
        responseData = channel.readOutbound();
        assertEquals("The topic already exists!", responseData.getResponse());
        channel.close();
    }

    @Test
    void testNotCreateVoteIfTopicDoesNotExists() {
        EmbeddedChannel channel = new EmbeddedChannel(
                new RequestDataDecoder(),
                new ResponseDataDecoder(),
                new ProcessingHandler()
        );

        NettyServer.survey = new ConcurrentHashMap<>();
        RequestData requestData = new RequestData();
        ResponseData responseData;

        requestData.setRequest("login -u=nastya");
        channel.writeInbound(requestData);
        channel.readOutbound();

        requestData.setRequest("create vote -n=Sport");
        channel.writeInbound(requestData);
        responseData = channel.readOutbound();
        assertEquals("The topic does not exists!", responseData.getResponse());
        channel.close();
    }

    @Test
    void testCreateVoteCorrect() {
        EmbeddedChannel channel = new EmbeddedChannel(
                new RequestDataDecoder(),
                new ResponseDataDecoder(),
                new ProcessingHandler()
        );

        NettyServer.survey = new ConcurrentHashMap<>();
        RequestData requestData = new RequestData();
        ResponseData responseData;

        requestData.setRequest("login -u=nastya");
        channel.writeInbound(requestData);
        channel.readOutbound();

        requestData.setRequest("create topic -n=Sport");
        channel.writeInbound(requestData);
        channel.readOutbound();

        requestData.setRequest("create vote -n=Sport");
        channel.writeInbound(requestData);
        responseData = channel.readOutbound();
        assertEquals("Enter vote name: ", responseData.getResponse());

        requestData.setRequest("Favorite sport");
        channel.writeInbound(requestData);
        responseData = channel.readOutbound();
        assertEquals("Enter vote topic: ", responseData.getResponse());

        requestData.setRequest("Choose your favorite sport");
        channel.writeInbound(requestData);
        responseData = channel.readOutbound();
        assertEquals("Enter answer count: ", responseData.getResponse());

        requestData.setRequest("3");
        channel.writeInbound(requestData);
        responseData = channel.readOutbound();
        assertEquals("Enter answers separated ';': ", responseData.getResponse());

        requestData.setRequest("Football; Hockey; Tennis");
        channel.writeInbound(requestData);
        responseData = channel.readOutbound();
        assertEquals("The vote is created", responseData.getResponse());

        requestData.setRequest("create vote -n=Sport");
        channel.writeInbound(requestData);
        channel.readOutbound();

        requestData.setRequest("Favorite sport");
        channel.writeInbound(requestData);
        responseData = channel.readOutbound();
        assertEquals("The vote already exists!", responseData.getResponse());
        channel.close();
    }

    @Test
    void testViewTopicCorrect() {
        EmbeddedChannel channel = new EmbeddedChannel(
                new RequestDataDecoder(),
                new ResponseDataDecoder(),
                new ProcessingHandler()
        );

        NettyServer.survey = new ConcurrentHashMap<>();
        RequestData requestData = new RequestData();
        ResponseData responseData;

        requestData.setRequest("login -u=nastya");
        channel.writeInbound(requestData);
        channel.readOutbound();

        requestData.setRequest("create topic -n=Fruit");
        channel.writeInbound(requestData);
        channel.readOutbound();

        requestData.setRequest("view -t=Fruit");
        channel.writeInbound(requestData);
        responseData = channel.readOutbound();
        assertEquals("There are no votes yet", responseData.getResponse());

        requestData.setRequest("create vote -n=Fruit");
        channel.writeInbound(requestData);
        channel.readOutbound();

        requestData.setRequest("Favorite fruit");
        channel.writeInbound(requestData);
        channel.readOutbound();

        requestData.setRequest("Choose your favorite fruit");
        channel.writeInbound(requestData);
        channel.readOutbound();

        requestData.setRequest("3");
        channel.writeInbound(requestData);
        channel.readOutbound();

        requestData.setRequest("Apple; Pear; Orange");
        channel.writeInbound(requestData);
        channel.readOutbound();

        requestData.setRequest("view");
        channel.writeInbound(requestData);
        responseData = channel.readOutbound();
        assertEquals("Fruit (votes in topic = 0)\n", responseData.getResponse());

        requestData.setRequest("view -t=Food");
        channel.writeInbound(requestData);
        responseData = channel.readOutbound();
        assertEquals("The topic does not exists!", responseData.getResponse());

        requestData.setRequest("view -t=Fruit");
        channel.writeInbound(requestData);
        responseData = channel.readOutbound();
        assertEquals("Favorite fruit (votes in topic = 0)\n", responseData.getResponse());

        requestData.setRequest("view -t=Fruit -v=Favorite fruit");
        channel.writeInbound(requestData);
        responseData = channel.readOutbound();
        assertEquals("Choose your favorite fruit\n" +
                "Apple (votes in answer = 0)\n" +
                "Pear (votes in answer = 0)\n" +
                "Orange (votes in answer = 0)\n", responseData.getResponse());

        requestData.setRequest("view -t=Fruit -v=Citrus");
        channel.writeInbound(requestData);
        responseData = channel.readOutbound();
        assertEquals("The vote does not exists!", responseData.getResponse());

        requestData.setRequest("view -t=Fruit -v=Favorite fruit -n=Apple");
        channel.writeInbound(requestData);
        responseData = channel.readOutbound();
        assertEquals("Wrong command!", responseData.getResponse());
        channel.close();
    }

    @Test
    void testMakeVoteCorrect() {
        EmbeddedChannel channel = new EmbeddedChannel(
                new RequestDataDecoder(),
                new ResponseDataDecoder(),
                new ProcessingHandler()
        );

        NettyServer.survey = new ConcurrentHashMap<>();
        RequestData requestData = new RequestData();
        ResponseData responseData;

        requestData.setRequest("login -u=nastya");
        channel.writeInbound(requestData);
        channel.readOutbound();

        requestData.setRequest("vote");
        channel.writeInbound(requestData);
        responseData = channel.readOutbound();
        assertEquals("Not a valid command", responseData.getResponse());

        requestData.setRequest("vote -t=Cars -v=Your car");
        channel.writeInbound(requestData);
        responseData = channel.readOutbound();
        assertEquals("The topic does not exists!", responseData.getResponse());

        requestData.setRequest("create topic -n=Cars");
        channel.writeInbound(requestData);
        channel.readOutbound();

        requestData.setRequest("create vote -t=Cars");
        channel.writeInbound(requestData);
        channel.readOutbound();

        requestData.setRequest("Your car");
        channel.writeInbound(requestData);
        channel.readOutbound();

        requestData.setRequest("What brand of car do you have?");
        channel.writeInbound(requestData);
        channel.readOutbound();

        requestData.setRequest("3");
        channel.writeInbound(requestData);
        channel.readOutbound();

        requestData.setRequest("Mercedes; Audi; Volkswagen");
        channel.writeInbound(requestData);
        channel.readOutbound();

        requestData.setRequest("vote -t=Cars -v=Your car");
        channel.writeInbound(requestData);
        responseData = channel.readOutbound();
        assertEquals("Volkswagen\n" +
                "Audi\n" +"Mercedes\n" + "Choose one answer:", responseData.getResponse());

        requestData.setRequest("Audi");
        channel.writeInbound(requestData);
        responseData = channel.readOutbound();
        assertEquals("The answer is recorded", responseData.getResponse());
    }

    @Test
    void testDeleteCorrect() {
        EmbeddedChannel channel = new EmbeddedChannel(
                new RequestDataDecoder(),
                new ResponseDataDecoder(),
                new ProcessingHandler()
        );

        NettyServer.survey = new ConcurrentHashMap<>();
        RequestData requestData = new RequestData();
        ResponseData responseData;

        requestData.setRequest("login -u=nastya");
        channel.writeInbound(requestData);
        channel.readOutbound();

        requestData.setRequest("create topic -n=Books");
        channel.writeInbound(requestData);
        channel.readOutbound();

        requestData.setRequest("create vote -t=Books");
        channel.writeInbound(requestData);
        channel.readOutbound();

        requestData.setRequest("Favorite book");
        channel.writeInbound(requestData);
        channel.readOutbound();

        requestData.setRequest("Your favorite Book");
        channel.writeInbound(requestData);
        channel.readOutbound();

        requestData.setRequest("2");
        channel.writeInbound(requestData);
        channel.readOutbound();

        requestData.setRequest("Harry Potter; Lord of the rings");
        channel.writeInbound(requestData);
        channel.readOutbound();

        requestData.setRequest("login -u=nikita");
        channel.writeInbound(requestData);
        channel.readOutbound();

        requestData.setRequest("delete -t=Books -v=Favorite book");
        channel.writeInbound(requestData);
        responseData = channel.readOutbound();
        assertEquals("You cannot delete this vote!", responseData.getResponse());

        requestData.setRequest("login -u=nastya");
        channel.writeInbound(requestData);
        channel.readOutbound();

        requestData.setRequest("delete -t=Books");
        channel.writeInbound(requestData);
        responseData = channel.readOutbound();
        assertEquals("Not a valid command", responseData.getResponse());

        requestData.setRequest("delete -t=Film -v=Favorite film");
        channel.writeInbound(requestData);
        responseData = channel.readOutbound();
        assertEquals("The topic does not exists!", responseData.getResponse());

        requestData.setRequest("delete -t=Books -v=Harry Potter");
        channel.writeInbound(requestData);
        responseData = channel.readOutbound();
        assertEquals("The vote does not exists!", responseData.getResponse());

        requestData.setRequest("delete -t=Books -v=Favorite book");
        channel.writeInbound(requestData);
        responseData = channel.readOutbound();
        assertEquals("The vote deleted", responseData.getResponse());
    }
}
