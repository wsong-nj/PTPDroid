package tool.java;

import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.util.*;

import soot.jimple.toolkits.callgraph.CallGraph;
import tool.consistency.ConsistencyAnalysis;
import tool.consistency.ConsistencyAnalysisResult;
import tool.java.preprocess;
import tool.ontology.EntityOntologyMap;
import tool.ontology.PrivacyOntologyMap;
import tool.modifyToTuple.*;
import tool.mapping.*;
import tool.analysis.infoflowResult;
import tool.other.*;

public class main {
    public static String apkPath = "D:\\ptpdroidapk\\MyLeviton.apk";
    public static String jarsPath = "D:\\AndroidSDK\\platforms";
    public static String androidCallbackPath = "D:\\PTPDroid-master\\configs\\FlowDroidConfigs\\AndroidCallBacks.txt";
    public static String sourceAndSinkPath = "D:\\PTPDroid-master\\configs\\FlowDroidConfigs\\SourceAndSinks.txt";
    public static String easyTaintWrapperSource = "D:\\PTPDroid-master\\configs\\FlowDroidConfigs\\EasyTaintWrapperSource.txt";

    //闅愮绛栫暐鐨勫垎鏋愮粨鏋�
    public static String privacyPolicyResults = "D:\\PTPDroid-master\\configs\\policyResults.txt";

    //api鏂规硶鍜岄殣绉佷俊鎭殑鏄犲皠  apiMappingToPrivacy.txt
    public static String apiTOPrivacy = "D:\\PTPDroid-master\\configs\\apiMappingToPrivacy.txt";
    //闅愮淇℃伅鏈綋  privacyOntology.txt
    public static String privacyTOOntology = "D:\\PTPDroid-master\\configs\\privacyOntology.txt";
    //绗笁鏂瑰疄浣撶殑鏈綋 entityOntology.txt
    public static String EntityOntology = "D:\\PTPDroid-master\\configs\\entityOntology.txt";
    //ip鍦板潃鍒皍rl鐨勬槧灏�  ipMappingToDNS.txt
    public static String IPToDNS = "D:\\PTPDroid-master\\configs\\ipMappingToDNS.txt";
    //dns鍩熷悕鍒板疄浣撶殑鏄犲皠  DNSMappingToEntity.txt
    public static String DNSToEntity = "D:\\PTPDroid-master\\configs\\DNSMappingToEntity.txt";

    public static String firstParty ;
    public static Set<String> missingThirdParty = new HashSet<>();

    public static void main(String[] args) throws IOException, XmlPullParserException {
        long start = System.currentTimeMillis();
        //鍒濆鍖杘ntology鍜宮apping
        EntityOntologyMap.initEntity(EntityOntology);
        PrivacyOntologyMap.initPrivacy(privacyTOOntology);
        ipMappingToDNS.initIp(IPToDNS);
        urlMappingToEntity.init(DNSToEntity);
        apiMappingToPrivacy.initApi(apiTOPrivacy);

        //灏嗛殣绉佺瓥鐣ョ殑鍒嗘瀽缁撴灉杞寲鎴愯鑼冩牸寮�
        List<String[]> policyResults =  modifyPolicyResults.modifiedPolicyResults(privacyPolicyResults);

        //姹＄偣鍒嗘瀽鎵�闇�鐨勯厤缃�
        String[] config = new String[]{apkPath,jarsPath,androidCallbackPath,sourceAndSinkPath,easyTaintWrapperSource};

        CallGraph callGraph = infoflowResult.getCallGraph(config);
        firstParty = apkMappingToEntity.changeToEntity(apkPath);

        List<String[]> formedResults = new LinkedList<>();
        List<String[]> sinksToEntity = new LinkedList<>();
        boolean flag = true;
        try {
            List<String[]> res = infoflowResult.taintAnalysis(config);
            List<String[]> sinksToEntity1 = modifyFlowResults.findThirdPartyName(res,callGraph);
            sinksToEntity = sinksToEntity1;
            modifyFlowResults.union(config,sinksToEntity1);
            List<String[]> formedResults1 = modifyFlowResults.modifiedFlowResults(sinksToEntity1,firstParty);
            formedResults = formedResults1;
        }catch (Exception e){
            flag = false;
            List<String[]> res = tool.other.main.sootAnalysis(config);
        }

        if(flag){
            System.out.println("**************************");
            System.out.println("闈欐�佸垎鏋愮粨鏋滐細");
            Map<String,Set<String>> results = modifyFlowResults.modifyResultStructure(formedResults);
            Iterator<Map.Entry<String, Set<String>>> iterator = results.entrySet().iterator();
            while (iterator.hasNext()){
                Map.Entry<String, Set<String>> entry = iterator.next();
                System.out.print(entry.getKey()+" : ");
                for(String data : entry.getValue()){
                    if(data.contains("<")||data.contains("(")){
                    }else {
                        System.out.print(data+" , ");
                    }
                }
                System.out.println();
            }
            System.out.println("**************************");
        }


        long end = System.currentTimeMillis();
        long time = end-start;

        List<String[]> flowResults = modifyFlowResults.modifiedFlowResults(sinksToEntity,firstParty);
        List<String[]> appResults = enhance.modify(flowResults);
            ConsistencyAnalysisResult result = ConsistencyAnalysis.consistencyAnalysis(appResults,policyResults);
            result.outputInconsistentResults(result);

    }




}

