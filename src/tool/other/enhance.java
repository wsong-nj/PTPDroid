package tool.other;

import soot.G;
import soot.PackManager;
import soot.Scene;
import soot.SootClass;
import soot.options.Options;
import soot.toolkits.scalar.ConstantValueToInitializerTransformer;

import java.util.*;

public class enhance {

    public static List<String[]> modify(List<String[]> flowResults){
        Map<String, Set<String>> map = new HashMap<>();
        for(String[] flowRes : flowResults){
            if(flowRes[0].contains("<")||flowRes[0].contains("(")||flowRes[1].equals("message")||flowRes[1].contains("<")||flowRes[1].contains("(")){
            }else {
                if(map.containsKey(flowRes[0])){
                    if(flowRes[1].contains("advertising")||flowRes[1].contains("android id")||flowRes[1].contains("device description")){
                        map.get(flowRes[0]).add("device identifier");
                    }else {
                        map.get(flowRes[0]).add(flowRes[1]);
                    }
                }else {
                    Set<String> set = new HashSet<>();
                    if(flowRes[1].contains("advertising")||flowRes[1].contains("android id")||flowRes[1].contains("device description")){
                        set.add("device identifier");
                    }else {
                        set.add(flowRes[1]);
                    }
                    map.put(flowRes[0],set);
                }
            }
        }
        List<String[]> res = new LinkedList<>();
        Set<Map.Entry<String, Set<String>>> entries = map.entrySet();
        for (Map.Entry<String, Set<String>>entry:entries){
            Set<String> values = entry.getValue();
            String key = entry.getKey();
            for(String value : values){
                String[] temp = new String[]{key,value};
                res.add(temp);
            }
        }
        return res;
    }

    public static void sootIni(String apkFileLocation, String apkName, String androidJar, boolean isOutputJimple){
        G.reset();

        Options.v().set_process_dir(Collections.singletonList(apkFileLocation));
        //prefer Android APK files// -src-prec apk
        Options.v().set_src_prec(Options.src_prec_apk);
        //output format //–f
        Options.v().set_force_android_jar(androidJar);

        //prepend the VM's classpath to Soot's own classpath
        Options.v().set_prepend_classpath(true);
        if (isOutputJimple) {
            Options.v().set_output_format(Options.output_format_jimple);
            Options.v().set_output_dir("E:\\IdeaProjects\\TokenDroid\\sootOutput\\" + apkName);
        }
        else
            Options.v().set_output_format(Options.output_format_none);
        Options.v().set_whole_program(true);
        Options.v().set_allow_phantom_refs(true);
        Options.v().set_process_multiple_dex(true);
        Options.v().set_keep_line_number(true);
        Options.v().process_multiple_dex();

        Scene.v().loadNecessaryClasses();

        PackManager.v().runPacks();

        if (isOutputJimple){

            PackManager.v().writeOutput();
            System.out.println("Jimple file generated.");
        }
        else {
            for (SootClass sc : Scene.v().getApplicationClasses()){
                if (!sc.isPhantom())
                    ConstantValueToInitializerTransformer.v().transformClass(sc);
            }
        }

    }

}
