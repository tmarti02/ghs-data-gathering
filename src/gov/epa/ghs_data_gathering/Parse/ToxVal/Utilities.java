package gov.epa.ghs_data_gathering.Parse.ToxVal;

import java.util.*;
import java.util.List;
import java.nio.channels.*;
import java.io.*;
import javax.swing.*;
import java.awt.*;

import javax.swing.border.*;

import java.util.zip.*;

public class Utilities {

  public static Cursor waitCursor = Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR);
  public static Cursor defaultCursor = Cursor.getPredefinedCursor(Cursor.
      DEFAULT_CURSOR);
  
	public static ArrayList<String> readFileToArray (String filepath) {
		
		try {
			
			ArrayList<String>lines=new ArrayList<String>();
			
			BufferedReader br=new BufferedReader(new FileReader(filepath));
			
			while (true) {
				String Line=br.readLine();
				if (Line==null) break;
				lines.add(Line);
				
			}
			
			br.close();
			
			return lines;
			
			
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		
		return null;
		
	}


	
  
  public static LinkedList<String> Parse(String Line, String Delimiter) {
    // parses a delimited string into a list

    LinkedList<String> myList = new LinkedList<String>();

    int tabpos = 1;

    while (tabpos > -1) {
      tabpos = Line.indexOf(Delimiter);

      if (tabpos > 0) {
        myList.add(Line.substring(0, tabpos));
        Line = Line.substring(tabpos + Delimiter.length(), Line.length());
      } else if (tabpos == 0) {
        myList.add("");
        Line = Line.substring(tabpos + Delimiter.length(), Line.length());
      } else {
        myList.add(Line.trim());
      }
    }

    return myList;

  }
  
  
  
  
	public static LinkedList<String> ParseWithTokenizer(String Line,
			String delimiter) {
		LinkedList<String> list = new LinkedList<String>();
		StringTokenizer st = new StringTokenizer(Line, delimiter);
		while (st.hasMoreTokens()) {
			list.add(st.nextToken());
		}
		return list;
	}
	
	
	
	public static LinkedList<Double> ParseToDoubleLinkedListWithTokenizer(String Line,
			String delimiter) {
		LinkedList<Double> list = new LinkedList<Double>();
		StringTokenizer st = new StringTokenizer(Line, delimiter);
		while (st.hasMoreTokens()) {
			String strVal=st.nextToken();
			list.add(new Double(strVal));
		}
		return list;
	}
	
	public static ArrayList<Double> ParseToDoubleArrayListWithTokenizer(String Line,
			String delimiter) {
		ArrayList<Double> list = new ArrayList<Double>();
		StringTokenizer st = new StringTokenizer(Line, delimiter);
		while (st.hasMoreTokens()) {
			String strVal=st.nextToken();
			list.add(new Double(strVal));
		}
		return list;
	}
	

	public static ArrayList<String> ParseToStringArrayListWithTokenizer(String Line,
			String delimiter) {
		ArrayList<String> list = new ArrayList<String>();
		StringTokenizer st = new StringTokenizer(Line, delimiter);
		while (st.hasMoreTokens()) {
			String strVal=st.nextToken();
			list.add(strVal);
		}
		return list;
	}

  public static List<String> Parse2(String Line, String Delimiter) {
    // parses a delimited string into a list (case where have one or spaces for delimiter)

    Line = Line.trim();

    java.util.List<String> myList = new ArrayList<String>();

    int tabpos = 1;

    while (tabpos > -1) {
      tabpos = Line.indexOf(Delimiter);

      if (tabpos > 0) {
        myList.add(Line.substring(0, tabpos));
        Line = Line.substring(tabpos + 1, Line.length());
        Line = Line.trim();
      } else if (tabpos == 0) {
        myList.add("");
        Line = Line.substring(tabpos + 1, Line.length());
        Line = Line.trim();
      } else {
        myList.add(Line.trim());
      }
    }

    return myList;

  }

  /** parses a delimited string into a list- accounts for the fact that can have quotation marks in comma delimited lines
   * 
   * @param Line - line to be parsed
   * @param Delimiter - character used to separate line into fields
   * @return
   */
  public static LinkedList<String> Parse3(String Line, String Delimiter) {

	    LinkedList<String> myList = new LinkedList<String>();

	    int tabpos = 1;

	    while (tabpos > -1) {

	    	tabpos = Line.indexOf(Delimiter);
	    	
	    	if (Line.length()<1) break;
	    	
	    	if (Line.substring(0,1).equals("\"")) {
	    		Line=Line.substring(1,Line.length()); // kill first " mark
	    		
	    		if (Line.length()==0) break;
	    		
	    		myList.add(Line.substring(0, Line.indexOf("\"")));
	    		
	    		if (Line.indexOf("\"")<Line.length()-1)
	    			Line = Line.substring(Line.indexOf("\"") + 2, Line.length());
	    		else 
	    			break;
	    	} else {
				

				if (tabpos > 0) {
					myList.add(Line.substring(0, tabpos));
					Line = Line.substring(tabpos + 1, Line.length());
				} else if (tabpos == 0) {
					myList.add("");
					Line = Line.substring(tabpos + 1, Line.length());
				} else {
					myList.add(Line.trim());
				}

	    	}
	    			
		}// end while loop

	    
//	    for (int j = 0; j <= myList.size() - 1; j++) {
//			System.out.println(j + "\t" + myList.get(j));					
//		}
	    
	    return myList;

	  }
  
  /** parses a delimited string into a list- accounts for the fact that can have quotation marks in comma delimited lines
   * 
   * @param Line - line to be parsed
   * @param Delimiter - character used to separate line into fields
   * @return
   */
  public static java.util.ArrayList<String> Parse3toArrayList(String Line, String Delimiter) {


	    java.util.ArrayList<String> myList = new ArrayList<String>();

	    int tabpos = 1;

	    while (tabpos > -1) {

	    	tabpos = Line.indexOf(Delimiter);
	    	
	    	if (Line.length()<1) break;
	    	
	    	if (Line.substring(0,1).equals("\"")) {
	    		Line=Line.substring(1,Line.length()); // kill first " mark
	    		
	    		if (Line.length()==0) break;
	    		
	    		myList.add(Line.substring(0, Line.indexOf("\"")));
	    		
	    		if (Line.indexOf("\"")<Line.length()-1)
	    			Line = Line.substring(Line.indexOf("\"") + 2, Line.length());
	    		else 
	    			break;
	    	} else {
				

				if (tabpos > 0) {
					myList.add(Line.substring(0, tabpos));
					Line = Line.substring(tabpos + 1, Line.length());
				} else if (tabpos == 0) {
					myList.add("");
					Line = Line.substring(tabpos + 1, Line.length());
				} else {
					myList.add(Line.trim());
				}

	    	}
	    			
		}// end while loop

	    
//	    for (int j = 0; j <= myList.size() - 1; j++) {
//			System.out.println(j + "\t" + myList.get(j));					
//		}
	    
	    return myList;

	  }
  /**
   * Finds all indices of a given character within a string
   * @param Line
   * @param s
   * @return
   */
  static java.util.ArrayList<Integer>GetIndices(String Line,String s) {
	  
	  java.util.ArrayList<Integer>l=new java.util.ArrayList<Integer>();
	  
	  for (int i=0;i<Line.length();i++) {
		  String c=Line.substring(i,i+1);
		  
//		  System.out.println(c);
		  if (c.equals(s)) 
			  l.add(new Integer(i));
		  
	  }
	  
	  
	  return l;
	  
	  
  }
  

  
  public static String RetrieveField(String Line, String Delimiter, int FieldNum) {
    // retrieves a field from a delimited string
    int tabpos;

    for (int i = 0; i <= FieldNum; i++) {
      tabpos = Line.indexOf(Delimiter);

      if (i == FieldNum && tabpos == -1) {
        return Line;
      }

      if (tabpos > -1) {
        if (i < FieldNum) {
          Line = Line.substring(tabpos + 1, Line.length());
        } else {
          return Line.substring(0, tabpos);
        }
      }
    }

    return "";

  }

  public static int GetColumnNumber(String var, java.util.List<String> l) {
		
		for (int i=0;i<=l.size()-1;i++) {
			String s=(String)l.get(i);
			if (s.equals(var)) return i;			
		}
		
		return -1;
	}
  
  public static int CopyFile(File SrcFile, File DestFile) {

    try {

      FileChannel in = new FileInputStream(SrcFile).getChannel();

      FileChannel out = new FileOutputStream(DestFile).getChannel();

      in.transferTo(0, (int) in.size(), out);
      in.close();
      out.close();

      return 0;

    } catch (Exception e) {
    	e.printStackTrace();
    	return -1;
    }

  }

  public static int FindFieldNumber(String Line, String field) {
    java.util.List<String> myList = Utilities.Parse(Line, "\t");

    for (int i = 0; i <= myList.size() - 1; i++) {
    	if (field.equals(myList.get(i))) {
        return i;
      }
    }

    return -1;

  }
  
  public static int FindFieldNumber(String Line, String field,String del) {
	    java.util.List<String> myList = Utilities.Parse3(Line, del);

	    for (int i = 0; i <= myList.size() - 1; i++) {
	      if (field.equals( (String) myList.get(i))) {
	        return i;
	      }
	    }

	    return -1;

	  }
  

	public static void CenterFrame(java.awt.Window w) {
		Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
		Dimension size = w.getSize();

		int y = screenSize.height/2 - size.height/2;
		int x = screenSize.width/2 - size.width/2;
		
		w.setLocation(x, y);

	}

  
  public static void SetFonts(Container myContainer) {

    int i = 0;
    Component[] myComponents = null;

    try {
      Font myFont = new java.awt.Font("Arial", 0, 11);
      Font myFontBold = new java.awt.Font("Arial", 1, 11);

      myComponents = myContainer.getComponents();

      for (i = 0; i <= myComponents.length - 1; i++) {

        String Name = myComponents[i].getClass().getName();
//          System.out.println(Name);

        if (myComponents[i] instanceof JPanel) {
          JPanel jp = (JPanel) myComponents[i];
          if (jp.getBorder() instanceof TitledBorder) {
            TitledBorder tb = (TitledBorder) jp.getBorder();
            tb.setTitleFont(myFont);
          }
        }

        Container myContainer2 = (Container) myComponents[i];

        if (myContainer2.getComponentCount() > 1) {
          // call recursively:
          myComponents[i].setFont(myFont);
          SetFonts( (Container) myComponents[i]);

        } else {

          if (myComponents[i].getName() instanceof String) {
            if (myComponents[i].getName().indexOf("Title") > -1) {
              myComponents[i].setFont(myFontBold);
            } else {
              myComponents[i].setFont(myFont);
            }
          } else {
            myComponents[i].setFont(myFont);
          }

        }
      }

    } catch (Exception e) {
      System.out.println(e + "\n, index = " + i + ", myComponent =" +
                         myComponents[i].getName() + ", class=" +
                         myComponents[i].getClass().getName());
    }

  }

  static double Log10(double value) {
    double log10value = 0;
    try {
      log10value = Math.log(value) / Math.log(10);
    } catch (Exception e) {

    }
    return log10value;
  }

  public static java.util.List <String>RetrieveFileList(String zipfilename) {
    // this method retrieves the list of files in a zip file (assumes no nesting)

    java.util.List<String> myFileList = new java.util.ArrayList<String>();

    ZipInputStream zipin1;
    String datfilename = "******";

    try {
      FileInputStream in = new FileInputStream(zipfilename);
      zipin1 = new ZipInputStream(in);

      while (true) {
        ZipEntry first = zipin1.getNextEntry();
        if (first instanceof ZipEntry) {
          datfilename = first.getName();
          myFileList.add(datfilename);
        } else break;

      }

    } catch (IOException e) {
      System.out.println("RetrieveFileList, error = " + e);
    }

    return myFileList;
  }


 public static java.util.List<String> RetrieveFilesWithString(java.util.List<String> l,
                                                String matchstring,
                                                int StartIndex) {

// this method retrieves all the strings in a list that match a given string
// the start index determines where to start looking for the match string

    java.util.List<String> myFileList = new java.util.ArrayList<String>();

    for (int i = 0; i <= l.size() - 1; i++) {
      String file = (String) l.get(i);
      if (file.indexOf(matchstring) == StartIndex) myFileList.add(file);
    }

    return myFileList;
  }
 
 public static void main(String[] args) {
//	 String Line="\"581-12-4\",Chemidplus3DMolFile,350,\"2-Pentanone, 1-(5-(3-furyl)-2-methyl-tetrahydro-2-furyl)-4-methyl-,\"";

	 
	 String Line="581-12-4,\"Chemidpl,,,,us3DMolFile\",350,\"2-Pentanone, 1-(5-(3-furyl)-2-methyl-tetrahydro-2-furyl)-4-methyl-,\",1234";
	 
	 java.util.List<String>l=Utilities.Parse3(Line, ",");
	 
	 System.out.println(Line+"\n");
//	 
	 for (int i=0;i<l.size();i++) {
		 System.out.println(i+"\t"+l.get(i));
	 }
	 
	 
	 
 }
 
 

}
