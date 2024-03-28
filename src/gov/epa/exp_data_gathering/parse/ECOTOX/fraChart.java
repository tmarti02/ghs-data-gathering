package gov.epa.exp_data_gathering.parse.ECOTOX;
import javax.swing.*;

import org.apache.commons.io.IOUtils;



import java.awt.*;
import java.awt.image.*;

import javax.imageio.*; // needed to write image to file

import java.io.*; // needed for file readers/writers
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.awt.event.*; // needed for mousemotionadapter
import java.text.*; // needed for DecimalFormat
import java.util.Base64;
import java.awt.font.*;


/**
 * 
 * @author TMARTI02
 *
 */
public class fraChart extends JFrame {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 6751103519589645460L;
	
	public JLabelChart jlChart = new JLabelChart();
	
//	JLabel jlPosition = new JLabel();
	
	JLabel jlResults=new JLabel();
	
	JButton jbOK = new JButton();
	
	//double [] Y2={0,1.253,2.260,3.266,4.272,5.278};
	
	
	public fraChart() {
		init();
	}
	
	public fraChart(double []X,double []Y1) {
		jlChart = new JLabelChart(X, Y1);
		init();
	}

	public fraChart(double []X,double []Y1,String charttitle,String xtitle,String ytitle) {
		jlChart = new JLabelChart(X, Y1, charttitle, xtitle, ytitle);
		init();
	}

	
	public fraChart(double []X,double []Y1,String xtitle,String ytitle) {
		jlChart = new JLabelChart(X, Y1, xtitle, ytitle);
		init();
	}
	
	public fraChart(double []X,double []Y1,double []SC,String charttitle,String xtitle,String ytitle) {
		jlChart = new JLabelChart(X, Y1, SC, charttitle, xtitle, ytitle);
		init();
	}

	public fraChart(double []X,double []Y1,double []e) {
		jlChart = new JLabelChart(X, Y1, e);
		init();
	}
	
	private void init() {
		jlChart.jlResults = jlResults;
		try {
			jbInit();
			myInit();
		} catch ( Exception ex ) {
			System.out.println("fraMapSources/Init, error = "+ex);
		}
	}
	
	public void WriteImageToFile(String filename,String OutputFolder) {
		this.WriteImageToFile(OutputFolder+"/"+filename);
	}
	
	
	
	public void WriteImageToFile(String filepath) {
		
		try {

			int w=jlChart.getWidth();
			
			BufferedImage ImgSrc = new BufferedImage(w, w,
					BufferedImage.TYPE_INT_RGB);
			Graphics2D g2 = (Graphics2D) ImgSrc.getGraphics();

			this.jlChart.DrawChart(g2);

			File myFile = new File(filepath);

			ImageIO.write(ImgSrc, "png", myFile);
			
		} catch (Exception e) {
			System.out.println("Exception creating pic file");
			e.printStackTrace();
		}
	}
	
	void jbInit() throws Exception {
		
		int size=400;
		jlChart.setBorder( BorderFactory.createLineBorder( Color.black ) );
		jlChart.setBounds( new Rectangle( 25, 25, size, size ) );
		jlChart.setVisible(true);
		
		this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		
//		this.setModal(true);
		this.setSize( new Dimension( 700, 500 ) );
		this.setTitle("Fit results");
		this.getContentPane().setLayout( null );
//		jlPosition.setBounds(new Rectangle(240, 363, 251, 52));
		jbOK.setBounds(new Rectangle(500, 381, 103, 31));
		jbOK.setText("Close");
		jbOK.addActionListener(new fraChart_jbOK_actionAdapter(this));
		
		jlResults.setSize(200,70);
		jlResults.setLocation(500, 200);
		jlResults.setForeground(Color.blue);
		
		
		this.getContentPane().add( jlChart, null );
		this.getContentPane().add( jlResults, null );
//		this.getContentPane().add(jlPosition, null);	 
		this.getContentPane().add(jbOK, null);  
//		this.setVisible(true);
	}
	
	void myInit() {
		Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
		Dimension frameSize = this.getSize();
		// center w.r.t screen:
		this.setLocation( ( screenSize.width - frameSize.width ) / 2,
				( screenSize.height - frameSize.height ) / 2 );
		
//		ToxPredictor.Utilities.Utilities.SetFonts(this.getContentPane());
		
		
		
	}
	
	public static class JLabelChart extends JLabel {
		
        private static final long serialVersionUID = 376070962352600060L;
        
        public double [] X={0,1,2,3,4,5}; // experimental value
		public double [] Y1={0,1.317,1.734,3.450,4.026,4.701}; //predicted/calculated value
		double [] err=null; // error on predicted value
		double [] SC=null;//similarity coefficients
		public boolean doDrawStatsMAE=false;
		public boolean doDrawStatsR2=false;
		public boolean doDrawLegend=false;
		
		Font fontGridLines=new Font( "Arial", Font.PLAIN, 10 );
		Font fontTitle=new Font( "Arial", Font.BOLD, 11);
		Font fontLegend = new Font("Arial", Font.PLAIN, 11);
		
		JLabel jlResults;

		// this class maps the sources relative to each other
		
		String Xtitle="Experimental toxicity"; // title of x-axis
		String Ytitle="Predicted toxicity"; // title of y-axis
		String ChartTitle="Model fit results"; // chart title
		double maxNum=10;
		
		double Xmin,Ymin,Xmax,Ymax,incr;
		int margin;
		int wdraw;
		
		int w; // width of this label
		DecimalFormat myF=new DecimalFormat("0");		
		DecimalFormat myF1=new DecimalFormat("0.0");
		
		int cw=6; // width of symbols;
		
		public JLabelChart() {
			int size = 800;
			setBorder(BorderFactory.createLineBorder(Color.black));
			setBounds(new Rectangle(25, 25, size, size));
		}

		public JLabelChart(double []X,double []Y1,String charttitle,String xtitle,String ytitle) {
			this();
			this.X=X;
			this.Y1=Y1;
			this.Xtitle=xtitle;
			this.Ytitle=ytitle;
			this.ChartTitle=charttitle;
		}
		
		public JLabelChart(double []X,double []Y) {
			this();
			this.X=X;
			this.Y1=Y;
		}
		
		public JLabelChart(double []X,double []Y1,String xtitle,String ytitle) {
			this();
			this.X=X;
			this.Y1=Y1;
			this.Xtitle=xtitle;
			this.Ytitle=ytitle;
		}

		public JLabelChart(double []X,double []Y1,double[] SC,String charttitle,String xtitle,String ytitle) {
			this();
			this.X=X;
			this.Y1=Y1;
			this.SC = SC;
			this.Xtitle=xtitle;
			this.Ytitle=ytitle;
			this.ChartTitle=charttitle;
		}

