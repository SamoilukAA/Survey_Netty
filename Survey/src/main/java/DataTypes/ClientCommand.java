package DataTypes;

import java.util.ArrayList;

public class ClientCommand {
    String command;
    ArrayList<String> parameters;

    public ClientCommand(String request) {
        parameters = new ArrayList<>();
        String[] parts = request.split("-");
        for (int i = 0; i < parts.length; ++i) {
            parts[i] = parts[i].trim();
            if (i == 0) {
                command = parts[i];
            } else {
                parameters.add(parts[i]);
            }
        }
    }

    public String getCommand() {
        return command;
    }

    public ArrayList<String> getParameters() {
        return parameters;
    }
}
