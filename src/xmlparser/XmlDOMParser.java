package xmlparser;
import java.io.File;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

// To extract the code snippets from java_body.xml

public class XmlDOMParser {

  static int count = 0; // num of snippets

  public static void main(String[] args) throws Exception {
	
	File xmlFile = new File("/Users/Di/Desktop/postat_body.xml");
    //Get the DOM Builder Factory
    DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();

    //Get the DOM Builder
    DocumentBuilder builder = factory.newDocumentBuilder();

    //Load and Parse the XML document
    //document contains the complete XML as a Tree.
    Document document = builder.parse(xmlFile);

    //Iterating through the nodes and extracting the data.
    NodeList nodeList = document.getDocumentElement().getChildNodes();
    
    XmlWriter xmlwriter = new XmlWriter(); // prepare to write snippets into xml file
    
    for (int i = 0; i < nodeList.getLength(); i++) {
 
      //We have encountered an <ROW> tag.
      Node node = nodeList.item(i);
      if (node instanceof Element) {
        Data row = new Data();
        NodeList childNodes = node.getChildNodes();
         
        for (int j = 0; j < childNodes.getLength(); j++) {
     
          Node cNode = childNodes.item(j);
          //Identifying the child tag of data encountered. 
          if (cNode instanceof Element) {
            String content = cNode.getLastChild().
                getTextContent().trim();
            switch (cNode.getNodeName()) {
              case "Id":
                row.id = content;
                break;
              case "Body":
                row.body = content;
                String find = "(<code>).*?(</code>)";// pay attention to the use of "?", without ?, we'll get the longest matched string
                Pattern pattern = Pattern.compile(find);
                Matcher matcher = pattern.matcher(content);
                while(matcher.find()){  
                	String rawSnippet = matcher.group();
                	String snippet = rawSnippet.substring(6, rawSnippet.length()-7);
                    row.snippets.add(snippet);               
                }  
                if(!row.snippets.isEmpty()){
                	for (String s : row.snippets){
                		xmlwriter.addElement(String.valueOf(row.id), s);
                		count++;
                	}
                }               	
                break;
            }
          }
        }
      }
     
    }
    
    xmlwriter.writeToXML("/Users/Di/Desktop/postat_snippets.xml");   
    System.out.println("number of cleaned snippets: " + count);

  }
}

class Data{
  String id;
  String body;
  ArrayList<String> snippets;

  public Data(){
	  id = "";
	  body = "";
	  snippets = new ArrayList<String>();
  }
  @Override
  public String toString() {
    return id + snippets;
  }
}