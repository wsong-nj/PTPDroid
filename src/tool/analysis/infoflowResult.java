package tool.analysis;

import org.xmlpull.v1.XmlPullParserException;
import soot.*;
import soot.jimple.infoflow.InfoflowConfiguration;
import soot.jimple.infoflow.android.InfoflowAndroidConfiguration;
import soot.jimple.infoflow.android.SetupApplication;
import soot.jimple.infoflow.results.InfoflowResults;
import soot.jimple.infoflow.taintWrappers.EasyTaintWrapper;
import soot.jimple.infoflow.taintWrappers.ITaintPropagationWrapper;
import soot.jimple.toolkits.callgraph.CallGraph;
import soot.options.Options;

import java.io.IOException;
import java.util.*;

import static soot.SootClass.BODIES;

public class infoflowResult {
    public static CallGraph getCallGraph(String[] configs){
        Options.v().set_src_prec(Options.src_prec_apk);
        Options.v().set_output_format(Options.output_format_jimple);
        Options.v().set_process_multiple_dex(true);
        Options.v().set_output_dir("JimpleOutput");
        Options.v().set_keep_line_number(true);
        Options.v().set_prepend_classpath(true);
        Options.v().set_no_bodies_for_excluded(true);
        Options.v().set_allow_phantom_refs(true);
        Options.v().set_process_dir(Collections.singletonList(configs[0]));
        Options.v().set_android_jars(configs[1]);
        Options.v().set_whole_program(true);
        Options.v().set_force_overwrite(true);

        //Scene.v().addBasicClass("android.util.Log",BODIES);
        Scene.v().loadNecessaryClasses();
        String androidJarPath = Scene.v().getAndroidJarPath(configs[1], configs[0]);
        SetupApplication setupApplication = new SetupApplication(androidJarPath, configs[0]);
        setupApplication.constructCallgraph();
        //setupApplication.printEntrypoints();
        //System.out.println("callgraph构建完成！"+Scene.v().getCallGraph().size());
        return Scene.v().getCallGraph();
    }


    //sink sinkSignature source sourceSignature
    //InThirdPartyLibrary signature source null
    public static List<String[]> taintAnalysis(String[] configs)throws IOException, XmlPullParserException{
        Options.v().set_src_prec(Options.src_prec_apk);
        Options.v().set_output_format(Options.output_format_jimple);
        Options.v().set_process_multiple_dex(true);
        Options.v().set_output_dir("JimpleOutput");
        Options.v().set_keep_line_number(true);
        Options.v().set_prepend_classpath(true);
        Options.v().set_no_bodies_for_excluded(true);
        Options.v().set_allow_phantom_refs(true);
        Options.v().set_process_dir(Collections.singletonList(configs[0]));
        Options.v().set_android_jars(configs[1]);
        Options.v().set_whole_program(true);
        Options.v().set_force_overwrite(true);

        Scene.v().loadNecessaryClasses();

        String androidJarPath = Scene.v().getAndroidJarPath(configs[1], configs[0]);
        SetupApplication setupApplication = new SetupApplication(androidJarPath, configs[0]);
        ITaintPropagationWrapper iTaintPropagationWrapper = new EasyTaintWrapper(configs[4]);
        setupApplication.setTaintWrapper(iTaintPropagationWrapper);
        setupApplication.setCallbackFile(configs[2]);

        InfoflowAndroidConfiguration config = setupApplication.getConfig();
        //性能优化
        //config.setAccessPathLength(4);
        //config.setAliasingAlgorithm(InfoflowConfiguration.AliasingAlgorithm.Lazy);
        //config.setEnableExceptionTracking(false);
        //config.setEnableStaticFieldTracking(false);
        config.setDataFlowTimeout(300);

        config.setCallgraphAlgorithm(InfoflowConfiguration.CallgraphAlgorithm.SPARK);
        config.setTaintAnalysisEnabled(true);
        config.setLogSourcesAndSinks(true);
        config.setMergeDexFiles(true);
        //config.setImplicitFlowMode(ArrayAccesses);
        InfoflowResults infoflowResults = setupApplication.runInfoflow(configs[3]);
        System.out.println("FlowDroid运行结束！");
        return setupApplication.SignatureResults;
    }

