package tool.mapping;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class ipMappingToDNS {
    public static Set<String> IPName = new HashSet<>();
    //[ip,dns]
    public static Map<String,String> ipToDnsMap = new HashMap<>();

    public static void initIp(String filename){
        try {
            FileInputStream in = new FileInputStream(filename);
            InputStreamReader inReader = new InputStreamReader(in, StandardCharsets.UTF_8);
            BufferedReader bufReader = new BufferedReader(inReader);
            String line = null;
            while((line = bufReader.readLine()) != null){
                if(!line.startsWith("//")){
                    String[] strings = line.split(",");
                    ipToDnsMap.put(strings[0].trim(),strings[1].trim().toLowerCase());
                    IPName.add(strings[0].trim());
                }
            }
            bufReader.close();
            inReader.close();
            in.close();
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("ip地址和dns域名的映射关系读取出错！");
        }
    }
    public static String changeToUrl(String ip) {
        if(IPName.contains(ip)){
            return ipToDnsMap.get(ip);
        }else {
            return ip;
        }
    }
}
