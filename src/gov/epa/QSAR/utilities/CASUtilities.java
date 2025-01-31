package gov.epa.QSAR.utilities;

public class CASUtilities {
	/**
     * Checks to see if have valid cas number with check sum method
     * 
      * @param CAS
     * @return
     */
     public static boolean isCAS_OK(String CAS) {
                    
                    if (CAS.indexOf(" ")>-1) {
//                               System.out.println("Space!");
                                  return false;
                    }
                    
                    String [] part=CAS.split("-");
                    
                    if (part.length!=3) return false;
                    
                    String part1=part[0];
                    String part2=part[1];
                    String part3=part[2];
                    
                    int sum=0;
                    
                    for (int i=0;i<part1.length();i++) {
                                  String s=part1.substring(i, i+1);
                                  if (!Character.isDigit(s.charAt(0))) return false;
                                  sum+=(part1.length()+2-i)*Integer.parseInt(s);
                    }
                    
                    String s1=part2.substring(0, 1);
                    String s2=part2.substring(1, 2);
                    
                    if (!Character.isDigit(s1.charAt(0)) || !Character.isDigit(s2.charAt(0))) {
                                  return false;
                    }
                    
                    int N2=Integer.parseInt(s1);
                    int N1=Integer.parseInt(s2);
                    int R=Integer.parseInt(part3);
                    
                    sum+=2*N2+N1;
                    
                    double bob=((double)sum)/10.0;
                    double bob2=Math.floor(bob);
                    double bob3=(bob-bob2)*10.0;
                    
                    int R2=(int)Math.round(bob3);
                    
//                 System.out.println(bob3);
//                 System.out.println(R+"\t"+R2);
                    
                    return R2==R;
                    
                    
     }
     
     public static String fixIntegerCAS(int icas) {
    	 
    	String CAS1=Integer.toString(icas).substring(0,Integer.toString(icas).length()-3);
 		String CAS2=Integer.toString(icas).substring(Integer.toString(icas).length()-3,Integer.toString(icas).length()-1);
 		String CAS3=Integer.toString(icas).substring(Integer.toString(icas).length()-1,Integer.toString(icas).length());
 		String CAS=CAS1+"-"+CAS2+"-"+CAS3;
 		
 		return CAS;
     }
     
     public static String fixDateCAS(String dateCAS) {
    	 if(dateCAS.contains("/")) {
    		 dateCAS=dateCAS.replace("/", "-");
    	 }
    	 
    	 if(!isCAS_OK(dateCAS)) {
    		 System.out.println("Invalid CAS (DATE): " + dateCAS);
    	 }
    	 
    	 return dateCAS;
     }
}