		public JLabelChart(double []X,double []Y1,double[] err) {
			this();
			this.X=X;
			this.Y1=Y1;
			this.err = err;
		}

		public void paintComponent( Graphics g ) {
			
			Graphics2D g2 = ( Graphics2D ) g;
			this.DrawChart(g2);
								  
//			this.WriteImageToFile();
			
			// draw fitted line:
			//this.DrawFittedLine(g2);
			
												
		} // end paintComponent
		
		void DrawChart(Graphics2D g2) {
			w=this.getWidth(); // width of drawing area;
			
			margin=(int)((double)w*0.15); // provides space for axis numbers and titles
			
			wdraw=w-2*margin;

			FontRenderContext frc = g2.getFontRenderContext();

			
			g2.setRenderingHint( RenderingHints.KEY_ANTIALIASING,
					RenderingHints.VALUE_ANTIALIAS_ON );
			
			g2.setColor(Color.white);			
			g2.fillRect(0,0,w,w); // make background white			

			SetAxisBounds();
			
			
			// draw horizontal grid lines:
			DrawHorizontalGridlines(g2,frc);
			
			//draw vertical gridlines:
			DrawVerticalGridlines(g2,frc);
			
			// draw in titles:
			DrawTitles(g2,frc);	
			
			// draw Y=X line:
			
			this.DrawYEqualsXLine(g2);

//			DrawStatsR2_RMSE(g2, frc);
			if (doDrawStatsMAE) DrawStatsMAE(g2, frc);
			
			if (doDrawLegend) DrawLegend(g2, frc);
			
			if (doDrawStatsR2) DrawStatsR2_RMSE(g2, frc);
			
			// draw symbols for exp vs pred series:
//			DrawSymbols (g2);
			DrawSymbols2 (g2);
			
			if (err instanceof double []) {
				DrawErrorBars(g2);
			}
			
			

		}
		
		public void WriteImageToFile(String filename,String OutputFolder) {
			this.WriteImageToFile(OutputFolder+"/"+filename);
		}
		
		public void WriteImageToFile(String filepath) {
			
			try {

				int w=this.getWidth();
				
				BufferedImage ImgSrc = new BufferedImage(w, w,
						BufferedImage.TYPE_INT_RGB);
				Graphics2D g2 = (Graphics2D) ImgSrc.getGraphics();

				this.DrawChart(g2);

				File myFile = new File(filepath);

				ImageIO.write(ImgSrc, "png", myFile);
			} catch (Exception e) {
				System.out.println("Exception creating pic file");
				e.printStackTrace();
			}
		}
		
		/**
		 * Write chart to a byte array, then encode, and return as web url
		 * @return URL
		 */
		public String createImgURL() {
			
			String imgURL=null;
			try {
				
				int w= this.getWidth();
				
				BufferedImage ImgSrc = new BufferedImage(w, w,
						BufferedImage.TYPE_INT_RGB);
				Graphics2D g2 = (Graphics2D) ImgSrc.getGraphics();

				this.DrawChart(g2);

				ByteArrayOutputStream baos = new ByteArrayOutputStream();
				ImageIO.write( ImgSrc, "png", baos );
				baos.flush();
				byte[] bytes = baos.toByteArray();
				baos.close();
				
				String base64 = Base64.getEncoder().encodeToString(bytes);
				
//				System.out.println("***"+base64);
				
				//need to add this or img url won't work (TMM):
				imgURL="data:image/png;base64, "+base64;
				
				return imgURL;
				
			} catch (IOException e) {
				e.printStackTrace();
				return null;
			}
		}
		
		
		
		
//		void SetAxisBounds() {
//			
//			Xmin=1e8;
//			Ymin=1e8;
//			Xmax=-1e8;
//			Ymax=-1e8;
//			
//			for (int i=0; i<X.length;i++) {
//				if (X[i]<Xmin) Xmin=X[i];
//				if (X[i]>Xmax) Xmax=X[i];
//				
//				if (Y1[i]<Ymin) Ymin=Y1[i];
//				//if (Y2[i]<Ymin) Ymin=Y2[i];
//				
//				if (Y1[i]>Ymax) Ymax=Y1[i];
//				//if (Y2[i]>Ymax) Ymax=Y2[i];
//								
//			}
//			
////			Xmin=Math.floor(Xmin)-1;
////			Xmax=Math.ceil(Xmax)+1;
////			Ymin=Math.floor(Ymin)-1;
////			Ymax=Math.ceil(Ymax)+1;
//			
//			Xmin=Math.floor(Xmin);
//			Xmax=Math.ceil(Xmax);
//			Ymin=Math.floor(Ymin);
//			Ymax=Math.ceil(Ymax);
//
//			
//
//			if (Xmax/20>1) {
//				double bob=Math.floor((Xmax-Xmin)/maxNum/10);
//				if (bob==0)bob=1;
//				bob*=10;
//				
//				Xmin=bob*Math.floor(Xmin/bob);
//				Xmax=bob*Math.ceil(Xmin/bob);
//				
//				double bob4=Math.floor((Xmax-Xmin)/maxNum/10);
//				bob4*=10;
//				
//				if (bob4>bob) {
//					Xmin=bob4*Math.floor(Xmin/bob4);
//					Xmax=bob4*Math.ceil(Xmin/bob4);
//				}
//			}
//			
//			
//			if (Ymax/20>1) {
//				double bob=Math.floor((Ymax-Ymin)/maxNum/10);
//				if (bob==0)bob=1;
//				bob*=10;
//				
//				double bob2=Math.floor(Ymin/bob);
//				Ymin=bob*bob2;
//				double bob3=Math.ceil(Ymax/bob);
//				Ymax=bob*bob3;
//			}
//
//
//			
//			// make bounds on both axes the same for tox graph:
//			if (Xmin<Ymin) Ymin=Xmin;
//			else Xmin=Ymin;
//			
//			if (Xmax>Ymax) Ymax=Xmax;
//			else Xmax=Ymax;
//			
////			System.out.println("Xmin="+Xmin);
////			System.out.println("Xmax="+Xmax);
////			System.out.println("Ymin="+Ymin);
////			System.out.println("Ymax="+Ymax);
//			
//			
//		}
		
//		void SetAxisBounds() {
//			
//			double min=1e8;
//			double max=-1e8;
//			
//			for (int i=0; i<X.length;i++) {
//				if (X[i]<min) min=X[i];
//				if (X[i]>max) max=X[i];
//				
//				if (Y1[i]<min) min=Y1[i];
//				if (Y1[i]>max) max=Y1[i];
//				//if (Y2[i]>Ymax) Ymax=Y2[i];
//								
//			}
//			
//			min=Math.floor(min);
//			max=Math.ceil(max);
//			
////			System.out.println(min);
////			System.out.println(max);
//			
//			if ((max-min)/10.0>1) {
//				double bob=Math.ceil((max-min)/maxNum/10);
//			
//				if (bob==0)bob=1;
//				bob*=10;
//				
////				double incrTemp=(Ymax-Ymin)/10;
////
////				if (incrTemp>1 && incrTemp<=5) bob=5;
////				if (incrTemp>5 && incrTemp<=10) bob=10;
////
////				if (incrTemp>10 && incrTemp<=50) bob=50;
////				if (incrTemp>50 && incrTemp<=100) bob=100;
//
//				min=Math.floor(min/bob)*bob;//make a multiple of bob
//				max=Math.ceil(max/bob)*bob;//make a multiple of bob
//			}
//			
//			Xmin=min;
//			Ymin=Xmin;
//			
//			Xmax=max;
//			Ymax=Xmax;
//			
//			incr=(Ymax-Ymin)/10;
//			
////			System.out.println("incr ="+incr);
//
//			if (incr>0.1 && incr<=0.5) incr=0.5;
//			if (incr>0.5 && incr<=1) incr=1;
//
//			if (incr>1 && incr<=5) incr=5;
//			if (incr>5 && incr<=10) incr=10;
//
//			if (incr>10 && incr<=50) incr=50;
//			if (incr>50 && incr<=100) incr=100;
//			
//			System.out.println("Xmin ="+Xmin);
//			System.out.println("Xmax ="+Xmax);
//			System.out.println("incr ="+incr);
//			
////			if (max/20.0>1) {
////			double bob=Math.floor((max-min)/maxNum/10);
////			if (bob==0)bob=1;
////			bob*=10;
////			
////			min=bob*Math.floor(min/bob);
////			max=bob*Math.ceil(max/bob);
////			
////			System.out.println(min);
////			System.out.println(max);
////			
////			double bob4=Math.floor((max-min)/maxNum/10);
////			bob4*=10;
////			
////			if (bob4>bob) {
////				min=bob4*Math.floor(min/bob4);
////				max=bob4*Math.ceil(max/bob4);
////			}
////		}
//
//
//		}
		
