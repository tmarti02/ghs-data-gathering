package hazard;

import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Base64;

import javax.imageio.ImageIO;
import javax.swing.ImageIcon;

import org.apache.commons.io.IOUtils;
import org.openscience.cdk.interfaces.IAtomContainer;
import org.openscience.cdk.DefaultChemObjectBuilder;
import org.openscience.cdk.depict.Depiction;
import org.openscience.cdk.depict.DepictionGenerator;
import org.openscience.cdk.exception.CDKException;

import org.openscience.cdk.smiles.SmilesParser;

import com.epam.indigo.IndigoException;


/**
 * Class to create image files and embedded image link in html
 * 
 * @author TMARTI02
 *
 */
public class StructureImageUtil {
	
	private static final SmilesParser parser = new SmilesParser(DefaultChemObjectBuilder.getInstance());

	
	
	public static ImageIcon decodeBase64ToImageIcon(String imageString, int size) {
   	 
        BufferedImage image = null;
        byte[] imageByte;
        try {
//        	System.out.println(imageString);
            imageByte = Base64.getDecoder().decode(imageString.replace("data:image/png;base64, ", ""));
            ByteArrayInputStream bis = new ByteArrayInputStream(imageByte);
            image = ImageIO.read(bis);
            
            int heightOriginal=image.getHeight();
            int heightFinal=(int)((double)size/(double)image.getWidth()*heightOriginal);
            
            Image newimg = image.getScaledInstance(size, heightFinal,  java.awt.Image.SCALE_SMOOTH); // scale it the smooth way
            bis.close();
            return new ImageIcon(newimg);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }        
    }
    
    
    public static ImageIcon urlToImageIcon(String url, int size) {
    	
    	try {
			ImageIcon imageIcon = new ImageIcon(new URL(url));
			Image image = imageIcon.getImage(); // transform it 
			Image newimg = image.getScaledInstance(size, size,  java.awt.Image.SCALE_SMOOTH); // scale it the smooth way  
			imageIcon = new ImageIcon(newimg);  // transform it back
			return imageIcon;

		} catch (MalformedURLException e) {
			// TODO Auto-generated catch block
			System.out.println(e.getMessage());
			System.out.println(url);
			return null;
		}
    }
    
    public static String dataUriFromImageFile(String imagePath) throws IOException
	{
		Path path = Paths.get(imagePath);
		byte[] data = Files.readAllBytes(path);
		String base64bytes = org.apache.commons.codec.binary.Base64.encodeBase64String(data);
		String src = "data:image/png;base64," + base64bytes;
		return src;
	}
	
    
    public static String convertImageToBase64(String url) {
		String imgURL=null;
		try {
			String base64 = null;
			
			BufferedInputStream bis = new BufferedInputStream(new URL(url).openConnection().getInputStream());
			byte[] imageBytes = IOUtils.toByteArray(bis);
			base64 = Base64.getEncoder().encodeToString(imageBytes);
			
			//need to add this or img url won't work (TMM):
			imgURL="data:image/png;base64, "+base64;
			
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		return imgURL;
	}
    
    
	/**
	 * Write image to file
	 * 
	 * @param ac
	 * @param filepath
	 * @throws IOException
	 * @throws CDKException
	 */
	public static void writeImageFile(IAtomContainer ac, String filepath) throws IOException, CDKException {
		new DepictionGenerator().withAtomColors().withZoom(1.5).depict(ac).writeTo(filepath);
	}
	
	/**
	 * Write image to byte array
	 * 
	 * @param ac
	 * @return
	 * @throws IOException
	 * @throws CDKException
	 */
	public static byte[] writeImageBytes(IAtomContainer ac) throws IOException, CDKException {
		ByteArrayOutputStream baos=new ByteArrayOutputStream();
		new DepictionGenerator().withAtomColors().withZoom(1.5).depict(ac).writeTo(Depiction.PNG_FMT,baos);
		return baos.toByteArray();
	}
	
	/**
	 * Creates image url so that image can be embedded inside html page using base64 encoded image
	 * 
	 * @param smiles
	 * @return
	 * @throws IOException
	 * @throws CDKException
	 * @throws IndigoException
	 */
	public static String generateImageSrcBase64FromSmiles(String smiles)  {
//		String inchikey = StructureUtil.indigoInchikeyFromSmiles(smiles);
		try {		
		IAtomContainer ac = parser.parseSmiles(smiles);
		byte[] bytes=writeImageBytes(ac);
		
        String base64 = Base64.getEncoder().encodeToString(bytes);//convert to base 64
   		String imgURL="data:image/png;base64, "+base64;
   		return imgURL;
	} catch (Exception ex) {
		return "error making image";
	}	
	}
	
	
	public static byte[] generateImageBytesFromSmiles(String smiles)  {
		try {		
			IAtomContainer ac = parser.parseSmiles(smiles);
//			System.out.println(ac.getAtomCount());
			byte[] bytes=writeImageBytes(ac);
			return bytes;
		} catch (Exception ex) {
			return null;
		}	
	}
	
	public static IAtomContainer generateAtomContainerFromSmiles(String smiles)  {
//		String inchikey = StructureUtil.indigoInchikeyFromSmiles(smiles);
		try {		
			IAtomContainer ac = parser.parseSmiles(smiles);
			return ac;
		} catch (Exception ex) {
			return null;
		}	
		
		
	}
	
	public static void main(String[] args) {
		
		try {
			String smiles="CCCC(Cl)CCCO";
			IAtomContainer ac = parser.parseSmiles(smiles);
			System.out.println(ac.getAtomCount());
			writeImageFile(ac, "bob.png");
			
		} catch (Exception ex) {
			ex.printStackTrace();
		}	
		
	}
}
