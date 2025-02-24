package DataTypes;

import java.util.HashMap;
import java.util.Map;

public class Vote {
    String author;
    String name;
    String vote_topic;
    int answer_number;
    int votes_count;
    Map<String, Integer> answers;

    public Vote(String author) {
        this.author = author;
        this.votes_count = 0;
        this.answers = new HashMap<String, Integer>();
    }

    public String getAuthor() {
        return author;
    }

    public String getName() {
        return name;
    }

    public String getVote_topic() {
        return vote_topic;
    }

    public int getAnswer_number() {
        return answer_number;
    }

    public int getVotes_count() {
        return votes_count;
    }

    public Map<String, Integer> getAnswers() {
        return answers;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setVote_topic(String vote_topic) {
        this.vote_topic = vote_topic;
    }

    public void setAnswer_number(int answer_number) {
        this.answer_number = answer_number;
    }

    public void setVotes_count(int votes_count) {
        this.votes_count = votes_count;
    }

    public void setAnswers(String answer) {
        answers.put(answer, 0);
    }

    public void incrementAnswer(String answer) {
        answers.put(answer, answers.get(answer) + 1);
        votes_count++;
    }
}
