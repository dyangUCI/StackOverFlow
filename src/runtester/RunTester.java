package runtester;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang3.StringEscapeUtils;



public class RunTester {
		
	public static void main(String args[]){
	
		// Create a File object on the root of the directory
        // containing the class file
        File file = new File("/Users/Di/Documents/eclipse/workspace/StackOverFlow/bin/compilable");
        int count = 0;
        ArrayList<String> runnable = new ArrayList<String>();
        ArrayList<String> unrunnable = new ArrayList<String>();
            	
        try
        {
            // Convert File to a URL
            URL url = file.toURI().toURL();
            URL[] urls = new URL[] { url };
 
            // Create a new class loader with the directory
            ClassLoader loader = new URLClassLoader(urls);
 
            // Load in the class; Class.childclass should be located in
            // the directory file:/bin/compilable
            File[] files = file.listFiles();
            System.out.println("Number of compilable classes: " + files.length);
            for(File f: files){
            	try{
            		System.out.println(f.getName().substring(0,f.getName().length()-6));
            		Class thisClass = loader.loadClass(f.getName().substring(0,f.getName().length()-6));
                    Object instance = thisClass.newInstance();
            	}catch(Exception e){
  //          		e.printStackTrace();
            		unrunnable.add(f.getName());
            		count++;
            	}              
            }       
            for (File f : files){
            	if (!unrunnable.contains(f.getName())){
            		runnable.add(f.getName());
            	}
            }
        }catch (Exception ex){
            ex.printStackTrace();
        }
        
        System.out.println("Number of Files that compile but not run: " + count);
        System.out.println("Number of Files that compile and run: " + runnable.size());
        for(String s : runnable){
        	System.out.println(s);
        }
					
	}
	
	
}
