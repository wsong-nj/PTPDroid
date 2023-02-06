package tool.other;

import soot.Body;
import soot.SootClass;
import soot.SootMethod;
import soot.ValueBox;
import soot.jimple.toolkits.callgraph.CallGraph;
import soot.jimple.toolkits.callgraph.Edge;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class test {

    public static void findAdvertiser(){

    }

    //检查未发现url的情况
    public static void findEdge(CallGraph callGraph){
        for(Edge edge:callGraph){
            if(edge.tgt()!=null&&
                    edge.tgt().getSignature().equals("<com.example.myapplication.DataService: java.lang.String sendDataByPost(java.lang.String,java.lang.String,java.lang.String,java.lang.String)>")){
                System.out.println("src:"+edge.src().getSignature());
            }
            if(edge.src()!=null&&
                    edge.src().getSignature().equals("<com.example.myapplication.DataService: java.lang.String sendDataByPost(java.lang.String,java.lang.String,java.lang.String,java.lang.String)>")){
                System.out.println("tgt:"+edge.tgt().getSignature());
            }
        }
        for(Edge edge:callGraph){
            if(edge.src()!=null
                    &&edge.src().getSignature().equals("<com.example.myapplication.DataService: java.lang.String sendDataByPost(java.lang.String,java.lang.String,java.lang.String,java.lang.String)>")){
                SootMethod sootMethod = edge.src();
                System.out.println(sootMethod.getActiveBody().toString());
                return;
            }
        }
    }

    //找到程序中所有url和ip的位置
    public static Map<String,Set<String>> findDNS(CallGraph callGraph){
        String urlReg = "(https?|ftp|file)://[-A-Za-z0-9+&@#/%?=~_|!:,.;]+[-A-Za-z0-9+&@#/%=~_|]";
        Pattern urlPattern = Pattern.compile(urlReg);
        String ipReg = "((2(5[0-5]|[0-4]\\\\d))|[0-1]?\\\\d{1,2})(\\\\.((2(5[0-5]|[0-4]\\\\d))|[0-1]?\\\\d{1,2})){3}";
        Pattern ipPattern = Pattern.compile(ipReg);
        Map<String,Set<String>> res = new HashMap<>();
        Set<String> set = new HashSet<>();
        for(Edge edge:callGraph){
            if(edge.src()!=null&&!set.contains(edge.src().getSignature())){
                set.add(edge.src().getSignature());
                if(edge.src().hasActiveBody()){
                    Body body = edge.src().getActiveBody();
                    for(ValueBox valueBox:body.getUseBoxes()){
                        String string = valueBox.getValue().toString();
                        Matcher matcher = urlPattern.matcher(string);
                        Matcher matcher2 = ipPattern.matcher(string);
                        if ((matcher.find()||matcher2.find())) {
                            if(res.containsKey(string)){
                                res.get(string).add(edge.src().getSignature());
                            }else {
                                Set<String> set1 = new HashSet<>();
                                set1.add(edge.src().getSignature());
                                res.put(string,set1);
                            }
                        }
                    }
                }
            }
            if(edge.tgt()!=null&&!set.contains(edge.tgt().getSignature())){
                set.add(edge.tgt().getSignature());
                if(edge.tgt().hasActiveBody()){
                    Body body = edge.tgt().getActiveBody();
                    for(ValueBox valueBox:body.getUseBoxes()){
                        String string = valueBox.getValue().toString();
                        Matcher matcher = urlPattern.matcher(string);
                        Matcher matcher2 = ipPattern.matcher(string);
                        if ((matcher.find()||matcher2.find())) {
                            if(res.containsKey(string)){
                                res.get(string).add(edge.tgt().getSignature());
                            }else {
                                Set<String> set1 = new HashSet<>();
                                set1.add(edge.tgt().getSignature());
                                res.put(string,set1);
                            }
                        }
                    }
                }
            }
        }
        System.out.println("sootclass.size:"+set.size());
        return res;
    }

    public static List<String[]> findThirdPartyName(List<String[]> infoFlowResults, CallGraph callGraph){
        String urlReg = "(https?|ftp|file)://[-A-Za-z0-9+&@#/%?=~_|!:,.;]+[-A-Za-z0-9+&@#/%=~_|]";
        Pattern urlPattern = Pattern.compile(urlReg);
        String packageReg = "([a-zA-Z_][a-zA-Z0-9_]*[.])([a-zA-Z_][a-zA-Z0-9_]*[.])*([a-zA-Z_][a-zA-Z0-9_]*)$";
        Pattern packagePattern = Pattern.compile(packageReg);
        String ipReg = "((2(5[0-5]|[0-4]\\\\d))|[0-1]?\\\\d{1,2})(\\\\.((2(5[0-5]|[0-4]\\\\d))|[0-1]?\\\\d{1,2})){3}";
        Pattern ipPattern = Pattern.compile(ipReg);

        List<String[]> res = new LinkedList<>();

        turnQueryToOntology(infoFlowResults,callGraph);

        for(String[] strings:infoFlowResults){
            Set<String> set = new HashSet<>();
            for(Edge edge : callGraph){
                SootMethod sootMethod = edge.src();
                SootMethod sootMethod1 = edge.tgt();
                if(sootMethod!=null&&sootMethod.getSignature().contains(strings[1])&&!set.contains(sootMethod.getSignature())){
                    set.add(sootMethod.getSignature());
                    boolean flag = false;
                    if(sootMethod.hasActiveBody()){
                        Body body = sootMethod.getActiveBody();
                        for(ValueBox valueBox : body.getUseBoxes()){
                            String string = valueBox.getValue().toString();
                            Matcher matcher = urlPattern.matcher(string);
                            Matcher matcher1 = packagePattern.matcher(string);
                            Matcher matcher2 = ipPattern.matcher(string);
                            if (matcher.find() || matcher1.find() || matcher2.find()) {
                                flag = true;
                                break;
                            }
                        }
                    }
                }
            }
        }

        for(String[] strings:infoFlowResults){
            Set<String> set = new HashSet<>();
            Set<String> set1 = new HashSet<>();
            String[] add = new String[2];
            //用于判断是否发现了第三方，如未发现，用signature名替代
            boolean flag = false;
            for(Edge edge:callGraph){
                SootMethod sootMethod = edge.src();
                SootMethod sootMethod1 = edge.tgt();
                if(sootMethod!=null){
                    if(sootMethod.getSignature().contains(strings[1])&&!set.contains(sootMethod.getSignature())){
                        set.add(sootMethod.getSignature());
                        if(sootMethod.hasActiveBody()){
                            Body body = sootMethod.getActiveBody();
                            for (ValueBox valueBox : body.getUseBoxes()) {
                                String string = valueBox.getValue().toString();
                                if (string.startsWith("java") || string.startsWith("@") || string.startsWith("$") || string.startsWith("new") ||
                                        string.startsWith("(")||string.contains("instanceof")) {
                                    continue;
                                }
                                Matcher matcher = urlPattern.matcher(string);
                                Matcher matcher1 = packagePattern.matcher(string);
                                Matcher matcher2 = ipPattern.matcher(string);
                                if (matcher.find() || matcher1.find() || matcher2.find()) {
                                    flag = true;
                                    add[0] = string;
                                    add[1] = strings[2];
                                    break;
                                }
                            }
                        }
                    }
                }
                if(sootMethod1!=null){
                    if(sootMethod1.getSignature().contains(strings[1])&&!set.contains(sootMethod1.getSignature())){
                        set.add(sootMethod1.getSignature());
                        if(sootMethod1.hasActiveBody()){
                            Body body = sootMethod1.getActiveBody();
                            for (ValueBox valueBox : body.getUseBoxes()) {
                                String string = valueBox.getValue().toString();
                                if (string.startsWith("java") || string.startsWith("@") || string.startsWith("$") || string.startsWith("new") ||
                                        string.startsWith("(")||string.contains("instanceof")) {
                                    continue;
                                }
                                Matcher matcher = urlPattern.matcher(string);
                                Matcher matcher1 = packagePattern.matcher(string);
                                Matcher matcher2 = ipPattern.matcher(string);
                                if (matcher.find() || matcher1.find() || matcher2.find()) {
                                    flag = true;
                                    add[0] = string;
                                    add[1] = strings[2];
                                    break;
                                }
                            }
                        }
                    }
                }
            }
            if(!flag){
                add[0] = strings[1];
                add[1] = strings[2];
            }
            if(strings[2].contains("android.database.Cursor")
                    &&(!strings[2].contains("android.provider.Browser"))){
                for(Edge edge:callGraph) {
                    SootMethod sootMethod = edge.src();
                    SootMethod sootMethod1 = edge.tgt();
                    if(sootMethod!=null){
                        if(sootMethod.getSignature().contains(strings[3])&&!set1.contains(sootMethod.getSignature())){
                            set1.add(sootMethod.getSignature());
                            if(sootMethod.hasActiveBody()){
                                Body body = sootMethod1.getActiveBody();
                                for (ValueBox valueBox : body.getUseBoxes()) {
                                    String string = valueBox.getValue().toString();
                                    if(string.contains("content")){
                                        if(string.toLowerCase().contains("email")){
                                            add[1] = "email";
                                        }else if(string.toLowerCase().contains("store.images")){
                                            add[1] = "image";
                                        }else if(string.toLowerCase().contains("contact")){
                                            add[1] = "contact";
                                        }else if(string.toLowerCase().contains("calendar")){
                                            add[1] = "calendar";
                                        }
                                    }
                                }
                            }
                        }
                    }
                    if(sootMethod1!=null){
                        if(sootMethod1.getSignature().contains(strings[3])&&!set1.contains(sootMethod1.getSignature())){
                            if(sootMethod1.hasActiveBody()){
                                Body body = sootMethod1.getActiveBody();
                                for (ValueBox valueBox : body.getUseBoxes()) {
                                    String string = valueBox.getValue().toString();
                                    if(string.contains("content")){
                                        if(string.toLowerCase().contains("email")){
                                            add[1] = "email";
                                        }else if(string.toLowerCase().contains("store.images")){
                                            add[1] = "image";
                                        }else if(string.toLowerCase().contains("contact")){
                                            add[1] = "contact";
                                        }else if(string.contains("calendar")){
                                            add[1] = "calendar";
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
            res.add(add);
        }
        return res;
    }

    private static void turnQueryToOntology(List<String[]> infoFlowResults, CallGraph callGraph) {
        Set<String> set = new HashSet<>();
        for (String[] strings : infoFlowResults) {
            if (strings[2].contains("android.database.Cursor")
                    && (!strings[2].contains("android.provider.Browser"))) {
                for (Edge edge : callGraph) {
                    SootMethod sootMethod = edge.src();
                    SootMethod sootMethod1 = edge.tgt();
                    if (sootMethod != null && sootMethod.getSignature().contains(strings[3])
                            && !set.contains(sootMethod.getSignature())) {
                        set.add(sootMethod.getSignature());
                        if (sootMethod.hasActiveBody()) {
                            Body body = sootMethod.getActiveBody();
                            for (ValueBox valueBox : body.getUseBoxes()) {
                                String string = valueBox.getValue().toString();
                                if (string.contains("content")) {
                                    strings[2] = judgeQueryIsPrivacy(string.toLowerCase());
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    //判断source是否是内容提供器,若是,将其转化成本体
    private static String judgeQueryIsPrivacy(String string) {
        if(string.contains("email")) {
            return "email";
        }else if(string.contains("store.images")){
            return "image";
        }else if(string.contains("contact")){
            return "contact";
        }else if(string.contains("calendar")){
            return "calendar";
        }else {
            return string;
        }
    }

    //获取所有的与之相连的边
    public static void findNextMethod(CallGraph callGraph){
        String compare = "<pw.thedrhax.mosmetro.httpclient.clients.OkHttp: pw.thedrhax.mosmetro.httpclient.Client customDnsEnabled(boolean)>";
        System.out.println(callGraph.size());
        System.out.println("------------------------------");
        for(Edge edge:callGraph){
            SootMethod sootMethod = edge.src();
            SootMethod sootMethod1 = edge.tgt();
            if(sootMethod!=null){
                //System.out.println(sootMethod.getName()+" "+sootMethod.getSignature()+" "+sootMethod.getSubSignature());
                //onResume , <de.bahnhoefe.deutschlands.bahnhofsfotos.MainActivity: void onResume()> , void onResume()
                if(sootMethod.getSignature().equals(compare)){
                    System.out.println(sootMethod1.getSignature());
                }
            }
        }
        System.out.println("------------------------------");
        for(Edge edge:callGraph){
            SootMethod sootMethod = edge.src();
            SootMethod sootMethod1 = edge.tgt();
            if(sootMethod1!=null){
                if(sootMethod1.getSignature().equals(compare)){
                    System.out.println(sootMethod.getSignature());
                }
            }
        }
        System.out.println("------------------------------");
        for(Edge edge:callGraph){
            SootMethod sootMethod = edge.src();
            SootMethod sootMethod1 = edge.tgt();
            if(sootMethod!=null){
                if(sootMethod.getSignature().contains(compare)){
                    System.out.println(sootMethod.getSignature());
                }
            }
        }
    }


}
