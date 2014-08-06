package com.citytechinc.maven.plugins.osgicomponentstatus;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.Object;import java.lang.String;import java.net.Authenticator;
import java.net.URL;
import java.util.HashMap;import java.util.Map;

public class ComponentsStatusChecker {

    private final JSONArray data;

    public ComponentsStatusChecker(String login, String password, URL url) throws IOException, ParseException {

        Authenticator.setDefault(new MyAuthenticator(login, password));

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(url.openStream()))) {
            JSONParser jsonParser = new JSONParser();
            JSONObject jsonObject = (JSONObject) jsonParser.parse(reader);
            data = (JSONArray) jsonObject.get("data");
        }

    }

    public void verify(String pakage) throws ComponentsStateException {

        Map<String, String> errors = new HashMap<>();
        for (Object obj : data) {
            JSONObject jsonObject = (JSONObject) obj;

            String pid = (String) jsonObject.get("pid");

            if (pid.startsWith(pakage)) {
                String state = (String) jsonObject.get("state");
                if (!"active".equals(state)) {
                    errors.put(pid, state);
                }
            }

        }

        if (!errors.isEmpty()) {
            throw new ComponentsStateException(errors);
        }

    }


}