    public static List<String[]> getAdIDBySoot(String[] configs){
        List<String[]> res = new LinkedList<>();

        Options.v().set_src_prec(Options.src_prec_apk);
        Options.v().set_output_format(Options.output_format_jimple);
        Options.v().set_process_multiple_dex(true);
        Options.v().set_output_dir("JimpleOutput");
        Options.v().set_keep_line_number(true);
        Options.v().set_prepend_classpath(true);
        Options.v().set_no_bodies_for_excluded(true);
        Options.v().set_allow_phantom_refs(true);
        Options.v().set_process_dir(Collections.singletonList(configs[0]));
        Options.v().set_android_jars(configs[1]);
        Options.v().set_whole_program(true);
        Options.v().set_force_overwrite(true);

        Scene.v().loadNecessaryClasses();
        PackManager.v().runPacks();

        for(SootClass sc:Scene.v().getApplicationClasses()){
            if (!sc.isPhantom()){
                List<SootMethod> list = sc.getMethods();
                for(SootMethod sootMethod:list){
                    if(isThirdPartyLib(sootMethod.getSignature())){
                        if(sootMethod.getSignature().toLowerCase().contains("getadvertisingid")){
                            String[] temp = new String[]{sootMethod.getSignature(),"advertiser id"};
                            res.add(temp);
                        }
                        if(sootMethod.getSignature().toLowerCase().contains("getandroidid")){
                            String[] temp = new String[]{sootMethod.getSignature(),"android id"};
                            res.add(temp);
                        }
                    }
                }
            }
        }
        for(String[] strings:res){
            System.out.println(strings[0]);
        }
        return res;
    }

    public static List<String[]> getInfoByValueBox(String[] configs){
        List<String[]> res = new LinkedList<>();

        Options.v().set_src_prec(Options.src_prec_apk);
        Options.v().set_output_format(Options.output_format_jimple);
        Options.v().set_process_multiple_dex(true);
        Options.v().set_output_dir("JimpleOutput");
        Options.v().set_keep_line_number(true);
        Options.v().set_prepend_classpath(true);
        Options.v().set_no_bodies_for_excluded(true);
        Options.v().set_allow_phantom_refs(true);
        Options.v().set_process_dir(Collections.singletonList(configs[0]));
        Options.v().set_android_jars(configs[1]);
        Options.v().set_whole_program(true);
        Options.v().set_force_overwrite(true);

        Scene.v().loadNecessaryClasses();
        PackManager.v().runPacks();

        for(SootClass sc:Scene.v().getApplicationClasses()){
            if (!sc.isPhantom()){
                List<SootMethod> list = sc.getMethods();
                for(SootMethod sootMethod:list){
                    String s = sootMethod.getSignature();
                    if(isThirdPartyLib(s)){
                        if(sootMethod.getSignature().toLowerCase().contains("getadvertisingid")){
                            String[] temp = new String[]{sootMethod.getSignature(),"advertising id"};
                            res.add(temp);
                            continue;
                        }
                        if(sootMethod.getSignature().toLowerCase().contains("getandroidid")){
                            String[] temp = new String[]{sootMethod.getSignature(),"android id"};
                            res.add(temp);
                            continue;
                        }
                        if(sootMethod.getSignature().contains("CrashlyticsController")&&sootMethod.toString().contains("writeSessionDevice")){
                            String[] temp = new String[]{"crashlytics","advertising id"};
                            res.add(temp);
                            continue;
                        }
                        if(sootMethod.hasActiveBody()){
                            Body body = sootMethod.getActiveBody();
                            for(ValueBox valueBox:body.getUseAndDefBoxes()){
                                if(isAdId(valueBox.toString())){
                                    String[] temp = new String[]{sootMethod.getSignature(),"advertising id"};
                                    res.add(temp);
                                }
                                if(isAndroidId(valueBox.toString())){
                                    String[] temp = new String[]{sootMethod.getSignature(),"android id"};
                                    res.add(temp);
                                }
                                /*
                                if(isLocationInfo(valueBox.toString())){
                                    String[] temp = new String[]{sootMethod.getSignature(),"location"};
                                    res.add(temp);
                                }
                                 */
                                if(isAndroidOsBuild(valueBox.toString())){
                                    String[] temp = new String[]{sootMethod.getSignature(),"device description"};
                                    res.add(temp);
                                }
                            }
                        }
                    }
                }
            }
        }
        /*
        for(String[] strings:res){
            System.out.println(strings[0]+"  "+strings[1]);
        }
         */

        return res;
    }

    private static boolean isThirdPartyLib(String s) {
        if(s.startsWith("<com.google")||s.startsWith("<android")||s.startsWith("<androidx")||s.startsWith("org")||
            s.startsWith("okhttp")||s.startsWith("retrofit")){
            return false;
        }
        return true;
    }

