package tool.other;

import org.xmlpull.v1.XmlPullParserException;
import soot.jimple.toolkits.callgraph.CallGraph;
import tool.analysis.infoflowResult;
import tool.mapping.apiMappingToPrivacy;
import tool.mapping.ipMappingToDNS;
import tool.mapping.urlMappingToEntity;
import tool.modifyToTuple.modifyFlowResults;
import tool.modifyToTuple.modifyPolicyResults;
import tool.ontology.EntityOntologyMap;
import tool.ontology.PrivacyOntologyMap;

import java.io.IOException;
import java.util.*;

public class main {
    public static String apkPath = "F:\\GooglePlayAPK\\googleplay\\tinder-dating-make-friends-and-meet-new-people_12.10.0(12100099).apk";
    //public static String apkPath = "E:\\\\tzy\\ContrastAPK\\test\\younow.live_v15.9.7-150907100_Android-4.4.apk";
    public static String jarsPath = "C:\\Program Files\\AndroidSDK\\platforms\\";
    public static String androidCallbackPath = "C:\\Users\\Administrator\\Desktop\\MyDroid\\configs\\FlowDroidConfigs\\AndroidCallBacks.txt";
    public static String sourceAndSinkPath = "C:\\Users\\Administrator\\Desktop\\MyDroid\\configs\\FlowDroidConfigs\\SourceAndSinks.txt";
    public static String easyTaintWrapperSource = "C:\\Users\\Administrator\\Desktop\\MyDroid\\configs\\FlowDroidConfigs\\EasyTaintWrapperSource.txt";

    //隐私策略的分析结果
    public static String privacyPolicyResults = "C:\\Users\\Administrator\\Desktop\\MyDroid\\configs\\policyResults.txt";

    //api方法和隐私信息的映射  apiMappingToPrivacy.txt
    public static String apiTOPrivacy = "C:\\Users\\Administrator\\Desktop\\MyDroid\\configs\\apiMappingToPrivacy.txt";
    //隐私信息本体  privacyOntology.txt
    public static String privacyTOOntology = "C:\\Users\\Administrator\\Desktop\\MyDroid\\configs\\privacyOntology.txt";
    //第三方实体的本体 entityOntology.txt
    public static String EntityOntology = "C:\\Users\\Administrator\\Desktop\\MyDroid\\configs\\entityOntology.txt";
    //ip地址到url的映射  ipMappingToDNS.txt
    public static String IPToDNS = "C:\\Users\\Administrator\\Desktop\\MyDroid\\configs\\ipMappingToDNS.txt";
    //dns域名到实体的映射  DNSMappingToEntity.txt
    public static String DNSToEntity = "C:\\Users\\Administrator\\Desktop\\MyDroid\\configs\\DNSMappingToEntity.txt";

    public static String firstParty ;
    public static Set<String> missingThirdParty = new HashSet<>();

    public static void main(String[] args) throws IOException, XmlPullParserException {
        int x = 6;
        //初始化ontology和mapping
        EntityOntologyMap.initEntity(EntityOntology);
        PrivacyOntologyMap.initPrivacy(privacyTOOntology);
        ipMappingToDNS.initIp(IPToDNS);
        urlMappingToEntity.init(DNSToEntity);
        apiMappingToPrivacy.initApi(apiTOPrivacy);

        //将隐私策略的分析结果转化成规范格式
        List<String[]> policyResults =  modifyPolicyResults.modifiedPolicyResults(privacyPolicyResults);
        //System.out.println("policy size : "+policyResults.size());

        //污点分析所需的配置
        String[] config = new String[]{apkPath,jarsPath,androidCallbackPath,sourceAndSinkPath,easyTaintWrapperSource};

        CallGraph callGraph = infoflowResult.getCallGraph(config);

        switch (x){
            case 1:
                //测试混淆
                infoflowResult.PrintGivenSootMethod(config);
                break;
            case 5:
                List<String[]> b = infoflowResult.getAdIDBySoot(config);
                System.out.println("------------------------------");
            case 6:
                List<String[]> c = infoflowResult.getInfoByValueBox(config);
                break;
        }

    }

    public static List<String[]> sootAnalysis(String[] configs){
        List<String[]> res = new LinkedList<>();
        List<String[]> temp = infoflowResult.getInfoByValueBox(configs);
        Set<String> set = new HashSet<>();
        for(String[] s : temp){
            for(String entity : EntityOntologyMap.entityOntologySet){
                if(s[0].contains(entity)){
                    set.add(entity);
                    break;
                }
            }
        }
        for(String entity : set){
            res.add(new String[]{entity,"device identifier"});
        }
        System.out.println("**************************");
        System.out.println("静态分析结果：");
        for(String entity : set){
            System.out.println(entity +" : "+"device identifier");
        }
        System.out.println("**************************");
        return res;
    }


}

