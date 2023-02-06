package tool.mapping;

import tool.ontology.EntityOntologyMap;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

public class apkMappingToEntity {
    //[apk,entity] 全部转化为小写
    public static Map<String,String> apkToEntityMap = new HashMap<>();
    public static void initApi(String filename){
        try {
            FileInputStream in = new FileInputStream(filename);
            InputStreamReader inReader = new InputStreamReader(in, StandardCharsets.UTF_8);
            BufferedReader bufReader = new BufferedReader(inReader);
            String line = null;
            while((line = bufReader.readLine()) != null){
                if(!line.startsWith("//")){
                    String[] strings = line.split(",");
                    apkToEntityMap.put(strings[0].trim().toLowerCase(),strings[1].trim().toLowerCase());
                }
            }
            bufReader.close();
            inReader.close();
            in.close();
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("apk包名与entity的映射关系读取出错！");
        }
    }

    public static boolean isInEntity(String apk){
        for(String str: EntityOntologyMap.entityOntologySet){
            if(apk.toLowerCase().contains(str)){
                return true;
            }
        }
        return false;
    }

    public static boolean isInMapping(String apk){
        return apkToEntityMap.containsKey(apk.toLowerCase().trim());
    }

    public static String changeToEntity(String apk){
        if(isInEntity(apk)){
            for(String str:EntityOntologyMap.entityOntologySet){
                if(apk.toLowerCase().contains(str)){
                    return str;
                }
            }
        }else if(isInMapping(apk)){
            return apkToEntityMap.get(apk.toLowerCase().trim());
        }
        return apk;
    }
}
