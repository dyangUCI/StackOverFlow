package asttester;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.commons.lang3.StringEscapeUtils;
import org.eclipse.jdt.core.compiler.IProblem;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import xmlparser.XmlWriter;


public class AstTester {

	ArrayList<Snippet> parsable = new ArrayList<Snippet>();//record the snippets that parse
	ArrayList<Snippet> unparsable = new ArrayList<Snippet>();
	int count = 0;
	
	public boolean hasProblems(String source){
		ASTParser parser = ASTParser.newParser(AST.JLS3);
		parser.setSource(source.toCharArray());
		parser.setKind(ASTParser.K_COMPILATION_UNIT);
		final CompilationUnit cu = (CompilationUnit) parser.createAST(null);
		parser.setKind(ASTParser.K_COMPILATION_UNIT);
		
		IProblem[] problems = cu.getProblems();
//		for (int i = 0; i < problems.length; i++) {
//			System.out.println(problems[i].getMessage());
//		}
		if(problems.length != 0){
			return true;
		}else{
			return false;
		}	
	}
	
	public void readXml(){	
		File xmlFile = new File("/Users/Di/Desktop/postaj_uncompilable.xml");
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
				            if(!hasProblems(row.snippet)){
				            	parsable.add(row);
				            }else{
				            	unparsable.add(row);
				            }
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
	
	public static void main(String args[]){
		AstTester at = new AstTester();
		at.readXml();
	
		XmlWriter xmlParse = new XmlWriter();	
		for (Snippet sn : at.parsable){
			//restore the html notations
			String source = StringEscapeUtils.escapeHtml3(sn.snippet);
            source = StringEscapeUtils.escapeHtml4(source);
            source = StringEscapeUtils.escapeXml10(source);
            source = StringEscapeUtils.escapeXml11(source);
			xmlParse.addElement(sn.id, sn.snippet);
		}
		xmlParse.writeToXML("/Users/Di/Desktop/postaj_parsable.xml");
		
		XmlWriter xmlUncompile = new XmlWriter();
		for (Snippet sn : at.unparsable){
			//restore the html notations
			String source = StringEscapeUtils.escapeHtml3(sn.snippet);
            source = StringEscapeUtils.escapeHtml4(source);
            source = StringEscapeUtils.escapeXml10(source);
            source = StringEscapeUtils.escapeXml11(source);
			xmlUncompile.addElement(sn.id, sn.snippet);
		}
		xmlUncompile.writeToXML("/Users/Di/Desktop/postaj_unparsable.xml");
				
		System.out.println("Number of uncompilable snippets: "+ at.count);
		System.out.println("Number of Parsale Files: "+ at.parsable.size());
		System.out.println("Number of Unparsable Files: "+ at.unparsable.size());
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
