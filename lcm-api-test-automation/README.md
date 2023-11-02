#Prerequisites
Install the below:
* Java - JDK (16)
* Maven - 3.9.1
* IntelliJ (preferred) or Eclipse

Check these settings and make sure we use Java 7 and above:
* File > Project Structure > Project > Project SDK
* File > Project Structure > Project > Project Language Level
* File > Project Structure > Modules > Your module > Sources > Language Level
* File > Project Structure > Modules > Your module > Dependencies > Module SDK

Also check compiler settings. Sometimes it adds extra arguments to compiler:
* File > Settings > Compiler > Java Compiler > Byte code version
* If you use maven plugin enable auto-import. Then the language level will be detected automatically from your pom.xml settings.

//#How to open this Project
* From IntelliJ > File >Open > navigate to the project folder on your desktop
* Select the pom.xml and click OPEN
* Choose to open existing project when prompted
* Choose this window when prompted
* If Event log (bottom-right pane) shows an option to "Enable Auto Import", then choose it
* Wait while the maven dependacies resolve

//#Common Issues and Answers

##### To generate the latest customized automation report, kindly the Serenity summary report jar in .m2 folder before starting the execution
###### Copy the file: "src/main/resources/data/Report/serenity-single-page-report-3.6.21.jar"
###### Replace here: "C:\Users\{sys.username}\.m2\repository\net\serenity-bdd\serenity-single-page-report\3.6.21"

##### I keep getting the error, “Unimplemented substep definition” in IntelliJ with Cucumber??
###### Ans : Uninstall Substeps IntelliJ Plugin. Cucumber for Java and Gherkin should be enough.

##### I keep getting the error, "Error running 'Scenario: .........': Command line is too long. Shorten command line for Scenario: ...... or also for Cucumber java default configuration."
###### Ans : Click on "Cucumber java default configuration" from th error that opens-up a window titled "Edit Default Configuration"
###### Under "Shorten Command Line" > Select "JAR manifest" > Apply > OK


//--#Commands to Run

##### Run the Test Suites by Tags. For example: VTS
#### The tags of any features to be run can be found in the project under the location: src/test/resources/features/*.feature 
### To run against the parameters configured in runner.properties, then please refer below
mvn clean verify -Dtags="VTSTests"
### To run with your own parameters, then please refer below
mvn clean verify -Dtags="VTSTests" -Denvironment="Demo" -Dissuer="Comdirect" -DtestCardsEnabled="Yes" -DallScenarios="Yes" 