    private static boolean isAndroidOsBuild(String str) {
        if(str.contains("<android.os.Build: java.lang.String")){
            if(str.contains("FINGERPRINT")||str.contains("SERIAL")||str.contains("INCREMENTAL")){
                return true;
            }
            return false;
        }
        return false;
    }

    private static boolean isLocationInfo(String str) {
        if(str.contains("android.location.Location")){
            return true;
        }
        return false;
    }

    private static boolean isAndroidId(String str) {
        if(str.toLowerCase().contains("androidid")||str.toLowerCase().contains("android_id")){
            return true;
        }
        return false;
    }

    //advertiserId advertising_id
    private static boolean isAdId(String str) {
        //str.contains("advertiserId")
        if(str.contains("advertising_id")||(str.toLowerCase().contains("advertisingid"))){
            return true;
        }
        return false;
    }

    //判断是否有代码混淆
    public static void PrintGivenSootMethod(String[] configs){
        List<String[]> res = new LinkedList<>();

        Options.v().set_src_prec(Options.src_prec_apk);
        Options.v().set_output_format(Options.output_format_jimple);
        Options.v().set_process_multiple_dex(true);
        Options.v().set_output_dir("JimpleOutput");
        Options.v().set_keep_line_number(true);
        Options.v().set_prepend_classpath(true);
        Options.v().set_no_bodies_for_excluded(true);
        Options.v().set_allow_phantom_refs(true);
        Options.v().set_process_dir(Collections.singletonList(configs[0]));
        Options.v().set_android_jars(configs[1]);
        Options.v().set_whole_program(true);
        Options.v().set_force_overwrite(true);

        Scene.v().loadNecessaryClasses();
        PackManager.v().runPacks();

        for(SootClass sc:Scene.v().getApplicationClasses()){
            if (!sc.isPhantom()){
                List<SootMethod> list = sc.getMethods();
                for(SootMethod sootMethod:list){
                    String s = sootMethod.getSignature();
                    if(isThirdPartyLib(s)){
                        if(s.contains("<com.segment.analytics")){
                            System.out.println(sootMethod.getSignature());
                            if(sootMethod.hasActiveBody()){
                                //System.out.println(sootMethod.getActiveBody().toString());
                            }
                        }
                    }
                }
            }
        }
        for(SootClass sc:Scene.v().getApplicationClasses()){
            if(!sc.isPhantom()){
                List<SootMethod> list = sc.getMethods();
                for(SootMethod sootMethod : list){
                    if(sootMethod.hasActiveBody()){
                        for(ValueBox valueBox : sootMethod.getActiveBody().getUseBoxes()){
                            if((valueBox.toString().toLowerCase().contains("nexage")||valueBox.toString().toLowerCase().contains("adsrvr")||
                                    valueBox.toString().toLowerCase().contains("adaptv")||valueBox.toString().toLowerCase().contains("branch")||
                                    valueBox.toString().toLowerCase().contains("smaato")||valueBox.toString().toLowerCase().contains("mopub")||
                                    valueBox.toString().toLowerCase().contains("pubnative")||valueBox.toString().toLowerCase().contains("adcolony")||
                                    valueBox.toString().toLowerCase().contains("unity")||valueBox.toString().toLowerCase().contains("criteo")||
                                    valueBox.toString().toLowerCase().contains("tapjoy")||valueBox.toString().toLowerCase().contains("ironsource")||
                                    valueBox.toString().toLowerCase().contains("applovin")||
                                    valueBox.toString().toLowerCase().contains("inmobi")||valueBox.toString().toLowerCase().contains("adview")||
                                    valueBox.toString().toLowerCase().contains("admixer")||valueBox.toString().toLowerCase().contains("fyber")||
                                    valueBox.toString().toLowerCase().contains("kochava")||
                                    //valueBox.toString().toLowerCase().contains("appsflyer")||
                                    valueBox.toString().toLowerCase().contains("remerge")||valueBox.toString().toLowerCase().contains("crashlytics")||
                                    valueBox.toString().toLowerCase().contains("bugsnag")||valueBox.toString().toLowerCase().contains("admob")||
                                    valueBox.toString().toLowerCase().contains("fabric")||valueBox.toString().toLowerCase().contains("appnexus"))&&
                                    !valueBox.toString().toLowerCase().contains("google") ){
                                System.out.println(sootMethod.getSignature()+" : "+valueBox.toString());
                                //System.out.println(valueBox.toString());
                            }
                        }
                    }
                }
            }
        }

    }


}