		void SetAxisBounds() {
			
			double min1=1e8;
			double max1=-1e8;
			
			for (int i=0; i<X.length;i++) {
				if (X[i]<min1) min1=X[i];
				if (X[i]>max1) max1=X[i];
				if (Y1[i]<min1) min1=Y1[i];
				if (Y1[i]>max1) max1=Y1[i];
			}
			
//			double min=Math.floor(min1);
//			double max=Math.ceil(max1);
//			
//			
//			incr=(max-min)/10;
//			
//			if (incr>0.1) {
//				if (incr>0.1 && incr<=0.5) incr=0.5;
//				if (incr>0.5 && incr<=1) incr=1;
//
//				if (incr>1 && incr<=5) incr=5;
//				if (incr>5 && incr<=10) incr=10;
//
//				if (incr>10 && incr<=50) incr=50;
//				if (incr>50 && incr<=100) incr=100;
//				
//				min=Math.floor(min/incr)*incr;//make a multiple of incr
//				max=Math.ceil(max/incr)*incr;//make a multiple of incr
//			} else {//incr=0.1 since max-min = 1
//				min=Math.floor(min1/incr)*incr;//make a multiple of incr
//				max=Math.ceil(max1/incr)*incr;//make a multiple of incr
//			}
//
//			double minDev=Math.abs(min-min1);
//			double maxDev=Math.abs(max-max1);
//
//			if (minDev<0.5*incr) min-=incr;//pad graph a bit if nearest point is too close
//			if (maxDev<0.5*incr) max+=incr;//pad graph a bit if nearest point is too close

			
			incr=(max1-min1)/10;

			if (incr<=0.1) incr=0.1;
			else if (incr>0.1 && incr<=0.5) incr=0.5;
			else if (incr>0.5 && incr<=1) incr=1;
			else if (incr>1 && incr<=5) incr=5;
			else if (incr>5 && incr<=10) incr=10;
			else if (incr>10 && incr<=50) incr=50;
			else if (incr>50 && incr<=100) incr=100;
			else if (incr>100) incr=100;
			
			if (incr<1 && max1>100) incr=1;//avoid overlapping x axis labels
			
			double min=Math.floor(min1/incr)*incr;//make a multiple of incr
			double max=Math.ceil(max1/incr)*incr;//make a multiple of incr

			double minDev=Math.abs(min-min1);
			double maxDev=Math.abs(max-max1);

			if (minDev<0.5*incr) min-=incr;//pad graph a bit if nearest point is too close
			if (maxDev<0.5*incr) max+=incr;//pad graph a bit if nearest point is too close
			
			Xmin=min;
			Ymin=Xmin;
			
			Xmax=max;
			Ymax=Xmax;
			
			//TODO: calc if labels overlap:
			

			
//			System.out.println("Xmin ="+Xmin);
//			System.out.println("Xmax ="+Xmax);
//			System.out.println("incr ="+incr);
			
		}


		
		
		void DrawHorizontalGridlines(Graphics2D g2, FontRenderContext frc) {
//			System.out.println("enter draw horizontal");
			// this method draws horizontal gridlines and labels them
			
//			double bob=Math.ceil((Ymax-Ymin)/maxNum/10);
//			if(bob==0) bob=1;
//			bob*=10;
//			
//			double incr=1.0; //12-14-11
//			boolean shrink=false;
//			if (Ymax-Ymin<=1) {
//				shrink=true;// we need go in 0.1 increments otherwise we wont have any divisions
//				incr=0.1;//12-14-11
//			}
				
//			for (double y=Ymin;y<=Ymax; y+=incr) {
//				
//				if (Ymax-Ymin>maxNum && Ymax-Ymin>20) {
//					if ((y)%bob!=0) continue;
//				}
//				
//				double yy = ( Ymax - y) * (wdraw) / ( Ymax-Ymin);
//				int iy=(int)Math.round(yy)+margin;
//				
//				g2.setColor(Color.lightGray);
//				g2.drawLine(margin,iy,wdraw+margin,iy);
//				
//				g2.setColor(Color.black);    
//								
//				// draw in numbers on y-axis:
//				String strNum=myF.format(y);
//				if (shrink) strNum=myF1.format(y);
//
//				double swidth = (double) g2.getFont().getStringBounds(strNum, frc).
//				getWidth();
//				
//				double sheight = (double) g2.getFont().getStringBounds(strNum, frc).
//				getHeight();
//																				
//				g2.drawString(strNum,margin-(int)swidth-3,iy+(int)(sheight/2));
//				
//			}//end loop
			
//			System.out.println("incr ="+incr);
			
			g2.setFont(fontGridLines);
			
			for (double y=Ymin;y<=Ymax+0.0001; y+=incr) {
//				System.out.println("y="+y+"\t"+incr);
				
				double yy = ( Ymax - y) * (wdraw) / ( Ymax-Ymin);
				int iy=(int)Math.round(yy)+margin;
				
				g2.setColor(Color.lightGray);
				g2.drawLine(margin,iy,wdraw+margin,iy);
				
				g2.setColor(Color.black);    
								
				// draw in numbers on y-axis:
				String strNum=myF.format(y);
				if (incr<1) strNum=myF1.format(y);

				double swidth = (double) g2.getFont().getStringBounds(strNum, frc).
				getWidth();
				
				double sheight = (double) g2.getFont().getStringBounds(strNum, frc).
				getHeight();
																				
				g2.drawString(strNum,margin-(int)swidth-3,iy+(int)(sheight/2));
				
			}//end loop
			
//			System.out.println("leave draw horizontal");

			g2.setColor(Color.lightGray);
			g2.drawLine(margin,margin,wdraw+margin,margin);//draw min line
			int iy=(int)Math.round(wdraw)+margin;
			g2.drawLine(margin,iy,wdraw+margin,iy);

			
		}
		
