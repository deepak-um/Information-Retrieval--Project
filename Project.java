import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;
import java.util.Properties;
import java.util.regex.*;


import org.apache.lucene.analysis.core.WhitespaceAnalyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.StringField;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopScoreDocCollector;
import org.apache.lucene.search.similarities.BM25Similarity;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.store.RAMDirectory;
import org.apache.lucene.util.Version;
/*
import edu.stanford.nlp.ling.CoreAnnotations;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;
import edu.stanford.nlp.util.CoreMap; */


public class Project 
{
	public static void main(String[] args)
	{
		try
		{
			
			
			double numOfdocs=0;
			double mrr_value=0;
			
			//int typeOfanalyzer=Integer.parseInt(args[0]);
			
			int typeOfanalyzer=Integer.parseInt(args[1]);
			
			int typeOfscoring=Integer.parseInt(args[2]);
			
			int typeOfmethod=Integer.parseInt(args[3]);
			
			float k1=0,b1=0;
			
			if(typeOfscoring==1)
			{	
			 k1=Float.parseFloat(args[5]);
			 b1=Float.parseFloat(args[6]);
			}
			
			
			
			
			StandardAnalyzer standard_analyzer= new StandardAnalyzer(Version.LUCENE_40);
		    WhitespaceAnalyzer whitespace_analyzer=new WhitespaceAnalyzer(Version.LUCENE_40);
			//String text="";
			//String lemma="";
				
			//	Code to create the index
			//Directory index = new RAMDirectory();
		    IndexWriterConfig config=null;
		    
		    if(typeOfanalyzer==0)
		    	config = new IndexWriterConfig(Version.LUCENE_40, whitespace_analyzer);
		    else
		    	config = new IndexWriterConfig(Version.LUCENE_40, standard_analyzer);
		    
			
			if(typeOfscoring==1)
				config.setSimilarity(new BM25Similarity(k1,b1)); 
			
			
			
		/*	Properties props = new Properties();
		    props.setProperty("annotators", "tokenize, ssplit, pos, lemma, ner");
		    StanfordCoreNLP pipeline = new StanfordCoreNLP(props); */
			
			
			Directory index = FSDirectory.open(new File("disk-directory"));
			
			if(!DirectoryReader.indexExists(index))
			{
			
			
			
			
			IndexWriter w = new IndexWriter(index, config);
			
			
		    Pattern p_Title = Pattern.compile("\\[\\[.*\\]\\]");
		    
		    Pattern p_References = Pattern.compile(".*=References=.*");
		    
		    Pattern p_ExternalLinks = Pattern.compile(".*=External links=.*");
		    
		    Pattern p_FurtherReading = Pattern.compile(".*=Further reading=.*");
		    
		    Pattern p_SeeAlso = Pattern.compile(".*=See also=.*");
		    
		   Pattern p_Categories=Pattern.compile("CATEGORIES.*");
		    
		    Pattern p_GeneralDoubleEqual = Pattern.compile("=.*=");
		    
		    
		    File dir = new File(args[0]);
		    
		  for(String fn: dir.list())  
		  {   File file=new File("./"+dir+"/"+fn);
			//File file_out=new File(args[1]);
			//FileWriter fileWriter=new FileWriter(file_out);
			FileReader reader1=new FileReader(file);
			
			//char all[]=new char[(int)file.length()];
			BufferedReader bufferedReader=new BufferedReader(reader1);
			//BufferedWriter bw=new BufferedWriter(fileWriter);
			//reader1.read(all);
			String line;
		    
		    String docTitle="";
		    int first_line_of_page=0;
		    int reference_checker=0;
		    int externalLinks_checker=0;
		    int furtherReading_checker=0;
		    int seeAlso_checker=0;
		    
		    StringBuilder docContents=new StringBuilder();
		    //StringBuilder lemmatized_string=new StringBuilder();
			
			while((line=bufferedReader.readLine())!=null)
			{
				Matcher m_Title = p_Title.matcher(line);
				boolean b_Title = m_Title.matches();
				
				
				
				if(b_Title)
				{
					reference_checker=0;
					externalLinks_checker=0;
					furtherReading_checker=0;
					seeAlso_checker=0;
				
					
					if(first_line_of_page==1)
					{
						
					/*	text=docContents.toString();
						text = text.replaceAll("[-+.^:,!]","");
						Annotation document = new Annotation(text);
						pipeline.annotate(document);
					    List<CoreMap> sentences = document.get(CoreAnnotations.SentencesAnnotation.class);
					    
					    for(CoreMap sentence: sentences) {
					    	for (CoreLabel token : sentence.get(CoreAnnotations.TokensAnnotation.class)) {
						           lemma = token.get(CoreAnnotations.LemmaAnnotation.class);
						           lemmatized_string.append(lemma);
						           lemmatized_string.append(" ");
						        }
					    } */
						
						
						addDoc(w,docContents.toString(),docTitle);
						//addDoc(w,lemmatized_string.toString().trim(),docTitle);
							
						
					}
					docTitle=line.substring(line.indexOf("[")+2,line.indexOf("]"));
					docContents.setLength(0);
					//lemmatized_string.setLength(0);
					
					
					first_line_of_page=1;
				}
				else
				{
					Matcher m_References = p_References.matcher(line);
					boolean b_References = m_References.matches();
					Matcher m_ExternalLinks = p_ExternalLinks.matcher(line);
					boolean b_ExternalLinks = m_ExternalLinks.matches();
					Matcher m_FutherReading = p_FurtherReading.matcher(line);
					boolean b_FurtherReading = m_FutherReading.matches();
					Matcher m_Seealso = p_SeeAlso.matcher(line);
					boolean b_SeeAlso = m_Seealso.matches();
					Matcher m_GeneralDoubleEqual = p_GeneralDoubleEqual.matcher(line);
					boolean b_GeneralDoubleEqual = m_GeneralDoubleEqual.matches();
					Matcher m_Categories=p_Categories.matcher(line);
					boolean b_Categories=m_Categories.matches();
					
					
					
					if(b_References)
					{	reference_checker=1;
						continue;
					}
					if(b_ExternalLinks)
					{	externalLinks_checker=1;
						continue;
					}
					if(b_FurtherReading)
					{	furtherReading_checker=1;
						continue;
					}
					if(b_SeeAlso)
					{	seeAlso_checker=1;
						continue;
					}
					
					else if(b_GeneralDoubleEqual)
					{
						
						reference_checker=0;
						externalLinks_checker=0;
						furtherReading_checker=0;
						seeAlso_checker=0;
					    continue;
					}
					else
					{
						if(reference_checker==0 && externalLinks_checker==0 && furtherReading_checker==0 && seeAlso_checker==0)
						{
							    //line = line.replaceAll("!'();[-+.^:,]","");
								line=line.replaceAll("()[-+.^:,'!]","");
							    docContents.append(line);
							    docContents.append("\n");
						}
						else
							continue;
					}
				}
			}
			
		/*	text=docContents.toString();
			text = text.replaceAll("[-+.^:,!]","");
			Annotation document = new Annotation(text);
			pipeline.annotate(document);
		    List<CoreMap> sentences = document.get(CoreAnnotations.SentencesAnnotation.class);
		    
		    for(CoreMap sentence: sentences) {
		    	for (CoreLabel token : sentence.get(CoreAnnotations.TokensAnnotation.class)) {
			           lemma = token.get(CoreAnnotations.LemmaAnnotation.class);
			          
			        }
		    } 
			
			
			addDoc(w,lemmatized_string.toString().trim(),docTitle); */
			 
			addDoc(w,docContents.toString(),docTitle);
			
		  }
			
			w.close();
			}
			
			File questions_file=new File(args[4]);

			if(args[4].matches("training_set.txt"))
				numOfdocs=20;
			else
				numOfdocs=80;
			
			FileReader questions_reader=null;
			
			questions_reader=new FileReader(questions_file);
			
			BufferedReader questions_bufferedReader=new BufferedReader(questions_reader);
			
			String questions_line;
			
			StringBuilder category_clue_answer=new StringBuilder();
			
			int clue_flag=0;
			
			int questions_first_iteration=0;
			
			double count=1;
			double precisionAt1_count=0;
			
			
			
			while((questions_line=questions_bufferedReader.readLine())!=null)
		{
				String category="";
				String clue="";
				String answer="";
			
			if(questions_line.equals(""))
			{
				if(questions_first_iteration==1)
				{
				String[] toSplitCategoryClueAnswer=category_clue_answer.toString().split("\n");
			  
				category=toSplitCategoryClueAnswer[0];
				clue=toSplitCategoryClueAnswer[1];
				clue_flag=1;
				answer=toSplitCategoryClueAnswer[2];
				category_clue_answer.setLength(0);
				}
				else
					continue;
			
				
			}
			else
			{
				
				
				category_clue_answer.append(questions_line);
				category_clue_answer.append("\n");
				clue_flag=0;
				questions_first_iteration=1;
			}
			
		if(clue_flag==1)
		{	
			StringBuilder new_clue=new StringBuilder();
			String querystr="";
			
			new_clue.append(category);
			new_clue.append(" ");
			new_clue.append(clue);  
			
			
			if(typeOfmethod==0)
				querystr = clue.replaceAll("()[-+.^:,'!]","");
			else
				querystr = new_clue.toString().replaceAll("()[-+.^:,'!]","");
		 /*   StringBuilder query_lemma=new StringBuilder();
			String lemma_1="";
			Properties props_1 = new Properties();
		    props_1.setProperty("annotators", "tokenize, ssplit, pos, lemma, ner");
		    StanfordCoreNLP pipeline_1 = new StanfordCoreNLP(props_1);
			//String querystr=clue;
		/*    String text_1=querystr;
		    
		    
		    Annotation document_1 = new Annotation(text_1);
		    pipeline.annotate(document_1);
		    List<CoreMap> sentences_1 = document_1.get(CoreAnnotations.SentencesAnnotation.class);
		    for(CoreMap sentence_1: sentences_1) {
		    	for (CoreLabel token : sentence_1.get(CoreAnnotations.TokensAnnotation.class)) {
			           lemma_1 = token.get(CoreAnnotations.LemmaAnnotation.class);
			           query_lemma.append(lemma_1);
					    query_lemma.append(" ");
			          
			        }
		    } */
			
			//	The \"title\" arg specifies the default field to use when no field is explicitly specified in the query
			Query q=null;
			
			if(typeOfanalyzer==0)
				q = new QueryParser(Version.LUCENE_40, "text",whitespace_analyzer).parse(querystr);
			else
				q = new QueryParser(Version.LUCENE_40, "text",standard_analyzer).parse(querystr); 
		    
		  // Query q = new QueryParser(Version.LUCENE_40, "text", analyzer).parse(query_lemma.toString().trim());
			
			// Searching code
			int hitsPerPage = 10;
		    IndexReader reader = DirectoryReader.open(index);
		    IndexSearcher searcher = new IndexSearcher(reader);
		    
		   if(typeOfscoring==1)
			   searcher.setSimilarity(new BM25Similarity(k1,b1)); 
		    
		    TopScoreDocCollector collector = TopScoreDocCollector.create(hitsPerPage, true);
		    searcher.search(q, collector);
		    ScoreDoc[] hits = collector.topDocs().scoreDocs;
		    
		    //	Code to display the results of search
		    //System.out.println("Found " + hits.length + " hits.");
		    
		    
		    
		    int hitsFoundinTop10=0;
		    int hitFoundatPosition=0;
		    answer=answer.replaceAll("'();[-+.^:,\"!]","");
		    for(int k=0;k<hits.length;k++)
		    {
		    	int docId_1 = hits[k].doc;
		    	Document d_1=searcher.doc(docId_1);
		    	if(d_1.get("docname").matches(answer))
		    	{
		    		
		    		if(k==0)
		    			precisionAt1_count++;
		    		
		    		hitsFoundinTop10=1;
		    		hitFoundatPosition=k+1;
		    		
		    		mrr_value+=(1.0/(float)hitFoundatPosition);
		    		
		    		
		    		break;
		    	}
		    }
		    
		    
		   
		    
		    
		    if(hitsFoundinTop10==1)
  {	    
		    	
		    
		    	
		    	System.out.println("the number is\t" + count++ + "\n");
		    	System.out.println("Found " + hits.length + " hits.");
			    System.out.println("\n");
			    System.out.println("hit is at position\t" + hitFoundatPosition);
			    System.out.println("\n"); 
		    	
		    for(int i=0;i<hits.length;++i) 
		    {
		      int docId = hits[i].doc;
		      
		      Document d = searcher.doc(docId);
		     
		     
		     System.out.println((i + 1) + ". " + d.get("docname")+"\tscore\t"+hits[i].score );
		     System.out.println("\n"); 
		    }
		   
		    System.out.println("\n");
  }
		    
		    
		    // reader can only be closed when there is no need to access the documents any more
		    reader.close();
		    
		    
		}
		
		    
		}
		
			
			System.out.println("the precision at one is\t" + (precisionAt1_count/numOfdocs));
			System.out.println("\n");
			count--;
			System.out.println("the precision at ten is\t" + (count/numOfdocs));
			System.out.println("\n");
			System.out.println("mrr value is\t" + (mrr_value/numOfdocs)); 
			
			
			//bw.close();
		
		}
		catch(Exception e)
		{
			System.out.println(e.getMessage());
		}
	}
	private static void addDoc(IndexWriter w, String text, String docname) throws IOException 
	{
		  Document doc = new Document();
		  // A text field will be tokenized
		  doc.add(new TextField("text", text, Field.Store.YES));
		  // We use a string field for isbn because we don\'t want it tokenized
		  doc.add(new StringField("docname", docname, Field.Store.YES));
		  w.addDocument(doc);
	}
}