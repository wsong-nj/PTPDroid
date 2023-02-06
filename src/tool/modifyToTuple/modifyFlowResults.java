package tool.modifyToTuple;

import soot.Body;
import soot.SootMethod;
import soot.ValueBox;
import soot.jimple.toolkits.callgraph.CallGraph;
import soot.jimple.toolkits.callgraph.Edge;
import tool.mapping.apiMappingToPrivacy;
import tool.mapping.apkMappingToEntity;
import tool.mapping.ipMappingToDNS;
import tool.mapping.urlMappingToEntity;
import tool.ontology.EntityOntologyMap;
import tool.analysis.infoflowResult;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class modifyFlowResults {
    public static final String urlReg = "(https?|ftp|file)://[-A-Za-z0-9+&@#/%?=~_|!:,.;]+[-A-Za-z0-9+&@#/%=~_|]";
    public static final Pattern urlPattern = Pattern.compile(urlReg);
    public static final String ipReg = "((2(5[0-5]|[0-4]\\\\d))|[0-1]?\\\\d{1,2})(\\\\.((2(5[0-5]|[0-4]\\\\d))|[0-1]?\\\\d{1,2})){3}";
    public static final Pattern ipPattern = Pattern.compile(ipReg);
    public static final String packageReg = "([a-zA-Z_][a-zA-Z0-9_]*[.])([a-zA-Z_][a-zA-Z0-9_]*[.])*([a-zA-Z_][a-zA-Z0-9_]*)$";
    public static final Pattern packagePattern = Pattern.compile(packageReg);
    
    //转化成entity privacy的格式
    public static List<String[]> modifiedFlowResults(List<String[]> flowResults,String firstParty){
        List<String[]> results = new LinkedList<>();
        for(String[] res:flowResults){
            String[] temp = new String[2];
            Matcher matcher = urlPattern.matcher(res[0]);
            Matcher matcher1 = ipPattern.matcher(res[0]);
            Matcher matcher2 = packagePattern.matcher(res[0]);
            if(matcher.find()){
                temp[0] = urlMappingToEntity.changeToEntity(res[0]);
            }else if(matcher1.find()){
                temp[0] = urlMappingToEntity.changeToEntity(ipMappingToDNS.changeToUrl(res[0]));
            }else {
                temp[0] = apkMappingToEntity.changeToEntity(res[0]);
            }
            if(temp[0].equals(firstParty)){
                //System.out.println("第一方收集的信息："+temp[0]+","+apiMappingToPrivacy.changeToOntology(res[1]));
                continue;
            }
            temp[1] = apiMappingToPrivacy.changeToOntology(res[1]);
            results.add(temp);
        }
        return results;
    }

    public static Map<String,Set<String>> modifyResultStructure(List<String[]> flowResults){
        Map<String,Set<String>> res = new HashMap<>();
        for(String[] str : flowResults){
            if(!str[0].contains(".")){
                if(!res.containsKey(str[0])){
                    Set<String> set = new HashSet<>();
                    set.add(str[1]);
                    res.put(str[0],set);
                }else {
                    res.get(str[0]).add(str[1]);
                }
            }
        }
        return res;
    }


    //通过resultSinkInfo获得第三方的apk/url/ip;通过resultSourceInfo转化uri
    //结果形式：[entity data]
    //bfs
    public static List<String[]> findThirdPartyName(List<String[]> infoFlowResults, CallGraph callGraph){
        List<String[]> res = new LinkedList<>();

        turnQueryToOntology(infoFlowResults,callGraph);

        for(String[] strings:infoFlowResults){

            if(strings[0].equals("InThirdPartyLibrary")){
                for(String entity : EntityOntologyMap.entityOntologySet){
                    if(strings[1].contains(entity)){
                        if(apiMappingToPrivacy.apiToOntologyMap.containsKey(trimToApi(strings[2]))){
                            String[] temp = new String[2];
                            temp[0] = entity;
                            temp[1] = apiMappingToPrivacy.apiToOntologyMap.get(trimToApi(strings[2]));
                            res.add(temp);
                        }
                        break;
                    }
                }
                for(String entity : apkMappingToEntity.apkToEntityMap.keySet()){
                    if(strings[1].contains(entity)){
                        if(apiMappingToPrivacy.apiToOntologyMap.containsKey(trimToApi(strings[2]))){
                            String[] temp = new String[2];
                            temp[0] = entity;
                            temp[1] = apiMappingToPrivacy.apiToOntologyMap.get(trimToApi(strings[2]));
                            res.add(temp);
                        }
                        break;
                    }
                }
                continue;
            }

            //deque中的每个方法遍历完后加入set，防止循环
            Set<String> set = new HashSet<>();
            //获取url/ip的队列
            Deque<String> deque = new LinkedList<>();
            deque.add(strings[1]);
            int len = 0;
            boolean flag = false;
            while (!deque.isEmpty()){
                len++;
                Set<String> nextSootMethod = new HashSet<>();
                while (!deque.isEmpty()){
                    String str = deque.pollFirst();
                    for(Edge edge:callGraph){
                        SootMethod sootMethod = edge.src();
                        if(sootMethod!=null&&str.contains(sootMethod.getSignature())){
                            if(!set.contains(sootMethod.getSignature())){
                                if(sootMethod.hasActiveBody()){
                                    Body body = sootMethod.getActiveBody();
                                    for (ValueBox valueBox : body.getUseBoxes()) {
                                        String string = valueBox.getValue().toString();
                                        if(!string.contains("java")&&string.contains("/")){
                                            Matcher matcher = urlPattern.matcher(string);
                                            Matcher matcher1 = packagePattern.matcher(string);
                                            Matcher matcher2 = ipPattern.matcher(string);
                                            if (matcher.find() || matcher1.find() || matcher2.find()) {
                                                //判断source是否已转化成ontology
                                                if(!strings[2].contains("android.database.Cursor")){
                                                    flag = true;
                                                    String[] temp = new String[2];
                                                    temp[0] = string;
                                                    temp[1] = strings[2];
                                                    res.add(temp);
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                            if(edge.tgt()!=null&&!set.contains(edge.tgt().getSignature())){
                                nextSootMethod.add(edge.tgt().getSignature());
                            }
                        }
                    }
                    set.add(str);
                }
                //找到匹配的字符串后退出遍历
                if(flag){
                    break;
                }
                //广度遍历搜索数次未找到后返回sink的signature
                if(len==5&&!strings[2].contains("android.database.Cursor")){
                    String[] temp = new String[2];
                    temp[0] = strings[1];
                    temp[1] = strings[2];
                    res.add(temp);
                    break;
                }
                if(!nextSootMethod.isEmpty()){
                    deque.addAll(nextSootMethod);
                }else {
                    //如果nextSootMethod为空必然跳出循环
                    if(!strings[2].contains("android.database.Cursor")){
                        String[] temp = new String[2];
                        temp[0] = strings[1];
                        temp[1] = strings[2];
                        res.add(temp);
                    }
                }
            }
            /*
            if(strings[2].toLowerCase().contains("android.database.cursor")){
                System.out.println("未找到uri的数据流:"+strings[0]+" "+strings[1]+" "+strings[2]+" "+strings[3]);
            }else {
                System.out.println("获取到的结果："+strings[0]+" "+strings[1]+" "+strings[2]+" "+strings[3]);
            }
             */
        }

        return res;
    }

    //修剪“$r1 = virtualinvoke $r6.<android.telephony.TelephonyManager: java.lang.String getDeviceId()>()”
    private static String trimToApi(String string) {
        int left = string.indexOf("<");
        int right = string.indexOf(">");
        return string.substring(left,right+1);
    }


    //将source为query的部分转化为相应的uri/本体词语
    private static void turnQueryToOntology(List<String[]> infoFlowResults, CallGraph callGraph) {
        Set<String> set = new HashSet<>();
        for (String[] strings : infoFlowResults) {
            if(containPrivacyInformation(strings)){
                continue;
            }
            if (strings[2].contains("android.database.Cursor") && (!strings[2].contains("android.provider.Browser"))) {
                Deque<String> deque = new LinkedList<>();
                deque.add(strings[3]);
                int len = 0;
                while (!deque.isEmpty()){
                    len++;
                    Set<String> nextSootMethod = new HashSet<>();
                    while (!deque.isEmpty()){
                        String str = deque.pollFirst();
                        for(Edge edge:callGraph){
                            SootMethod sootMethod = edge.src();
                            if(sootMethod!=null&&str.contains(sootMethod.getSignature())&&!set.contains(sootMethod.getSignature())){
                                if(sootMethod.hasActiveBody()){
                                    Body body = sootMethod.getActiveBody();
                                    for(ValueBox valueBox:body.getUseBoxes()){
                                        String string = valueBox.getValue().toString();
                                        //检测是否是广告id
                                        if(string.contains("advertising_id")||string.toLowerCase().contains("advertisingid")){
                                            strings[2] = "advertise_id";
                                            break;
                                        }
                                        //检测android系统的uri,不以content://开头
                                        if(string.contains("android.provider.Contacts")){
                                            strings[2] = "contact";
                                            break;
                                        }
                                        if(string.contains("android.provider.CalendarContract")){
                                            strings[2] = "calendar";
                                            break;
                                        }
                                        if(string.contains("android.provider.MediaStore")){
                                            strings[2] = "media";
                                            break;
                                        }
                                        //检测以content://开头的是否是隐私信息
                                        if(string.contains("content://")){
                                            String temp = judgeQueryIsPrivacy(string.toLowerCase());
                                            if(!temp.contains("content://")){
                                                strings[2] = temp;
                                            }
                                            if(!strings[2].contains(".")){
                                                break;
                                            }
                                        }
                                    }
                                }
                                if(edge.tgt()!=null){
                                    nextSootMethod.add(edge.tgt().getName());
                                }
                            }
                        }
                        set.add(str);
                    }
                    deque.addAll(nextSootMethod);
                    if(!strings[2].contains(".")||len==4){
                        break;
                    }
                }
            }
        }
    }

    private static boolean containPrivacyInformation(String[] strings) {
        String temp = strings[3].toLowerCase();
        if(temp.contains("advertising")){
            strings[2] = "advertise_id";
            return true;
        }
        if(temp.contains("get")){
            if(temp.contains("image")||temp.contains("camera")){
                strings[2] = "image";
                return true;
            }
        }
        return false;
    }

    //判断source是否是内容提供器,若是,将其转化成本体
    private static String judgeQueryIsPrivacy(String string) {
        if(string.contains("phone")){
            return "sms";
        }else if(string.contains("email")) {
            return "email";
        }else if(string.contains("contact")){
            return "contact";
        }else if(string.contains("calendar")){
            return "calendar";
        }else if(string.contains("sms")){
            return "phone";
        }else if(string.contains("image")){
            return "image";
        } else {
            return string;
        }
    }

    public static void union(String[] config, List<String[]> sinksToEntity) {
        List<String[]> temp = infoflowResult.getInfoByValueBox(config);
        sinksToEntity.addAll(temp);
    }

    //获取所有可能的第三方名，如果url获取在一个if/switch语句中可能导致获取不止1个url
    /*
    public static List<String[]> findAllThirdPartyName(List<String[]> list,CallGraph callGraph){
        String urlReg = "(https?|ftp|file)://[-A-Za-z0-9+&@#/%?=~_|!:,.;]+[-A-Za-z0-9+&@#/%=~_|]";
        Pattern urlPattern = Pattern.compile(urlReg);
        String packageReg = "([a-zA-Z_][a-zA-Z0-9_]*[.])([a-zA-Z_][a-zA-Z0-9_]*[.])*([a-zA-Z_][a-zA-Z0-9_]*)$";
        Pattern packagePattern = Pattern.compile(packageReg);
        String ipReg = "((2(5[0-5]|[0-4]\\\\d))|[0-1]?\\\\d{1,2})(\\\\.((2(5[0-5]|[0-4]\\\\d))|[0-1]?\\\\d{1,2})){3}";
        Pattern ipPattern = Pattern.compile(ipReg);

        List<String[]> res = new LinkedList<>();
        for(String[] strings:list){
            List<String> list1 = new LinkedList<>();
            for(Edge edge:callGraph){
                SootMethod sootMethod = edge.src();
                SootMethod sootMethod1 = edge.tgt();
                if(sootMethod.getSignature().contains(strings[1])||sootMethod1.getSignature().contains(strings[1])){
                    Body body = sootMethod.getActiveBody();
                    for (ValueBox valueBox : body.getUseAndDefBoxes()) {
                        String string = valueBox.getValue().toString();
                        if(string.startsWith("java")||string.startsWith("@")||string.startsWith("$")||string.startsWith("new")||
                                string.startsWith("(")){
                            continue;
                        }
                        Matcher matcher = urlPattern.matcher(string);
                        Matcher matcher1 = packagePattern.matcher(string);
                        Matcher matcher2 = ipPattern.matcher(string);
                        if (matcher.find()|| matcher1.find()||matcher2.find()) {
                            list1.add(string);
                        }
                    }
                }
            }
            for(String str:list1){
                res.add(new String[]{str,strings[2]});
            }
        }
        return res;
    }
     */
}