		void DrawVerticalGridlines(Graphics2D g2, FontRenderContext frc) {
			// draw vertical grid lines
			g2.setFont(fontGridLines);
			
			for (double x=Xmin;x<=Xmax+0.0001; x+=incr) {
//				System.out.println("x="+x);
				
				double xx = ( x - Xmin ) * (wdraw) / ( Xmax-Xmin);
				int ix=(int)Math.round(xx)+margin;
				
				g2.setColor(Color.lightGray);
				g2.drawLine(ix,margin,ix,wdraw+margin);
				
				g2.setColor(Color.black);        
				
				String strNum=myF.format(x);
				if (incr<1) strNum=myF1.format(x);
					
				double swidth = g2.getFont().getStringBounds(strNum, frc).getWidth();			
				double sheight = g2.getFont().getStringBounds(strNum, frc).getHeight();
				                																										
				// draw in numbers on x-axis:
				g2.drawString(strNum+"",ix-(int)(swidth/2.0),wdraw+margin+(int)sheight+3);
				
								
			}
			g2.setColor(Color.lightGray);
			g2.drawLine(margin,margin,margin,wdraw+margin);
			g2.drawLine(wdraw+margin,margin,wdraw+margin,wdraw+margin);
//			System.out.println("Xmax="+Xmax);

//			double bob=Math.ceil((Xmax-Xmin)/maxNum/10);
//			if(bob==0) bob=1;
//			bob*=10;
//
//			double incr=1.0; //12-14-11
//			boolean shrink=false;
//			if (Xmax-Xmin<=1) {
//				shrink=true;// we need go in 0.1 increments otherwise we wont have any divisions
//				incr=0.1;//12-14-11
//			}
//			
//			for (double x=Xmin;x<=Xmax; x+=incr) {
//				
//				if (Xmax-Xmin>maxNum && Xmax-Xmin>20) {
//					if ((x)%bob!=0) continue;
//				}
//				
//				double xx = ( x - Xmin ) * (wdraw) / ( Xmax-Xmin);
//				int ix=(int)Math.round(xx)+margin;
//				
//				g2.setColor(Color.lightGray);
//				g2.drawLine(ix,margin,ix,wdraw+margin);
//				
//				g2.setColor(Color.black);        
//				
//				String strNum=myF.format(x);
//				if (shrink) strNum=myF1.format(x);
//					
//				double swidth = g2.getFont().getStringBounds(strNum, frc).getWidth();			
//				double sheight = g2.getFont().getStringBounds(strNum, frc).getHeight();
//				                																										
//				// draw in numbers on x-axis:
//				g2.drawString(strNum+"",ix-(int)(swidth/2.0),wdraw+margin+(int)sheight+3);
//				
//								
//			}
			
			
			
			
		
		}
		
		void DrawTitles(Graphics2D g2,FontRenderContext frc) {

			g2.setFont( fontGridLines);
			
			String strMax=myF.format(Ymax);
			if (incr<1) strMax=myF1.format(Ymax);

			double widthMaxNumber = (double) g2.getFont().getStringBounds(strMax, frc).getWidth();
			double heightMaxNumber = (double) g2.getFont().getStringBounds(strMax, frc).getHeight();
			
			int textPadding=5;
			
			g2.setColor(Color.black);
			g2.setFont(fontTitle);
			
			double sheight = (double) g2.getFont().getStringBounds("P", frc).
			getHeight();
			
			double swidthXtitle = (double) g2.getFont().getStringBounds(Xtitle, frc).
			getWidth();
			double swidthYtitle = (double) g2.getFont().getStringBounds(Ytitle, frc).
			getWidth();
			double swidthTitle = (double) g2.getFont().getStringBounds(ChartTitle, frc).
			getWidth();

			double xx=(w-swidthXtitle)/2.0;
			int ixx=(int)Math.round(xx);
			
			double yy=wdraw+margin+heightMaxNumber+sheight+textPadding;
//			double yy=wdraw+margin+0.5*margin+sheight/2;
			int iyy=(int)Math.round(yy);
			
			// draw title of x-axis:			
			g2.drawString(Xtitle,ixx,iyy);
//			(int)(w +margin+sheight+20)
			// ***************************************************************
			
			xx=swidthYtitle+(getHeight()-swidthYtitle)/2.0;				
			ixx=(int)Math.round(xx);
		
//			yy=margin-(int)widthMaxNumber-sheight-textPadding;
			yy=margin-(int)widthMaxNumber-10;
			
//			yy=0.5*margin-sheight/2;
			iyy=(int)Math.round(yy);
			
			int angle=-90;
			g2.rotate(angle*Math.PI/180.0);
			g2.setFont(fontTitle);
//			draw title of y-axis:	
			g2.drawString(Ytitle,-ixx,iyy);			
			g2.rotate(-angle*Math.PI/180.0);
			
			// ***************************************************************
			
			// now draw chart title:
			g2.setFont( fontTitle);
			
			
			sheight = (double) g2.getFont().getStringBounds(ChartTitle, frc).
			getHeight();
			
			g2.drawString(ChartTitle,(int)((w-swidthTitle)/2.0),margin/2+(int)(sheight/2));
					
		}
		
