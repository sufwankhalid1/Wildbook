package org.ecocean;

import java.util.Comparator;
import org.ecocean.*;
import java.util.Iterator;
import java.util.StringTokenizer;

public class IndividualKeywordComparator implements Comparator{
    
  Shepherd myShepherd;
  Iterator keywords;
  
  public IndividualKeywordComparator(Shepherd shep, Iterator keywords) {
    this.myShepherd=shep;
    this.keywords=keywords;
  }
    
    public int compare(Object a, Object b) {
      
      //System.out.println("\n\nStarting new comparison!");
      

      
      MarkedIndividual individualA=(MarkedIndividual)a;
      MarkedIndividual individualB=(MarkedIndividual)b;
      
      String aKeyword="ZZZZZZZZZZ";
      String bKeyword="ZZZZZZZZZZ";

      
      // = myShepherd.getAllKeywords();
      //System.out.println("Starting to iterate keywords");
      while(keywords.hasNext()){
        Keyword keyword=(Keyword)keywords.next();
        String readableName=keyword.getReadableName();
        //System.out.println("     Looking at keyword: "+readableName);
        
        //a keyword
        if(keyword.isMemberOf(individualA)){
         // System.out.println("          Image a is a member of this keyword!");
          if(aKeyword.equals("ZZZZZZZZZZ")){aKeyword=readableName;}
          else{if(aKeyword.compareTo(readableName)>0){aKeyword=readableName;}}
          //System.out.println("          aKeyword is now: "+aKeyword);
        }
        
        //b keyword
        if(keyword.isMemberOf(individualB)){
          //System.out.println("          Image b is a member of this keyword!");
          if(bKeyword.equals("ZZZZZZZZZZ")){bKeyword=readableName;}
          else{if(bKeyword.compareTo(readableName)>0){bKeyword=readableName;}}
          //System.out.println("          bKeyword is now: "+bKeyword);
        }
        
        
      }
      

      //System.out.println("aKeyword is now: "+aKeyword);
      //System.out.println("bKeyword is now: "+bKeyword);
      //System.out.println("Attempting a comparison of a to b: "+aKeyword.compareTo(bKeyword));
      
      
      //if they're equal then sort by name
      if(aKeyword.compareTo(bKeyword)==0){
        return individualA.getName().compareTo(individualB.getName());
      }
      
      //otherwise, just return their different values
      return aKeyword.compareTo(bKeyword);
    }
    
    
    
}