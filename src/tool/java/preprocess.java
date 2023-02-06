package tool.java;

import org.xmlpull.v1.XmlPullParserException;
import soot.jimple.toolkits.callgraph.CallGraph;

import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import tool.ontology.EntityOntologyMap;
import tool.mapping.ipMappingToDNS;

public class preprocess {
    static String urlReg = "(https?|ftp|file)://[-A-Za-z0-9+&@#/%?=~_|!:,.;]+[-A-Za-z0-9+&@#/%=~_|]";
    static Pattern urlPattern = Pattern.compile(urlReg);
    static String packageReg = "([a-zA-Z_][a-zA-Z0-9_]*[.])([a-zA-Z_][a-zA-Z0-9_]*[.])*([a-zA-Z_][a-zA-Z0-9_]*)$";
    static Pattern packagePattern = Pattern.compile(packageReg);
    static String ipReg = "((2(5[0-5]|[0-4]\\\\d))|[0-1]?\\\\d{1,2})(\\\\.((2(5[0-5]|[0-4]\\\\d))|[0-1]?\\\\d{1,2})){3}";
    static Pattern ipPattern = Pattern.compile(ipReg);

    public static Set<String> preProcess(List<String[]> foundThirdParty, CallGraph callGraph) throws IOException, XmlPullParserException {

        Set<String> missingThirdParty = new HashSet<>();

        for(String[] strings:foundThirdParty){
            boolean isINConfig = false;
            String found = strings[0];
            Matcher matcher = urlPattern.matcher(found);
            Matcher matcher1 = packagePattern.matcher(found);
            if(matcher.find()||matcher1.find()){
                for(String str: EntityOntologyMap.entityOntologySet){
                    if(found.contains(str)){
                        isINConfig = true;
                        break;
                    }
                }
            }else {
                for(String str: ipMappingToDNS.IPName){
                    if(found.contains(str)){
                        isINConfig = true;
                        break;
                    }
                }
            }
            if(!isINConfig){
                missingThirdParty.add(found);
            }
        }

        return missingThirdParty;
    }

}
