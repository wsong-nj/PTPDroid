package tool.mapping;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

public class apiMappingToPrivacy {
    //[api,ontology]
    public static Map<String,String> apiToOntologyMap = new HashMap<>();
    public static void initApi(String filename){
        try {
            FileInputStream in = new FileInputStream(filename);
            InputStreamReader inReader = new InputStreamReader(in, StandardCharsets.UTF_8);
            BufferedReader bufReader = new BufferedReader(inReader);
            String line = null;
            while((line = bufReader.readLine()) != null){
                if(!line.startsWith("//")){
                    String[] strings = line.split(";");
                    apiToOntologyMap.put(strings[0].trim(),strings[1].trim().toLowerCase());
                }
            }
            bufReader.close();
            inReader.close();
            in.close();
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("api方法与ontology的映射关系读取出错！"+"apiToOntologyMap.size():"+apiToOntologyMap.size());
        }
    }

    public static String changeToOntology(String api){
        return apiToOntologyMap.getOrDefault(api.trim(), api);
    }
}