		/*void DrawFittedLine(Graphics2D g2) {
			g2.setColor(Color.black);
			
			double x1 = ( X[1] - Xmin ) * wdraw / ( Xmax-Xmin);
			int ix1 = ( int ) Math.round( x1)+margin;
			
			double x2 = ( X[X.length-1] - Xmin ) * wdraw / ( Xmax-Xmin);
			int ix2 = ( int ) Math.round( x2)+margin;
			
			double y1 = ( Ymax - Y2[1] ) * wdraw / ( Ymax-Ymin );
			int iy1 = ( int ) Math.round( y1)+margin;
			
			double y2 = ( Ymax - Y2[X.length-1] ) * wdraw / ( Ymax-Ymin );
			int iy2 = ( int ) Math.round( y2)+margin;
			
			g2.drawLine(ix1,iy1,ix2,iy2);
		}
		*/
		
		void DrawYEqualsXLine(Graphics2D g2) {
			g2.setColor(Color.black);
			
			
			int ix1 = margin; // x=0					
			int ix2 = wdraw+margin;	// x=Xmax					
			int iy1 = wdraw+margin; // y=0
			int iy2 = margin; // y=Ymax
			
			g2.drawLine(ix1,iy1,ix2,iy2);
		}
		
		void DrawSymbols(Graphics2D g2) {
//			 now draw in data:                  
			for (int i = 0; i < X.length; i++) {            	  
				double x = ( X[i] - Xmin ) * (wdraw) / ( Xmax-Xmin);
				int ix = ( int ) Math.round( x -cw/2)+margin;
				
				double y = ( Ymax - Y1[i] ) * (wdraw) / ( Ymax-Ymin );
				int iy = ( int ) Math.round( y -cw/2)+margin;
				
				Color color=null;
				
				if (SC!=null) {//makes symbol red if SC=1, white if SC<=0.5
					int red=255;

					//linear transition from red (SC=1) to white (SC=0.5):
					int green=(int)(-510*SC[i]+510);
					
					//linear transition from red (SC=1) to white (SC=0.75)
//					int green=(int)(-1020*SC[i]+1020);
					
//					int green =(int)(-3177.9*Math.pow(SC[i], 3)+ 5591.1*Math.pow(SC[i], 2) - 3335.3*SC[i] + 922.09);//cubic gets whiter faster-easier to see diffs
					
					if (green>255) green=255;
					
					int blue=green;
					color=new Color(red,green,blue);
				} else {
					color=Color.red;
				}
				
				g2.setColor(color);
				g2.fillOval(ix, iy, cw, cw);
				
				g2.setColor(Color.black);
				g2.drawOval(ix, iy, cw, cw);								
			}
			
		}
		
		void DrawSymbols2(Graphics2D g2) {
//			 now draw in data:                  
			for (int i = 0; i < X.length; i++) {            	  
				double x = ( X[i] - Xmin ) * (wdraw) / ( Xmax-Xmin);
				int ix = ( int ) Math.round( x -cw/2)+margin;
				
				double y = ( Ymax - Y1[i] ) * (wdraw) / ( Ymax-Ymin );
				int iy = ( int ) Math.round( y -cw/2)+margin;
				
				Color color=null;
				
				if (SC!=null) {//makes symbol red if SC=1, white if SC<=0.5

					color=getColor(SC[i]);
				
				} else {
					color=Color.red;
				}
				
				g2.setColor(color);
				g2.fillOval(ix, iy, cw, cw);
				
				g2.setColor(Color.black);
				g2.drawOval(ix, iy, cw, cw);								
			}
			
		}
		
		public static Color getColor(double SCi) {

			Color color = null;

			if (SCi >= 0.9) {
				color = Color.green;
			} else if (SCi < 0.9 && SCi >= 0.8) {
				// color=Color.blue;
				color = new Color(100, 100, 255);// light blue
			} else if (SCi < 0.8 && SCi >= 0.7) {
				color = Color.yellow;
			} else if (SCi < 0.7 && SCi >= 0.6) {
				color = Color.orange;
			} else if (SCi < 0.6) {
				// color=Color.red;//255,153,153
				color = new Color(255, 100, 100);// light red
			}

			if (color == null)
				System.out.println("null color for " + SCi);
			// System.out.println(SCi+"\t"+color.getRGB());
			return color;
		}

		
		void DrawErrorBars(Graphics2D g2) {
//			 now draw in data:                  
			for (int i = 0; i <X.length; i++) {            	  
				double x = ( X[i] - Xmin ) * (wdraw) / ( Xmax-Xmin);
				int ix = ( int ) Math.round( x)+margin;
				
				int ix1 = ix-cw/2;
				int ix2 = ix+cw/2;

				double y1 = ( Ymax - (Y1[i]-err[i]) ) * (wdraw) / ( Ymax-Ymin );
				int iy1 = ( int ) Math.round( y1)+margin;

				double y2 = ( Ymax - (Y1[i]+err[i]) ) * (wdraw) / ( Ymax-Ymin );
				int iy2 = ( int ) Math.round( y2)+margin;

				g2.setColor(Color.black);
				g2.drawLine(ix,iy1,ix,iy2);				
				g2.drawLine(ix1,iy1,ix2,iy1);
				g2.drawLine(ix1,iy2,ix2,iy2);				
												
			}
			
		}

