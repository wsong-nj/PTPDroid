package tool.consistency;

import tool.ontology.EntityOntologyMap;
import tool.ontology.PrivacyOntologyMap;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class ConsistencyAnalysis {
    //appResult : [entity,privacy]
    //policyResult : [entity,action,privacy]
    public static ConsistencyAnalysisResult consistencyAnalysis(List<String[]> appResult,
                                                         List<String[]> policyResult){

        ConsistencyAnalysisResult consistencyAnalysisResult = new ConsistencyAnalysisResult();

        /*
        for(String[] appRes : appResult){
            System.out.println(appRes[0]+" "+appRes[1]);
        }
        for(String[] poliRes : policyResult){
            System.out.println(poliRes[0]+" "+poliRes[1]+" "+poliRes[2]);
        }
         */

        for(String[] appRes : appResult){
            boolean isOmitted = false;
            boolean isVague = false;
            boolean isIncorrect = false;
            boolean isClear = false;
            for(String[] poliRes : policyResult){
                if(appRes[0].equals(poliRes[0])&&appRes[1].equals(poliRes[2])&&!poliRes[1].contains("not")){
                    isClear = true;
                    break;
                }
                if(poliRes[1].contains("not")){
                    if(EntityOntologyMap.a_isAncestorOf_b(poliRes[0],appRes[0])&&PrivacyOntologyMap.a_isAncestorOf_b(poliRes[2],appRes[1])){
                        isIncorrect = true;
                        break;
                    }
                }else {
                    if(EntityOntologyMap.a_isAncestorOf_b(poliRes[0],appRes[0])&&PrivacyOntologyMap.a_isAncestorOf_b(poliRes[2],appRes[1])){
                        isVague = true;
                    }
                }
            }
            if(isClear){
                consistencyAnalysisResult.clearStatement.add(appRes[0]+ " collect "+ appRes[1]);
            }else if(isVague){
                consistencyAnalysisResult.ambiguousStatement.add(appRes[0]+ " collect "+ appRes[1]);
            }else if(isIncorrect){
                consistencyAnalysisResult.incorrectStatement.add(appRes[0]+ " collect "+ appRes[1]);
            }else {
                consistencyAnalysisResult.omittedStatement.add(appRes[0]+ " collect "+ appRes[1]);
            }
        }

        return consistencyAnalysisResult;

        /*
        Map<String,Map<String,Boolean>> consistent = new HashMap<>();
        for(String[] str:appResult){
            if(consistent.containsKey(str[0])){
                consistent.get(str[0]).put(str[1],false);
            }else {
                Map<String,Boolean> map1 = new HashMap<>();
                map1.put(str[1],false);
                consistent.put(str[0],map1);
            }
        }

        for(String[] str1:policyResult){
            if(str1[1].equals("not collect")){
                Iterator<Map.Entry<String,Map<String,Boolean>>> iterator = consistent.entrySet().iterator();
                while (iterator.hasNext()){
                    Map.Entry<String,Map<String,Boolean>> next = iterator.next();
                    if(EntityOntologyMap.a_isAncestorOf_b(next.getKey(),str1[0])||next.getKey().equals(str1[0])||
                            EntityOntologyMap.a_isAncestorOf_b(str1[0],next.getKey())){
                        Iterator<Map.Entry<String, Boolean>> iterator1 = next.getValue().entrySet().iterator();
                        while (iterator1.hasNext()){
                            Map.Entry<String,Boolean> next1 = iterator1.next();
                            if(PrivacyOntologyMap.a_isAncestorOf_b(str1[2],next1.getKey())||str1[2].equals(next1.getKey())||
                                    PrivacyOntologyMap.a_isAncestorOf_b(next1.getKey(),str1[2])){
                                consistencyAnalysisResult.incorrectStatement.add("policy: "+str1[0]+" not collect "+
                                        str1[2]+"; app: "+next.getKey()+" collect "+next1.getKey()+".");
                                consistent.get(next.getKey()).put(next1.getKey(),true);
                            }
                        }
                    }
                }
            }else if((consistent.containsKey(str1[0]))?consistent.get(str1[0]).containsKey(str1[2]):false){
            }else {
                boolean isRedundant = true;
                for (Map.Entry<String, Map<String, Boolean>> next2 : consistent.entrySet()) {
                    if (EntityOntologyMap.a_isAncestorOf_b(next2.getKey(), str1[0]) || next2.getKey().equals(str1[0]) ||
                            EntityOntologyMap.a_isAncestorOf_b(str1[0], next2.getKey())) {
                        for (Map.Entry<String, Boolean> next3 : next2.getValue().entrySet()) {
                            if (PrivacyOntologyMap.a_isAncestorOf_b(str1[2], next3.getKey()) || str1[2].equals(next3.getKey()) ||
                                    PrivacyOntologyMap.a_isAncestorOf_b(next3.getKey(), str1[2])) {
                                consistencyAnalysisResult.ambiguousStatement.add("policy: " + str1[0] + " collect " +
                                        str1[2] + "; app: " + next2.getKey() + " collect " + next3.getKey() + ".");
                                consistent.get(next2.getKey()).put(next3.getKey(), true);
                                isRedundant = false;
                            }
                        }
                    }
                }
                if(isRedundant){
                    consistencyAnalysisResult.redundantStatement.add("policy: "+str1[0]+" collect "+str1[1]+".");
                }
            }
        }

        for (Map.Entry<String, Map<String, Boolean>> next4 : consistent.entrySet()) {
            for (Map.Entry<String, Boolean> next5 : next4.getValue().entrySet()) {
                if (!next5.getValue()) {
                    consistencyAnalysisResult.omittedStatement.add("app: " + next4.getKey() + " collect " + next5.getKey());
                }
            }
        }

        return consistencyAnalysisResult;

         */
    }
}
