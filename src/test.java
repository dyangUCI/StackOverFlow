


import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
//import java.util.logging.Logger;

import javax.tools.Diagnostic;
import javax.tools.DiagnosticCollector;
import javax.tools.JavaCompiler;
import javax.tools.JavaCompiler.CompilationTask;
import javax.tools.JavaFileObject;
import javax.tools.SimpleJavaFileObject;
import javax.tools.StandardJavaFileManager;
import javax.tools.ToolProvider;

import org.apache.commons.lang3.StringEscapeUtils;

/**
 * A test class to test dynamic compilation API.
 * 
 */
public class test {
//	final Logger logger = Logger.getLogger(CompileTester.class.getName()) ;
	
	/**Java source code to be compiled dynamically*/
	Map<String, String> snippets = new HashMap<String, String>(); // key=id, value=snippet
	ArrayList<DynamicJavaSourceCodeObject> unCompilable = new ArrayList<DynamicJavaSourceCodeObject>();//record file objects that do not compile
	Map<String, String> compilable = new HashMap<String, String>();//record the snippets that compile
	
	@SuppressWarnings("resource")
//	public void readFile(){
//		String temp = "";
//		String[] str = new String[2];
//		int i = 0;
//		try {
//			BufferedReader br = new BufferedReader(new FileReader(new File("/Users/Di/Desktop/java_snippets.txt")));
//			while ((temp = br.readLine()) != null )
//			{
//				
//				str = temp.split("-------->");
//				str[1] = StringEscapeUtils.unescapeHtml4(str[1]);
//				str[1] = StringEscapeUtils.unescapeHtml3(str[1]);
//				str[1] = StringEscapeUtils.unescapeXml(str[1]);
//				snippets.put(str[0], str[1]);
//				i++;
//			}
//			System.out.println(i);
//		} catch (FileNotFoundException e) {
//			e.printStackTrace();
//		} catch (IOException e) {
//			e.printStackTrace();
//		}
//	}
	
	/**
	 * Does the required object initialization and compilation.
	 */
	public void doCompilation (){
		/*Creating dynamic java source code file object*/
		snippets.put("2000", "@OneToMany(mappedBy=\"foo\", orphanRemoval=true)");
		JavaFileObject[] javaFileObjects = new JavaFileObject[snippets.size()];
		System.out.println(javaFileObjects.length);
		
		Iterator<Entry<String, String>> it = snippets.entrySet().iterator();
		int i=0;
		while (it.hasNext()){
			Map.Entry<String, String> entry = it.next();
//			System.out.println(entry);
			SimpleJavaFileObject fileObject = new DynamicJavaSourceCodeObject (entry.getKey(), entry.getValue()) ;
			System.out.println(fileObject);
			javaFileObjects[i] = fileObject;
			i++;
		}	
		
		/*Instantiating the java compiler*/
		JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
		
		/**
		 * Retrieving the standard file manager from compiler object, which is used to provide
		 * basic building block for customizing how a compiler reads and writes to files.
		 * 
		 * The same file manager can be reopened for another compiler task. 
		 * Thus we reduce the overhead of scanning through file system and jar files each time 
		 */
		StandardJavaFileManager stdFileManager = compiler.getStandardFileManager(null, Locale.getDefault(), null);
		
		/* Prepare a list of compilation units (java source code file objects) to input to compilation task*/
		Iterable<? extends JavaFileObject> compilationUnits = Arrays.asList(javaFileObjects);
		
		/*Prepare any compilation options to be used during compilation*/
		//In this example, we are asking the compiler to place the output files under bin folder.
		String[] compileOptions = new String[]{"-d", "bin"} ;
		Iterable<String> compilationOptionss = Arrays.asList(compileOptions);
		
		/*Create a diagnostic controller, which holds the compilation problems*/
		DiagnosticCollector<JavaFileObject> diagnostics = new DiagnosticCollector<JavaFileObject>();
		
		/*Create a compilation task from compiler by passing in the required input objects prepared above*/
		CompilationTask compilerTask = compiler.getTask(null, stdFileManager, diagnostics, compilationOptionss, null, compilationUnits) ;
		
		//Perform the compilation by calling the call method on compilerTask object.
		boolean status = compilerTask.call();
		
		if (!status){//If compilation error occurs
			/*Iterate through each compilation problem and print it*/
//			System.out.println("number of errors: "+ diagnostics.getDiagnostics().size());
			for (Diagnostic<?> diagnostic : diagnostics.getDiagnostics()){
				System.out.format("Error on line %d in %s", diagnostic.getLineNumber(), diagnostic);
				DynamicJavaSourceCodeObject source = (DynamicJavaSourceCodeObject)diagnostic.getSource();	
				if (!unCompilable.contains(source)){
					unCompilable.add(source);
				}
				
			}
		}
		for (JavaFileObject jfo : javaFileObjects){
			if(!unCompilable.contains(jfo)){
				compilable.put(((DynamicJavaSourceCodeObject) jfo).getQualifiedName(), ((DynamicJavaSourceCodeObject) jfo).getSourceCode());
			}
		}
		
		Iterator<Entry<String, String>> iter = compilable.entrySet().iterator();
		while (iter.hasNext()){
			Map.Entry<String, String> entry = iter.next();
			System.out.println(entry);
		}	
		
		System.out.println("Number of cleaned snippets: "+ javaFileObjects.length);
		System.out.println("Number of Compilable Files: "+ compilable.size());
		System.out.println("Number of Uncompilable Files: "+ unCompilable.size());
		
		
		try {
			stdFileManager.close() ;//Close the file manager
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public static void main(String args[]){
		test ct = new test();
	//	ct.readFile();
		ct.doCompilation() ;
	}

}

/**
 * Creates a dynamic source code file object
 * 
 * This is an example of how we can prepare a dynamic java source code for compilation.
 * This class reads the java code from a string and prepares a JavaFileObject
 * 
 */
class DynamicJavaSourceCodeObject extends SimpleJavaFileObject{
	private String qualifiedName ;
	private String sourceCode ;
	
	/**
	 * Converts the name to an URI, as that is the format expected by JavaFileObject
	 * 
	 * 
	 * @param fully qualified name given to the class file
	 * @param code the source code string
	 */
	protected DynamicJavaSourceCodeObject(String name, String code) {
		super(URI.create("string:///"+ name + Kind.SOURCE.extension), Kind.SOURCE);
		this.qualifiedName = name ;
		this.sourceCode = code ;
	}
	
	@Override
	public CharSequence getCharContent(boolean ignoreEncodingErrors)
			throws IOException {
		return sourceCode ;
	}

	public String getQualifiedName() {
		return qualifiedName;
	}

	public void setQualifiedName(String qualifiedName) {
		this.qualifiedName = qualifiedName;
	}

	public String getSourceCode() {
		return sourceCode;
	}

	public void setSourceCode(String sourceCode) {
		this.sourceCode = sourceCode;
	}
}