		void DrawStatsR2_RMSE(Graphics2D g2, FontRenderContext frc) {

			double MeanX = 0;
			double MeanY = 0;

			for (int i = 0; i < X.length; i++) {
				MeanX += X[i];
				MeanY += Y1[i];
			}
			// System.out.println("");

			MeanX /= (double) X.length;
			MeanY /= (double) X.length;

			// double Yexpbar=this.ccTraining.meanOrMode(this.classIndex);

			// System.out.println("Yexpbar = "+Yexpbar);

			double termXY = 0;
			double termXX = 0;
			double termYY = 0;
			double SSreg=0;
			double SStot=0;
			double MAE=0;

			double R2 = 0;

			for (int i = 0; i < X.length; i++) {
				termXY += (X[i] - MeanX) * (Y1[i] - MeanY);
				termXX += (X[i] - MeanX) * (X[i] - MeanX);
				termYY += (Y1[i] - MeanY) * (Y1[i] - MeanY);
				SSreg+=Math.pow(X[i]-Y1[i],2.0);
				MAE+=Math.abs(X[i]-Y1[i]);
				SStot+=Math.pow(X[i]-MeanX,2.0);
			}

			R2 = termXY * termXY / (termXX * termYY);
			double R2old = 1 - SSreg / SStot;

			double R = Math.sqrt(R2);
			
			double RMSE = Math.sqrt(SSreg / (double) X.length);
			MAE /= (double)X.length;
			
//			System.out.println(R2+"\t"+R2old);
			 
			DecimalFormat myDF = new DecimalFormat("0.000");

			String results = "<html>R<sup>2</sup> =" + myDF.format(R2)
					+ "<br>R=" + myDF.format(R) + "<br>RMSE = "
					+ myDF.format(RMSE) + "<br>N=" + X.length + "</html>";

			if (jlResults != null)
			    jlResults.setText(results);

//			String s1 = "r2=" + myDF.format(R2);
//			int s1width = (int) g2.getFont().getStringBounds(s1, frc)
//			.getWidth();
//			int s1height = (int) g2.getFont().getStringBounds(s1, frc)
//			.getHeight();

			String s1a = "r";
			String s1b = "2";
			String s1c = " = " + myDF.format(R2);

			if (X.length == 1)
				s1c = " = N/A";

			String s2 = "RMSE = " + myDF.format(RMSE);


			int s1awidth = (int) g2.getFont().getStringBounds(s1a, frc)
					.getWidth();
			
			int s1aheight = (int) g2.getFont().getStringBounds(s1a, frc)
			.getHeight();


			int s1bwidth = (int) g2.getFont().getStringBounds(s1b, frc)
					.getWidth();

			int s1cwidth = (int) g2.getFont().getStringBounds(s1c, frc)
					.getWidth();

			int s2width = (int) g2.getFont().getStringBounds(s2, frc)
					.getWidth();


			int s2height = (int) g2.getFont().getStringBounds(s2, frc)
					.getHeight();

			int xbox = 75;
			int ybox = 75;
			int xpad = 12;
			int ypad = 12;

			int widthbox = s2width + xpad;
			int heightbox = s1aheight + s2height + ypad;

			g2.setColor(Color.white);
			g2.fillRect(xbox, ybox, widthbox, heightbox);

			g2.setColor(Color.black);
			g2.drawRect(xbox, ybox, widthbox, heightbox);

			Font f11 = new Font("Arial", Font.BOLD, 11);
			Font f6 = new Font("Arial", Font.BOLD, 6);

			g2.setColor(Color.black);
			g2.setFont(f11);

			// g2.drawString(s1,90,90);

			g2.drawString(s1a, xbox + xpad, ybox + ypad + 5);
			g2.setFont(f6);
			g2.drawString(s1b, xbox + xpad + s1awidth, ybox + ypad);
			g2.setFont(f11);
			g2.drawString(s1c, xbox + xpad + s1awidth + s1bwidth, ybox + ypad
					+ 5);

			g2.setFont(f11);
			g2.drawString(s2, xbox + xpad, ybox + ypad + 5 + s1aheight);

		}
		
		void DrawStatsMAE(Graphics2D g2, FontRenderContext frc) {

			double MAE=0;

			for (int i = 0; i < X.length; i++) {
				MAE+=Math.abs(X[i]-Y1[i]);
			}

			MAE /= (double)X.length;
			
			 
			DecimalFormat myDF = new DecimalFormat("0.00");

			String results = "<html>MAE=" + myDF.format(MAE)
					+ "</html>";

			if (jlResults != null)
			    jlResults.setText(results);

			String s1 = "MAE = "+myDF.format(MAE);

			int s1width = (int) g2.getFont().getStringBounds(s1, frc)
					.getWidth();
			
			int s1height = (int) g2.getFont().getStringBounds(s1, frc)
					.getHeight();

			int xbox = (int)(margin+0.05*wdraw);
			int ybox = (int)(margin+0.05*wdraw);
			int xpad = 12;
			int ypad = 12;

			int widthbox = s1width + xpad;
			int heightbox = s1height + ypad;
			
			boolean havePointInBox=HavePointInBox(xbox, ybox, widthbox, heightbox);
			
			if (havePointInBox) {
				//try bottom right corner:
				xbox = (int)(w-margin-0.05*wdraw-widthbox);
				ybox = (int)(w-margin-0.05*wdraw-heightbox);
				
				havePointInBox=HavePointInBox(xbox, ybox, widthbox, heightbox);
				
				if (havePointInBox) {//put it back to upper left
					xbox = (int)(margin+0.05*wdraw);
					ybox = (int)(margin+0.05*wdraw);
				}
			}
			
			

			g2.setColor(Color.white);
			g2.fillRect(xbox, ybox, widthbox, heightbox);

			g2.setColor(Color.black);
			g2.drawRect(xbox, ybox, widthbox, heightbox);


			g2.setColor(Color.black);
			g2.setFont(fontLegend);

			g2.drawString(s1, xbox + xpad, ybox + ypad + 5);

		}
		
		void DrawLegend(Graphics2D g2, FontRenderContext frc) {

			int xbox = (int)(margin+0.05*wdraw);
			int ybox = (int)(margin+0.05*wdraw);
			int xpad = 12;
			int ypad = 12;

			int widthbox = 75;
			int heightbox = 40;
			
			boolean havePointInBox=HavePointInBox(xbox, ybox, widthbox, heightbox);
			
			if (havePointInBox) {
				//try bottom right corner:
				xbox = (int)(w-margin-0.05*wdraw-widthbox);
				ybox = (int)(w-margin-0.05*wdraw-heightbox);
				
				havePointInBox=HavePointInBox(xbox, ybox, widthbox, heightbox);
				
				if (havePointInBox) {//put it back to upper left
					xbox = (int)(margin+0.05*wdraw);
					ybox = (int)(margin+0.05*wdraw);
				}
			}
			
			g2.setColor(Color.white);
			g2.fillRect(xbox, ybox, widthbox, heightbox);

			g2.setColor(Color.black);
			g2.drawRect(xbox, ybox, widthbox, heightbox);

			Font f11 = new Font("Arial", Font.PLAIN, 11);

			g2.setColor(Color.black);
			g2.setFont(f11);
			
			int ix=xbox+10;
			int iy=ybox+10;
			g2.setColor(Color.red);
			g2.fillOval(ix, iy, cw, cw);
			
			g2.setColor(Color.black);
			g2.drawOval(ix, iy, cw, cw);								

			String s1="Exp.";
			int s1height = (int) g2.getFont().getStringBounds(s1, frc)
			.getHeight();

			g2.setColor(Color.black);
			g2.setFont(f11);
			g2.drawString(s1, ix+15, (int)(iy+s1height/2.0)+2);
			
			int iy2=iy+20;
			g2.drawLine(ix-5, iy2, ix+10, iy2);

			String s2="Y=X line";
			g2.drawString(s2, ix+15, (int)(iy2+4));
		}

		
		boolean HavePointInBox(int xbox,int ybox,int widthbox,int heightbox) {

			
			double x1=xbox;
			double x2=xbox+widthbox;
			double y1=ybox;
			double y2=ybox+heightbox;
			
			for (int i = 0; i < X.length; i++) {            	  
				double x = ( X[i] - Xmin ) * (wdraw) / ( Xmax-Xmin);
				int ix = ( int ) Math.round( x -cw/2)+margin;
				
				double y = ( Ymax - Y1[i] ) * (wdraw) / ( Ymax-Ymin );
				int iy = ( int ) Math.round( y -cw/2)+margin;
				
				if (ix >=x1 && ix <=x2 && iy>=y1 && iy<=y2) {
					return true;
				}
				
			}
			
			return false;
			
			
		}

	} // end MAP class
	
