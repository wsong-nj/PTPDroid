package tool.ontology;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.*;

public class EntityOntologyMap {
    //上位词与下位词的映射
    public static Map<String, List<String>> entityOntologyMap = new HashMap<>();
    //包含的所有第三方实体
    public static Set<String> entityOntologySet = new HashSet<>();

    public static void initEntity(String filename){
        try {
            FileInputStream fin = new FileInputStream(filename);
            InputStreamReader reader = new InputStreamReader(fin);
            BufferedReader buffReader = new BufferedReader(reader);
            String strTmp = "";
            while((strTmp = buffReader.readLine())!=null){
                if(!strTmp.startsWith("//")){
                    String[] strings = strTmp.split(",");
                    if(entityOntologyMap.containsKey(strings[0].trim())){
                        if(entityOntologyMap.get(strings[0].trim()).contains(strings[1].trim())){
                        }else {
                            entityOntologyMap.get(strings[0].trim()).add(strings[1].trim().toLowerCase());
                        }
                    }else {
                        List<String> list = new LinkedList<>();
                        list.add(strings[1].trim().toLowerCase());
                        entityOntologyMap.put(strings[0].trim().toLowerCase(),list);
                    }
                    //entityOntologySet.add(strings[0].trim().toLowerCase());
                    entityOntologySet.add(strings[1].trim().toLowerCase());
                }
            }
            buffReader.close();
            entityOntologySet.remove("advertising");
            entityOntologySet.remove("authentication");
            entityOntologySet.remove("analytics");
            //System.out.println("entityOntologyMap.size():"+entityOntologyMap.size());
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("entity ontology读取出错！"+entityOntologySet.size());
        }
    }

    public static boolean a_isAncestorOf_b(String a,String b){
        if(a.equals(b)){
            return true;
        }
        Deque<String> deque = new LinkedList<>();
        if(entityOntologyMap.containsKey(a)){
            deque.add(a);
        }
        while (!deque.isEmpty()){
            String temp = deque.pollFirst();
            if(temp.equals(b)){
                return true;
            }
            if(entityOntologyMap.containsKey(temp)&&!entityOntologyMap.get(temp).isEmpty()){
                List<String> list = entityOntologyMap.get(temp);
                deque.addAll(list);
            }
        }
        return false;
    }

}
