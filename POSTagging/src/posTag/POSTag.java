/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package posTag;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.Runtime;
import java.lang.Process;



/**
 *
 * @author denis
 */
public class POSTag {
    
    private static String CURRENT;
    private static String TEMPDIR;
    private static String TESTFILENAME = "/home/denis/Projects/NLP_HW2/pos.blind.test";
    private static String TESTFILENAMEFORMEGAM = "/pos.test";
    private static String MODELFILENAME;

    
    public static void main(String[] args) throws IOException{
        parseArgs(args);
        createTestFileForMegaM();
       // callMegaM();
    }
    
        public static void parseArgs(String[] args) {
              if(args.length < 1){
             System.out.println("Insufficient arguments. Usage -");
             System.out.println("java -jar postag.jar MODEL");
             System.exit(1);
       }
       MODELFILENAME = args[0];
    }

    private static void createTestFileForMegaM() throws FileNotFoundException, IOException {
        CURRENT = new File( "." ).getCanonicalPath();
        TEMPDIR = CURRENT + "/denisjos_temp";
        File tempDir = new File(TEMPDIR);
        if(!tempDir.exists())
            tempDir.mkdir();
        //System.out.println("Please kill the process at the shell to exit or will exist at the end of the inputstream automatically...");
        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
        //BufferedReader br = new BufferedReader(new FileReader(TESTFILENAME));
            
        File testFileForMegaM = new File(tempDir.getAbsolutePath()+TESTFILENAMEFORMEGAM);
                
        String line = null;       
        while((line=br.readLine()) != null){
//            if(line.equalsIgnoreCase("1")){
//                 System.out.println("Exiting...");
//                break;
//            }   
            if(testFileForMegaM.exists())
                 testFileForMegaM.delete();
            testFileForMegaM.createNewFile();
            BufferedWriter bw = new BufferedWriter(new FileWriter(testFileForMegaM.getAbsolutePath()));

            String nw = null;
            String pw1 = "Start", pw2 = "Start"; 
            String psuf = "StartSuf";
            String cursuf = null;
            String[] components = line.split(" ");
            for(int i=0; i<components.length; i++){
                String word = components[i];
                cursuf = word.length() > 2 ? word.substring(word.length() - 2) : word;
                if(word.equalsIgnoreCase("#"))
                    word = "HASH";
                if(i==components.length-1)
                    nw="End";
                else
                    nw = components[i+1];
                String trainLine = "0 w:" + word + " pw1:" + pw1 + " pw2:" + pw2 + " nw:" + nw  + " cursuf:" + cursuf + " psuf:" + psuf ;;
                bw.write(trainLine);
                //if(!(i==components.length-1))
                    bw.write("\n");
                pw2=pw1;
                pw1=word;
                psuf=cursuf;
               
            }
            bw.close();
            //System.out.println("Calling MegaM to tag...");
            callMegaM();
            finalOutput(line);
        }
        br.close();
        //bw.close();
    }

    private static void callMegaM() {        
        try{
            String cmd = CURRENT + "/megam.opt " + "-predict " + MODELFILENAME + "  -nc multitron " + TEMPDIR +TESTFILENAMEFORMEGAM;
            Process pr = Runtime.getRuntime().exec(cmd);
            InputStream stdin = pr.getInputStream();
            InputStreamReader isr = new InputStreamReader(stdin);
            BufferedReader br = new BufferedReader(isr);
            String line = null;
            //System.out.println("");
            
            File outPutFile = new File(TEMPDIR + "/out");
            if(outPutFile.exists())
                outPutFile.delete();
            outPutFile.createNewFile();
            BufferedWriter bw = new BufferedWriter(new FileWriter(outPutFile.getAbsolutePath()));
            while ( (line = br.readLine()) != null)
                bw.write(line+ "\n");
             
//             while ( (line = br.readLine()) != null)
//System.out.println(line);

            int exitVal = pr.waitFor();
            //System.out.println("Process exitValue: " + exitVal);
            bw.close();
            br.close();
        } catch (IOException | InterruptedException t){
            t.printStackTrace();
        }
    }

    private static void finalOutput(String line) throws FileNotFoundException, IOException {
        String[] components = line.split(" ");
            
            BufferedReader br = new BufferedReader(new FileReader(TEMPDIR + "/out"));
            String line2 = null;
            int i=0;
            while( (line2=br.readLine()) != null){
                String tag = line2.split("\t")[0];
                System.out.print(components[i++]+"/"+tag + " ");
            }
               System.out.print("\n"); 
               br.close();
        
    }
    
}
