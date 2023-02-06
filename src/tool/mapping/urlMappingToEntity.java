package tool.mapping;

import tool.ontology.EntityOntologyMap;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.*;

public class urlMappingToEntity {
    //部分没有显示所属公司名的域名
    public static Set<String> DNSNameSet = new HashSet<>();
    public static Map<String,String> DNSToEntityMap = new HashMap<>();

    public static void init(String filename){
        try {
            FileInputStream fin = new FileInputStream(filename);
            InputStreamReader reader = new InputStreamReader(fin);
            BufferedReader buffReader = new BufferedReader(reader);
            String strTmp = "";
            while((strTmp = buffReader.readLine())!=null){
                if(!strTmp.startsWith("//")){
                    String[] strings = strTmp.split(",");
                    if(!DNSToEntityMap.containsKey(strings[0].trim().toLowerCase())){
                        DNSToEntityMap.put(strings[0].trim().toLowerCase(),strings[1].trim().toLowerCase());
                        DNSNameSet.add(strings[0].trim().toLowerCase());
                    }
                }
            }
            buffReader.close();
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("DNS域名和实体的映射读取出错！");
        }
    }

    public boolean isInEntity(String url){
        for(String str:EntityOntologyMap.entityOntologySet){
            if(url.toLowerCase().contains(str)){
                return true;
            }
        }
        for(String str:DNSNameSet){
            if(url.toLowerCase().contains(str)){
                return true;
            }
        }
        return false;
    }

    public static String changeToEntity(String url){
        for(String str:EntityOntologyMap.entityOntologySet){
            if(url.toLowerCase().contains(str)){
                return str;
            }
        }
        for(String str:DNSNameSet){
            if(url.toLowerCase().contains(str)){
                return str;
            }
        }
        return url;
    }

}
