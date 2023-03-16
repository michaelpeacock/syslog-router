package mil.army.rcce.kstreamrouterdatagen.service;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import javax.validation.constraints.NotNull;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

@Service
public class LogMessageGenerator {

    Logger logger = LoggerFactory.getLogger(LogMessageGenerator.class);

    private List<JsonNode> logMessageList;
    private int index;

    //@NotNull
    @Value("${application.configs.data.file.location}")
    private String testDataFile;


    @PostConstruct
    private void loadDataFileToList() {
        //String testDataFile = "data/messages";
        index = 0;
        try {
            logMessageList = readFileFromClasspath(testDataFile);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

    }


    private List<JsonNode> readFileFromClasspath(String filenameWithPath)
            throws IOException {
        List<JsonNode> logList = new ArrayList();
        ObjectMapper mapper = new ObjectMapper();
        mapper.configure(JsonParser.Feature.ALLOW_BACKSLASH_ESCAPING_ANY_CHARACTER, true);
        logger.info(String.format("Starting to read data from file %s", filenameWithPath));

        InputStream inputStream = null;
        try {
            ClassLoader classLoader = getClass().getClassLoader();
            inputStream = classLoader.getResourceAsStream(filenameWithPath);

            BufferedReader br = new BufferedReader(new InputStreamReader(inputStream));
            String line;
            while ((line = br.readLine()) != null) {
               logList.add(mapper.readTree(line));
               logger.info("Read Line::" + line);
            }
            logger.info(String.format("Read total of %s log lines from source file", logList.size()));
        } finally {
            if (inputStream != null) {
                try {
                    inputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return logList;
    }


    private int getIndex() {
        return index++;
    }

    public JsonNode getNextMessage() {
        JsonNode message = logMessageList.get(index);
        index++;
        return message;
    }

    public JsonNode getMessage(int i) {
        JsonNode message = logMessageList.get(index);
        return message;
    }

    public int getMessageCount() {
        return logMessageList.size();
    }


}
