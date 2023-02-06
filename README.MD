PTPDroid is an Eclipse project. Then please follow the following three steps to make the code run:

1.Import the project first: After openning Eclipse, please click: ①File ②Import ③General ④Existing Projects into Workspace; You need to add external JARS from ./libs/.

2.Modify parameters: Please open Java file "tool.java.main" in the PTPDroid to modify the variables "apkPath" and "jarsPath" to your own. "jarspath" refers to the Android sdk platforms folder, and "apkpath" refers to the path of the apks(apps) to be analyzed. Other configurations can be found in the file "configs", which are related to the ontologies and mappings.

3.Run: Run tool.java.main, and after a while the results that which third-parties collect which data types will be generated. If you want to get the consistency results between the app code and the privacy policy. You can put the results of PolicyLint (https://github.com/benandow/PrivacyPolicyAnalysis) in the variables "privacyPolicyResults". The output classfied flow results into four types of disclosures: clear disclosure, vague disclosures, omitted disclosures, and incorrect disclosures.
