package DataTypes;

import java.util.concurrent.ConcurrentHashMap;

public class Topic {
    ConcurrentHashMap<String, Vote> votes;
    int votes_count = 0;

    public Topic() {
        votes = new ConcurrentHashMap<>();
    }

    public void addVote(Vote vote) {
        votes.put(vote.getName(), vote);
    }

    public void incrementVoteCount() {
        votes_count++;
    }

    public void CutVoteCount(int value) {
        votes_count -= value;
    }

    public ConcurrentHashMap<String, Vote> getVotes() {
        return votes;
    }

    public int getVoteCount() {
        return votes_count;
    }

    public void setVotes(ConcurrentHashMap<String, Vote> votes) {
        this.votes = votes;
    }

    public void setVotes_count(int votes_count) {
        this.votes_count = votes_count;
    }
}
