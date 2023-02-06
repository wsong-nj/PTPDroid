package tool.modifyToTuple;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.LinkedList;
import java.util.List;

public class modifyPolicyResults {

    public static List<String[]> modifiedPolicyResults(String filename){
        List<String[]> results = new LinkedList<>();
        try {
            FileInputStream in = new FileInputStream(filename);
            InputStreamReader inReader = new InputStreamReader(in, "UTF-8");
            BufferedReader bufReader = new BufferedReader(inReader);
            String line = null;
            while((line = bufReader.readLine()) != null){
                line.replace("(","");
                line.replace(")","");
                String[] strings = line.split(",");
                String[] temp = new String[3];
                if(strings[3]!=null){
                    temp[0] = strings[3];
                    temp[2] = strings[2];
                    if(strings[1].contains("not")){
                        temp[1] = "not collect";
                    }else {
                        temp[1] = "collect";
                    }
                }
                String[] temp1 = change(temp);
                results.add(temp1);
            }
            bufReader.close();
            inReader.close();
            in.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return results;
    }

    private static String[] change(String[] temp) {
        String[] res = new String[3];
        res[1] = temp[1];
        if(temp[0].contains("third")){
            res[0] = "third_party";
        }else if(temp[0].contains("advert")){
            res[0] = "advertising";
        }else if(temp[0].contains("analytic")){
            res[0] = "analytics";
        }else if(temp[0].contains("social")){
            res[0] = "social_network_integration";
        }else{
            res[0] = temp[0];
        }
        if(temp[2].contains("pii")){
            res[2] = "information";
        }else if(temp[2].contains("identifier")||temp[2].contains("gsfid")||temp[2].contains("mac")){
            res[2] = "device identifier";
        }else if(temp[2].contains("email")){
            res[2] = "email";
        }else if(temp[2].contains("phone")){
            res[2] = "phone number";
        }else{
            res[2] = "information";
        }
        if(res[0].charAt(res[0].length()-1)==')'){
            res[0] = res[0].substring(0,res[0].length()-1);
        }
        return res;
    }
}
