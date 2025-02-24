package Server;

import DataTypes.*;
import io.netty.channel.*;
import io.netty.util.internal.StringUtil;

import java.util.ArrayList;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class ProcessingHandler extends ChannelInboundHandlerAdapter {
    private String username = "";
    private ChannelFuture future;
    private ResponseData responseData;
    boolean create_vote_flag = false;
    boolean vote_flag = false;
    private String topic;
    private String vote_name;
    private Vote vote;
    int count = -1;

    private void getLogin(ChannelHandlerContext ctx, ClientCommand command) {
        ArrayList<String> params = command.getParameters();
        if (params.size() == 1) {
            username = StringUtil.substringAfter(params.getFirst(), '=');
            responseData.setResponse("You are in, " + username);
            future = ctx.writeAndFlush(responseData);
        } else {
            responseData.setResponse("Not a valid command");
            future = ctx.writeAndFlush(responseData);
        }
    }

    private void createTopic(ChannelHandlerContext ctx, ClientCommand command) {
        ArrayList<String> params = command.getParameters();
        String topic = StringUtil.substringAfter(params.getFirst(), '=');
        if (NettyServer.survey.containsKey(topic)) {
            responseData.setResponse("The topic already exists!");
            future = ctx.writeAndFlush(responseData);
        } else {
            NettyServer.survey.put(topic, new Topic());
            responseData.setResponse("The topic is created");
            future = ctx.writeAndFlush(responseData);
        }
    }

    private void createVote(ChannelHandlerContext ctx, ClientCommand command) {
        ArrayList<String> params = command.getParameters();
        topic = StringUtil.substringAfter(params.getFirst(), '=');
        vote = new Vote(username);
    }

    private void constructVote(ChannelHandlerContext ctx, Object msg) {
        RequestData requestData = (RequestData)msg;
        String command = requestData.getRequest();
        switch (count) {
            case 0:
                Topic section = NettyServer.survey.get(topic);
                ConcurrentHashMap<String, Vote> map_votes = section.getVotes();
                if (map_votes.containsKey(command)) {
                    responseData.setResponse("The vote already exists!");
                } else {
                    vote.setName(command);
                    count++;
                    responseData.setResponse("Enter vote topic: ");
                }
                future = ctx.writeAndFlush(responseData);
                break;
            case 1:
                vote.setVote_topic(command);
                count++;
                responseData.setResponse("Enter answer count: ");
                future = ctx.writeAndFlush(responseData);
                break;
            case 2:
                vote.setAnswer_number(Integer.parseInt(command));
                count++;
                responseData.setResponse("Enter answers separated ';': ");
                future = ctx.writeAndFlush(responseData);
                break;
            case 3:
                String[] variants = command.split(";");
                for (String variant : variants) {
                    vote.setAnswers(variant.trim());
                }
                NettyServer.survey.get(topic).addVote(vote);
                responseData.setResponse("The vote is created");
                future = ctx.writeAndFlush(responseData);
                count = -1;
                create_vote_flag = false;
                break;
        }
    }

    private void view(ChannelHandlerContext ctx, ClientCommand command) {
        ArrayList<String> params = command.getParameters();
        if (params.isEmpty()) {
            StringBuilder response = new StringBuilder();
            for (ConcurrentHashMap.Entry<String, Topic> elem : NettyServer.survey.entrySet()) {
                response.append(elem.getKey())
                        .append(" (votes in topic = ")
                        .append(elem.getValue().getVoteCount())
                        .append(")\n");
            }
            if (response.isEmpty()) {
                responseData.setResponse("There are no topics yet");
            } else {
                responseData.setResponse(response.toString());
            }
            future = ctx.writeAndFlush(responseData);
        } else {
            params.replaceAll(value -> StringUtil.substringAfter(value, '='));
            if (NettyServer.survey.containsKey(params.getFirst())) {
                Topic section = NettyServer.survey.get(params.getFirst());
                ConcurrentHashMap<String, Vote> map_votes = section.getVotes();
                if (params.size() == 1) {
                    StringBuilder response = new StringBuilder();
                    for (ConcurrentHashMap.Entry<String, Vote> elem : map_votes.entrySet()) {
                        response.append(elem.getKey())
                                .append(" (votes in topic = ")
                                .append(elem.getValue().getVotes_count())
                                .append(")\n");
                    }
                    if (response.isEmpty()) {
                        responseData.setResponse("There are no votes yet");
                    } else {
                        responseData.setResponse(response.toString());
                    }
                    future = ctx.writeAndFlush(responseData);
                } else if (params.size() == 2) {
                    if (map_votes.containsKey(params.get(1))) {
                        Vote vote_elem = map_votes.get(params.get(1));
                        StringBuilder response = new StringBuilder(vote_elem.getVote_topic() + "\n");
                        for (Map.Entry<String, Integer> elem : vote_elem.getAnswers().entrySet()) {
                            response.append(elem.getKey())
                                    .append(" (votes in answer = ")
                                    .append(elem.getValue())
                                    .append(")\n");
                        }
                        responseData.setResponse(response.toString());
                    } else {
                        responseData.setResponse("The vote does not exists!");
                    }
                    future = ctx.writeAndFlush(responseData);
                } else {
                    responseData.setResponse("Wrong command!");
                    future = ctx.writeAndFlush(responseData);
                }
            } else {
                responseData.setResponse("The topic does not exists!");
                future = ctx.writeAndFlush(responseData);
            }
        }
    }

    private void make_vote(ChannelHandlerContext ctx, ClientCommand command) {
        StringBuilder response = new StringBuilder();
        ArrayList<String> params = command.getParameters();
        if (params.size() == 2) {
            topic = StringUtil.substringAfter(params.getFirst(), '=');
            vote_name = StringUtil.substringAfter(params.get(1), '=');
            if (NettyServer.survey.containsKey(topic)) {
                Topic section = NettyServer.survey.get(topic);
                ConcurrentHashMap<String, Vote> map_votes = section.getVotes();
                if (map_votes.containsKey(vote_name)) {
                    Vote vote_elem = map_votes.get(vote_name);
                    for (String v : vote_elem.getAnswers().keySet()) {
                        response.append(v).append("\n");
                    }
                    response.append("Choose one answer:");
                    responseData.setResponse(response.toString());
                } else {
                    responseData.setResponse("The vote does not exists!");
                    vote_flag = false;
                }
            } else {
                responseData.setResponse("The topic does not exists!");
                vote_flag = false;
            }
        } else {
            responseData.setResponse("Not a valid command");
            vote_flag = false;
        }
        future = ctx.writeAndFlush(responseData);
    }

    private void save_vote(ChannelHandlerContext ctx, Object msg) {
        RequestData requestData = (RequestData)msg;
        String command = requestData.getRequest();
        NettyServer.survey.get(topic).getVotes().get(vote_name).incrementAnswer(command);
        NettyServer.survey.get(topic).incrementVoteCount();
        responseData.setResponse("The answer is recorded");
        future = ctx.writeAndFlush(responseData);
        vote_flag = false;
    }

    private void delete_vote(ChannelHandlerContext ctx, ClientCommand command) {
        ArrayList<String> params = command.getParameters();
        if (params.size() == 2) {
            topic = StringUtil.substringAfter(params.getFirst(), '=');
            vote_name = StringUtil.substringAfter(params.get(1), '=');
            if (NettyServer.survey.containsKey(topic)) {
                Topic section = NettyServer.survey.get(topic);
                ConcurrentHashMap<String, Vote> map_votes = section.getVotes();
                if (map_votes.containsKey(vote_name)) {
                    if (username.equals(map_votes.get(vote_name).getAuthor())) {
                        NettyServer.survey.get(topic)
                                        .CutVoteCount(NettyServer.survey.get(topic)
                                        .getVotes().get(vote_name).getVotes_count());
                        NettyServer.survey.get(topic).getVotes().remove(vote_name);
                        responseData.setResponse("The vote deleted");
                        future = ctx.writeAndFlush(responseData);
                    } else {
                        responseData.setResponse("You cannot delete this vote!");
                        future = ctx.writeAndFlush(responseData);
                    }
                } else {
                    responseData.setResponse("The vote does not exists!");
                    future = ctx.writeAndFlush(responseData);
                }
            } else {
                responseData.setResponse("The topic does not exists!");
                future = ctx.writeAndFlush(responseData);
            }
        } else {
            responseData.setResponse("Not a valid command");
            future = ctx.writeAndFlush(responseData);
        }
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        NettyServer.logger.info("Received message: {}", msg.toString());
        responseData = new ResponseData();
        if (vote_flag) {
            save_vote(ctx, msg);
        } else if (create_vote_flag) {
            constructVote(ctx, msg);
        } else {
            RequestData requestData = (RequestData)msg;
            String command = requestData.getRequest();
            ClientCommand clientCommand = new ClientCommand(command);
            if (clientCommand.getCommand().equals("login")) {
                getLogin(ctx, clientCommand);
            } else {
                if (username.isEmpty()) {
                    responseData.setResponse("Log in first");
                    future = ctx.writeAndFlush(responseData);
                } else {
                    switch (clientCommand.getCommand()) {
                        case "create topic":
                            createTopic(ctx, clientCommand);
                            break;
                        case "create vote":
                            ArrayList<String> params = clientCommand.getParameters();
                            String section = StringUtil.substringAfter(params.getFirst(), '=');
                            if (!NettyServer.survey.containsKey(section)) {
                                responseData.setResponse("The topic does not exists!");
                            } else {
                                create_vote_flag = true;
                                count++;
                                createVote(ctx, clientCommand);
                                responseData.setResponse("Enter vote name: ");
                            }
                            future = ctx.writeAndFlush(responseData);
                            break;
                        case "view":
                            view(ctx, clientCommand);
                            break;
                        case "vote":
                            vote_flag = true;
                            make_vote(ctx, clientCommand);
                            break;
                        case "delete":
                            delete_vote(ctx, clientCommand);
                            break;
                        default:
                            responseData.setResponse("Not a valid command");
                            future = ctx.writeAndFlush(responseData);
                            break;
                    }
                }
            }
        }
        NettyServer.logger.info("Sent message: {}", responseData.toString());
    }
}