	public static void main( String[] args ) {
		
		
		
//		double [] err={0.2,0.3,0.1,0.01,0.5}; //error

		// eldred training set: predictions using type III model
		
//		double[] x = { 1.86, 1.4, 0.55, 2.12, 0.57, 1.44, -0.81, 0.72, -2.15,
//				1.35, 3.55, 0.04, 1.62, -0.61, 0.81, 2.36, 2.26, 2.67, 0.4,
//				0.42, 0.94, 1.89, 1.08, 0.75, 4.46, 1.07, 1.34, 0.57, 0.73,
//				0.96, 0.29, -0.02, 2.51, -1.13, 1.41, 1.54, 1.22, -0.28, 2.86,
//				-1.25, 1.42, 3.5, 1.32, 1.73, 2.65, -0.41, 0.6, -0.3, 2.92,
//				0.84, -0.08, 1.74, 1.42, 2.19, -0.43, 3.77, 0.58, 0.92, 1.05,
//				-1.3, -2.95, 4.74, 0.34, 2.44, 1.3, 2.07, 0.95, 1.28, -0.5,
//				0.11, 0.73, 0.49, 2.18, -1.25, -0.84, 1.32, -0.64, 0.02, -0.06,
//				0.93, 0.47, 0.89, 0.62, 1.89, 1.42, -0.45, -0.64, -0.63, 1.44,
//				1.21, 1.18, -2.16, 1.4, 1.08, 0.54, -1.61, 0.1, 0.11, 0.17,
//				2.79, 0.78, 2.26, 0, -1.77, 1.1, -0.71, 2.54, 6, 0.21, -2.85,
//				1.4, 2.22, 1.33, 1.47, 1.73, 1.85, -1.88, -0.14, -1.96, -0.88,
//				0.81, 0.93, 0.65, 4.78, 0.25, 2.11, -0.99, 1.22, 0.81, 0.47,
//				1.95, -1.94, -0.73, 1.25, -0.85, 3.6, -1.41, 2.45, -0.04, 1.75,
//				1.35, 0.82, -2.64, 1.28, 2.42, 0.67, -0.56, -1.07, 0.98, 0.75,
//				-0.14, 0.62, 0.42, 0.35, 4.33, 4.29, 1.21, 2.54, 0.25, 0.1,
//				-0.74, -0.73, 1.85, 2.16, 2.08, 1.03, 0.01, 0.33, 2.72, 0,
//				-0.31, 2.85, 0.99, -0.94, 0.56, 0.54, 2.1, -0.05, 1.81, -1.65,
//				-0.12, -2.21, 0.5, 1.84, -0.41, 0.43, 3.53, 1.53, 3.52, 0.86,
//				2.68, 3.69, -0.84, 1.47, 1.36, 1.63, 0.72, 1.23, 1.4, 0.73,
//				0.51, 0.44, 1.7, 0.84, -0.87, 1.14, -0.72, 1.03, 0.55, 1.78,
//				-1.11, 0.25, 1.6, 1.08, 1.58, 2.26, 2.05, 0.51, -0.64, -0.13,
//				0.04, -2.67, 1.32, -1.54, 2.91, -0.26, -1.37, 0.51, 0.21, 0.02,
//				1.18, 1.97, 2.38, 0.53, -1.53, -0.1, 0.45, -1.07, 0.07, 0.22,
//				0.29, 0.21, 0.84, 0.66, 0.76, 0.92, 0.22, 2.21, 1.02, 1.02,
//				0.57, 0.77, 1.9, 0.65, -1.9, 1.2, 1.09, -0.59, -0.5, 2.16,
//				2.35, 1.4, 0.02, 1.99, -1.94, -0.88, -0.19, 1.19, 3.4, -0.4,
//				0.77, 1.29, -0.89, -0.14, -2.4, 0.63, 1.23, -1.48, 3.06, 0.36,
//				0.96, 2.2, 0.23, 3.63, -0.19, 2.31, 3.83 };
//		
//		double[] y = { 1.58, 0.86, 0.28, 1.85, 1.2, 1.57, -0.12, 0.89, -0.45,
//				0.6, 4.67, 1.4, 1.56, 0.69, 0.03, 2.47, 1.76, 1.06, 0.74, 1.06,
//				0.78, 1.41, 1.28, 1.45, 4.68, 1.58, 0.14, 0.37, 0.69, 0.97,
//				0.46, -0.62, 1.94, -0.51, 0.75, 1.55, 1.35, -0.53, 3.16, 0.52,
//				1.37, 3.28, 1.27, 2.16, 2.24, 0.44, 0.34, 0.12, 2.52, 0.96,
//				2.49, 1.43, 0.94, 1.45, -0.18, 2.51, 0.6, 1.15, 2.03, -1.69,
//				-1.86, 4.44, -0.41, 3.49, 1.46, 0.1, 0.54, 1.28, 0.35, 1.04,
//				1.15, 0.64, 2.2, -0.19, -1.38, 1.04, -0.37, 0.73, 0.65, 1.66,
//				1.08, 1.07, 0.54, 2.06, 1.5, -0.66, -1.39, 0.53, 1.4, 0.88,
//				1.13, -1.11, 0.95, 2.72, 0.82, -0.52, 1.1, 0.67, 0.01, 3.15,
//				0.64, 2.57, -0.18, -0.87, 0.84, -0.08, 1.97, 4.19, 0.77, -2.95,
//				1.43, 1.77, 0.98, 1.02, 2.03, 1.48, -1.73, -0.07, -2.24, -1.1,
//				0.5, 1.15, 0.35, 4.32, -0.03, 1.44, -0.58, 1.01, 0.25, 0.69,
//				1.61, -1.65, 0.66, 1, -1.3, 3.48, -1.56, 1.46, 0.3, 0.07, 0.73,
//				0.51, -0.54, 0.46, 1.78, 1.64, 0.33, -0.56, 0.89, 0.32, 0.21,
//				0.87, 0.37, 0.97, 3.49, 3.58, 0.54, 2, 1.25, 0.13, 0.04, -0.8,
//				2.23, 2.55, 1.52, 0.79, 0.1, 1.2, 1.9, 0.06, 0.43, 2.89, 1.61,
//				0.51, 0.65, 0.69, 2, 0.41, 1.03, -0.34, -0.44, -1.84, 0.09,
//				2.86, -0.34, 0.39, 0.7, 1.33, 3.54, 0.78, 2.51, 2.35, -0.6,
//				0.88, 1.33, 1.62, 0.04, 1.16, 1.26, 0.44, 0.88, 0.39, 2.04,
//				1.76, 0.62, 0.82, -0.59, 0.67, 1.06, 2.11, -0.83, 0.39, 0.54,
//				0.93, 1.76, 0.15, 1.75, 0.43, -0.91, 0.39, 0.3, -2.08, 1.17,
//				-1.21, 2.31, -0.27, -1.38, 0.86, 0.78, -0.05, 1.48, 2.64, 2.52,
//				2.72, -1.5, -0.19, 0.71, 0.11, 0.45, 0.48, -0.25, -0.01, -0.38,
//				1.03, 0.42, 1.05, 0.1, 1.27, 0.98, 0.87, 0.15, 0.25, 1.64,
//				-0.2, -2, 1.44, 1.66, 0.53, -0.3, 1.21, 1.96, 1.76, -0.05,
//				1.38, -2, -0.62, -0.37, -0.09, 1.51, -0.03, 0.34, 1.64, -0.99,
//				-0.87, -3.39, 0.35, 0.82, -1.42, 2.37, 1, 0.51, 2.07, -0.42,
//				2.07, 0.09, 1.66, 1.13 };
//		
//		
//		
//		double [] err=new double [x.length];
//		
//		// create dummy error array for convenience:
//		for (int i=0;i<x.length;i++) err[i]=Math.random();// use random
//																// number from
//																// 0-1 for
//																// error for
//																// each pt
//				
//		fraChart fc = new fraChart(x,y,err);
//		
////		fc.jlChart.WriteImageToFile();
//		
//		fc.setVisible(true);
		
//		fraChart fc = new fraChart(x,y,null); // no error bars
//		fc.setTitle("Fit results for Type III model for training set (Eldred paper)");		
		
		

		// eldred prediction set:
		
//		 double[] x = { 1.73, 3.26, -1.65, -1, 2.43, 0.53, 1.14, -0.08, -0.14,
//		 -2.05, 1.78, -2.93, 2.27, 2.01, 1.96, 0.12, 0.7, 0.7, 0.82,
//		 0.75, -0.56, 1.89, 0.21, 0.4, 0.48, 0.66, 0.23, 1, 0.03, 3.72,
//		 2.49, 0.15, 5.43, 0.66, 0.05, 2.82, 0.24, 1.91, -0.22, 0.62,
//		 1.65, 0.79, 0.35, 1.45 };
//		 double[] y = { 0.97, 2.25, -0.98, -0.31, 2.59, 0.88, 0.55, -1.07, 0.46,
//		 -2.78, 2.49, -2.85, 2.95, 1.54, 0.53, 0.26, 0.89, 0.63, -0.07,
//		 1.73, -0.36, 1.8, 1.43, 0.42, 0.25, 1.1, 0.67, 1.76, -0.06,
//		 2.95, 1.01, -0.48, 4.34, 0.09, 0.12, 1.02, 0.27, 2.15, 0.54,
//		 0.41, 1.67, 0.08, 0.37, 1.38 };
//		fraChart fc = new fraChart(x,y,null); // no error bars
//		fc.setTitle("Fit results for Type III model for prediction set");	
		
//**************************************************************************
//		double[] x = { 100,200,300,400,500,600,700,800,900,1000,1100 };
//		double[] y = { 110,190,302,430,470,601,702,803,904,1005,1106 };
//		for (int i=0;i<x.length;i++) {
//			double incr=1;
//			x[i]*=incr;
//			y[i]*=incr;
//			System.out.println(x[i]+"\t"+y[i]);
//		}

//		double []x={85.0000,116.0000,-17.0000,12.0000,-54.0000,	12.0000,1.0000,	-23.0000,15.0000,-7.0000,-41.0000,-37.0000,103.9000,42.0000,31.1000,0.4000,128.7000,145.0000,40.0000,109.9000,109.9000,54.3000};
//		double[] y = { 104.2035, 100.9223, 0.0577, -2.2151, -50.7597, 0.9757,
//				-1.4695, -21.1542, 14.8926, 0.4276, -51.7317, -40.9900,
//				77.3995, 43.3887, 33.4775, 23.3004, 137.7889, 134.3696,
//				47.8241, 110.1179, 110.1179, 56.2563 };
		
//		double []x={2.98,3.76,3.25,3.31,2.61,3.12};
//		double[] y = { 3.62,3.88,3.21,3.05,3.32,3};
//		double [] SC={0.95,	0.9,0.85,0.80,0.75,0.5};

//		double []x={0.7};
//		double[] y = { 0.74};

//		double []x={10.7};
//		double[] y = { 10.74};
		
//		double []x={184.68 	};
//		double[] y = { 185.42};
		
		double []x={184.68 ,186	};
		double[] y = { 185.42,185.9};

		
//		double []y={184.68 	};
//		double[] x = { 185.42};


//		double []x={1000.7};
//		double[] y = { 1000.74};

		
//		double []x={2.9,5.45};
//		double[] y = { 2.9,5.45 };

//		double []x={184};
//		double[] y = { 185};

		
//		int num=200;
//		double []x=new double[num];
//		double []y=new double[num];
//		for (int i=0;i<num;i++) {
//			x[i]=i+0.5;
//			double rand=Math.random();
//			if (rand<0.5) y[i]=i+0.5+(num/10.0)*Math.random();
//			else y[i]=i+0.5-(num/10.0)*Math.random();
//			
//			x[i]+=35;
//			y[i]+=35;
//		}

//		double []x={35,100,208};
//		double[] y = { 33,95,202 };

		
//		double []x={-0.5,1,1.5};
//		double[] y = { -0.5,1,1.5 };


//		fraChart fc = new fraChart(x,y);

		String title="Fathead minnow LC50 (96 hr) -Log(mol/L)";
		String xtitle="exp. "+title;
		String ytitle="pred. "+title;
		fraChart fc = new fraChart(x,y,xtitle,ytitle);
		fc.jlChart.doDrawLegend=false;
		fc.jlChart.doDrawStatsMAE=false;
		
//		fraChart fc = new fraChart(x,y,SC,"Chart with SC",xtitle,ytitle);
		
//		fc.jlChart.WriteImageToFile();
		
		fc.setVisible(true);
		
	}
	
	void jbOK_actionPerformed(ActionEvent e) {
		// this.setVisible(false);
		System.exit(0);
		
		
	}
	

	class fraChart_jbOK_actionAdapter implements java.awt.event.ActionListener {
		fraChart adaptee;
		
		fraChart_jbOK_actionAdapter(fraChart adaptee) {
			this.adaptee = adaptee;
		}
		public void actionPerformed(ActionEvent e) {
			adaptee.jbOK_actionPerformed(e);
		}
	}
	
	
	
	
}
