package tool.consistency;

import java.util.LinkedList;
import java.util.List;

public class ConsistencyAnalysisResult {
    /*
   incorrectStatement:隐私策略中声明没有收集的信息在实际应用中收集了
   omittedStatement:实际应用中收集的信息未在隐私策略中描述
   ambiguousStatement:实际应用中收集的信息与隐私策略声明中的描述不完全符合
   redundantStatement:隐私策略中声明收集的信息在实际应用中并未收集
    */
    List<String> incorrectStatement;
    List<String> omittedStatement;
    //vagueStatement更为合适
    List<String> ambiguousStatement;
    List<String> redundantStatement;
    List<String> clearStatement;

    ConsistencyAnalysisResult(){
        this.incorrectStatement = new LinkedList<>();
        this.ambiguousStatement =  new LinkedList<>();
        this.omittedStatement = new LinkedList<>();
        this.redundantStatement = new LinkedList<>();
        this.clearStatement = new LinkedList<>();
    }

    public boolean isViolation(ConsistencyAnalysisResult result){
        return result.incorrectStatement != null || result.omittedStatement != null;
    }

    public boolean isRedundant(ConsistencyAnalysisResult result){
        return result.redundantStatement != null;
    }

    public List<String> getIncorrectStatement(ConsistencyAnalysisResult result){
        return result.incorrectStatement;
    }

    public List<String> getAmbiguousStatement(ConsistencyAnalysisResult result){
        return result.ambiguousStatement;
    }

    public List<String> getOmittedStatement(ConsistencyAnalysisResult result){
        return result.omittedStatement;
    }

    public List<String> getRedundantStatement(ConsistencyAnalysisResult result){
        return result.redundantStatement;
    }

    public int inconsistencyResultNumbers(ConsistencyAnalysisResult result){
        return result.omittedStatement.size()+result.incorrectStatement.size();
    }

    public void outputInconsistentResults(ConsistencyAnalysisResult result){
        if(isViolation(result)||isRedundant(result)){
            List<String> incorrectStatement = getIncorrectStatement(result);
            List<String> omittedStatement = getOmittedStatement(result);
            List<String> ambiguousStatement = getAmbiguousStatement(result);
            List<String> redundantStatement = getRedundantStatement(result);

            if(incorrectStatement!=null){
                System.out.println("Incorrect Statement：");
                for(int i=0;i<incorrectStatement.size();i++){
                    System.out.println(incorrectStatement.get(i));
                }
            }else {
                System.out.println("There is no incorrect Statement.");
            }

            if(omittedStatement!=null){
                System.out.println("Omitted Statement：");
                for(int i=0;i<omittedStatement.size();i++){
                    System.out.println(omittedStatement.get(i));
                }
            }else {
                System.out.println("There is no omitted Statement.");
            }

            if(ambiguousStatement!=null){
                System.out.println("Ambiguous Statement：");
                for(int i=0;i<ambiguousStatement.size();i++){
                    System.out.println(ambiguousStatement.get(i));
                }
            }else {
                System.out.println("There is no ambiguous Statement.");
            }

            /*
            //不考虑冗余的情况
            if(redundantStatement!=null){
                System.out.println("冗余的声明如下：");
                for(int i=0;i<redundantStatement.size();i++){
                    System.out.println(redundantStatement.get(i));
                }
            }else {
                System.out.println("无冗余的声明。");
            }
             */
        }
    }
}
