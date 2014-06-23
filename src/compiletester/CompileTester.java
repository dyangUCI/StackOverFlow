package compiletester;


import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Locale;

import javax.tools.Diagnostic;
import javax.tools.DiagnosticCollector;
import javax.tools.JavaCompiler;
import javax.tools.JavaCompiler.CompilationTask;
import javax.tools.JavaFileObject;
import javax.tools.SimpleJavaFileObject;
import javax.tools.StandardJavaFileManager;
import javax.tools.ToolProvider;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import xmlparser.XmlWriter;

import org.apache.commons.lang3.StringEscapeUtils;

import runtester.RunTester;




/**
 * A test class to test dynamic compilation API.
 * 
 */
public class CompileTester {
	
	/**Java source code to be compiled dynamically*/
	ArrayList<DynamicJavaSourceCodeObject> unCompilable = new ArrayList<DynamicJavaSourceCodeObject>();//record file objects that do not compile
	ArrayList<DynamicJavaSourceCodeObject> compilable = new ArrayList<DynamicJavaSourceCodeObject>();//record the snippets that compile
	int count = 0;
	
	public void readXml(){
		
		File xmlFile = new File("/Users/Di/Desktop/postaj_snippets.xml");
	    //Get the DOM Builder Factory
	    DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
	    
	    //Get the DOM Builder
		try {
			  	DocumentBuilder builder = factory.newDocumentBuilder();
			  	//Load and Parse the XML document
			  	//document contains the complete XML as a Tree.
			  	Document document = builder.parse(xmlFile);

			    //Iterating through the nodes and extracting the data.
			    NodeList nodeList = document.getDocumentElement().getChildNodes();
			    
			    for (int i = 0; i < nodeList.getLength(); i++) {
			 
			      //We have encountered an <ROW> tag.
			      Node node = nodeList.item(i);
			      if (node instanceof Element) {
			        Snippet row = new Snippet();
			        NodeList childNodes = node.getChildNodes();
			        for (int j = 0; j < childNodes.getLength(); j++) {
			     
			          Node cNode = childNodes.item(j);
			          //Identifying the child tag of data encountered. 
			          if (cNode instanceof Element) {
			            String content = cNode.getLastChild().
			                getTextContent().trim();
			            switch (cNode.getNodeName()) {
			              case "id":
			                row.id = content;
			                break;
			              case "snippet":
			                row.snippet = content;	 
			                //clean the html notations
			                row.snippet = StringEscapeUtils.unescapeHtml3(row.snippet);
			                row.snippet = StringEscapeUtils.unescapeHtml4(row.snippet);
			                row.snippet = StringEscapeUtils.unescapeXml(row.snippet);
			                /*Creating dynamic java source code file object*/
				            count++;
				            SimpleJavaFileObject fileObject = new DynamicJavaSourceCodeObject (row.id, row.snippet);
				            doCompilation(fileObject);
			                break;
			            }	
			           
			          }
			         
			        }
			      }
			    }
			    
		} catch (ParserConfigurationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (SAXException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}     
	}
	
	/**
	 * Does the required object initialization and compilation.
	 */
	public void doCompilation (JavaFileObject javaFileObject){	
		
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
		Iterable<? extends JavaFileObject> compilationUnits = Arrays.asList(javaFileObject);
			
		/*Prepare any compilation options to be used during compilation*/
		//In this example, we are asking the compiler to place the output files under bin folder.
		String[] compileOptions = new String[]{"-d", "bin/compilable"} ;
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
			//	System.out.format("Error on line %d in %s", diagnostic.getLineNumber(), diagnostic);
				DynamicJavaSourceCodeObject source = (DynamicJavaSourceCodeObject)diagnostic.getSource();	
				if (!unCompilable.contains(source)){
					unCompilable.add(source);
				}			
			}
		}else{
			compilable.add(((DynamicJavaSourceCodeObject)javaFileObject));
		}
	
		try {
			stdFileManager.close() ;//Close the file manager
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public static void main(String args[]){
		CompileTester ct = new CompileTester();
		ct.readXml();
	
		XmlWriter xmlCompile = new XmlWriter();	
		for (DynamicJavaSourceCodeObject djsc : ct.compilable){
			//restore the html notations
			String source = StringEscapeUtils.escapeHtml3(djsc.getSourceCode());
            source = StringEscapeUtils.escapeHtml4(source);
            source = StringEscapeUtils.escapeXml10(source);
            source = StringEscapeUtils.escapeXml11(source);
			xmlCompile.addElement(djsc.getQualifiedName(), source);
		}
		xmlCompile.writeToXML("/Users/Di/Desktop/postaj_compilable.xml");
		
		XmlWriter xmlUncompile = new XmlWriter();
		for (DynamicJavaSourceCodeObject djsc : ct.unCompilable){
			//restore the html notations
			String source = StringEscapeUtils.escapeHtml3(djsc.getSourceCode());
            source = StringEscapeUtils.escapeHtml4(source);
            source = StringEscapeUtils.escapeXml10(source);
            source = StringEscapeUtils.escapeXml11(source);
			xmlUncompile.addElement(djsc.getQualifiedName(), source);
		}
		xmlUncompile.writeToXML("/Users/Di/Desktop/postaj_uncompilable.xml");
				
		System.out.println("Number of cleaned snippets: "+ ct.count);
		System.out.println("Number of Compilable Files: "+ ct.compilable.size());
		System.out.println("Number of Uncompilable Files: "+ ct.unCompilable.size());
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
		super(URI.create(name + Kind.SOURCE.extension), Kind.SOURCE);
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

class Snippet{
	  String id;
	  String snippet;

	  public Snippet(){
		  id = "";
		  snippet = "";
	  }
	  @Override
	  public String toString() {
	    return id + snippet;
	  }
	}