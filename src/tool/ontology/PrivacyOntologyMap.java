package tool.ontology;



import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.*;

public class PrivacyOntologyMap {
    public static Set<String> privacyOntologySet = new HashSet<>();
    public static Map<String,List<String>> privacyOntologyMap = new HashMap<>();

    public static void initPrivacy(String filename){
        try {
            privacyOntologySet.add("information");
            privacyOntologyMap.put("information",new LinkedList<>());
            FileInputStream fin = new FileInputStream(filename);
            InputStreamReader reader = new InputStreamReader(fin);
            BufferedReader buffReader = new BufferedReader(reader);
            String strTmp = "";
            while((strTmp = buffReader.readLine())!=null){
                if(!strTmp.startsWith("//")){
                    String[] strings = strTmp.split(",");
                    if(privacyOntologyMap.containsKey(strings[0].trim())){
                        if(privacyOntologyMap.get(strings[0].trim()).contains(strings[1].trim())){
                        }else {
                            privacyOntologyMap.get(strings[0].trim()).add(strings[1].trim().toLowerCase());
                        }
                    }else {
                        List<String> list = new LinkedList<>();
                        list.add(strings[1].trim().toLowerCase());
                        privacyOntologyMap.put(strings[0].trim().toLowerCase(),list);
                    }
                    privacyOntologySet.add(strings[0].trim().toLowerCase());
                    privacyOntologySet.add(strings[1].trim().toLowerCase());
                }
            }
            buffReader.close();
            //System.out.println("privacyOntologyMap.size():"+privacyOntologyMap.size());
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("privacy ontology读取出错！"+privacyOntologyMap.size());
        }
    }

    public static boolean a_isAncestorOf_b(String a,String b){
        if(a.equals(b)){
            return true;
        }
        Deque<String> deque = new LinkedList<>();
        if(privacyOntologyMap.containsKey(a)){
            deque.add(a);
        }
        while (!deque.isEmpty()){
            String temp = deque.pollFirst();
            if(temp.equals(b)){
                return true;
            }
            if(privacyOntologyMap.containsKey(temp)&&!privacyOntologyMap.get(temp).isEmpty()){
                List<String> list = privacyOntologyMap.get(temp);
                deque.addAll(list);
            }
        }
        return false;
    }


    /*
    public static class privacyOntologyTree{
        public String info;
        public List<privacyOntologyTree> children;

        public privacyOntologyTree(){
            info = "information";
            children = null;
        }
        public privacyOntologyTree(String information,List<privacyOntologyTree> children){
            this.info = information;
            this.children = children;
        }

        public void addChild(String child){
            privacyOntologyTree childTree = new privacyOntologyTree(child,null);
            this.children.add(childTree);
        }

        public void removeChild(String child){
            int flag = -1;
            for(int i=0;i<children.size();i++){
                if(children.get(i).info.equals(child)){
                    flag = i;
                }
            }
            if(flag!=-1){
                children.remove(flag);
            }
        }
    }
     */

}
