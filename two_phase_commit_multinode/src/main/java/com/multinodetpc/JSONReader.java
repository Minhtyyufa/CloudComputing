package main.java.com.multinodetpc;


import java.io.FileReader;
import java.io.IOException;


import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

public class JSONReader {
    private JSONParser jsonParser = new JSONParser();
    private JSONObject configObj;

    public JSONReader()  {
        try{
            FileReader reader = new FileReader("config.json");
            Object obj = jsonParser.parse(reader);
            configObj = (JSONObject) obj;
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public String getAttribute(String key){
        return (String) configObj.get(key);
    }
}

