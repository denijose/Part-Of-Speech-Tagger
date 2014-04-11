/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package posTagLearn;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import util.*;

/**
 *
 * @author denis
 */
public class POSTagLearn {

    /**
     * @param args the command line arguments
     */      
    private static String TRAINFILENAME;
    private static String MODEL;
    private static String CURRENT;
    private static String TEMPDIR;

    
    public static void main(String[] args) throws IOException {
        parseArgs(args);
        createTraininFileForMegaM();
        callMegaMToLearn();
    }
    
    public static void parseArgs(String[] args) {
              if(args.length < 2){
             System.out.println("Insufficient arguments. Usage -");
             System.out.println("java -jar postrain.jar TRAININGFILE MODEL");
             System.exit(1);
       }
       TRAINFILENAME = args[0];
       MODEL = args[1];
       
}

    private static void createTraininFileForMegaM() throws FileNotFoundException, IOException {
        CURRENT = new File( "." ).getCanonicalPath();
        TEMPDIR = CURRENT + "/denisjos_temp";
        File tempDir = new File(TEMPDIR);
        tempDir.mkdir();
        System.out.println("A temp directory -" + tempDir.getAbsolutePath() + " has been made. Please do not delete the dir till after the end.." );
        BufferedReader br = new BufferedReader(new FileReader(TRAINFILENAME));
        File trainingFileForMegaM = new File(tempDir.getAbsolutePath()+"/pos.tr");
        if(trainingFileForMegaM.exists())
            trainingFileForMegaM.delete();
        trainingFileForMegaM.createNewFile();
        BufferedWriter bw = new BufferedWriter(new FileWriter(trainingFileForMegaM.getAbsolutePath()));
        
        String line = null;       
        while((line=br.readLine()) != null){
            String nw = null;
            String pw1 = "Start", pw2 = "Start";
            String psuf = "StartSuf";
            String cursuf = null;
            String[] components = line.split(" ");
            for(int i=0; i<components.length; i++){
                String word = components[i].split("/")[0];
                cursuf = word.length() > 2 ? word.substring(word.length() - 2) : word;
                if(word.equalsIgnoreCase("#"))
                    word = "HASH";
                String tag =  components[i].split("/")[1];                
                if(i==components.length-1)
                    nw="End";
                else
                    nw = components[i+1].split("/")[0];
                String trainLine = tag + " w:" + word + " pw1:" + pw1 + " pw2:" + pw2 + " nw:" + nw + " cursuf:" + cursuf + " psuf:" + psuf ;
                //String trainLine = tag + " w:" + word + " pw1:" + pw1 + " pw2:" + pw2 + " nw:" + nw ;
                bw.write(trainLine);
                //if(!(i==components.length-1))
                    bw.write("\n");
                pw2=pw1;
                pw1=word;
                psuf=cursuf;

            }
        }
        br.close();
        bw.close();
    }

    private static void callMegaMToLearn() {
        System.out.println("Calling MegaM to learn...");
        try{
            String cmd = CURRENT + "/megam.opt " + " -nc multitron " + TEMPDIR +"/pos.tr";
            Process pr = Runtime.getRuntime().exec(cmd);

            StreamGobbler errorGobbler = new StreamGobbler(pr.getErrorStream(), "ERROR"); 
            FileOutputStream fos = new FileOutputStream(MODEL);
            StreamGobbler outputGobbler = new StreamGobbler(pr.getInputStream(), "OUTPUT", fos);
            errorGobbler.start();
            outputGobbler.start();
      
            int exitVal = pr.waitFor();
            if(exitVal == 0)
                System.out.println("Finished Learning successfully ");
            else
                System.out.println("There was some error in MegaM's Learning..");
            fos.flush();
            fos.close();
           // bw.close();
            
        } catch (IOException | InterruptedException t){
            t.printStackTrace();
        }
    }
    
}
