import java.lang.ProcessBuilder;
import java.lang.SecurityException;
import java.io.InputStreamReader;
import java.io.DataInputStream;
import java.io.ByteArrayInputStream;
import java.io.PrintWriter;
import java.io.File;
import java.util.Vector;
import java.awt.image.BufferedImage;
import javax.imageio.ImageIO;

public class CamGrabber extends Thread{
	static final String DELIMITER = "{{{NEXt}}}";
	static final int DLEN = DELIMITER.length();
	Vector<Byte> buffer = new Vector<Byte>();
	BufferedImage frame = null;
	byte[] framebytes;
	boolean newframe = false;
	boolean running = false;
	
	public CamGrabber(){}

	public boolean newFrame(){
		return newframe;
	}
		
	public BufferedImage getFrame(){
		newframe = false;
		return frame;
	}

	public byte[] getFrameBytes(){
		newframe = false;
		return framebytes;	
	}

	public void interrupt() throws SecurityException{
		running = false;	
	}

	public void run(){
		try{
			ProcessBuilder pb = new ProcessBuilder("./uvccapture","-m","-p1","-t200","-ostdout");
			Process p = pb.start();
			DataInputStream dis = new DataInputStream(p.getInputStream());
			running = true;
			while(running){
				buffer.add(new Byte((byte)dis.readUnsignedByte()));
				int blen = buffer.size();
				if(blen >= DLEN &&
					new String(getVecPart(buffer,blen-DLEN,DLEN)).equals(DELIMITER))
				{
					framebytes = getVecPart(buffer, 0, blen - DLEN);
					frame = ImageIO.read(new ByteArrayInputStream(framebytes));
					buffer.clear();
					newframe = true;
				}
			}
			p.destroy();
		}catch(Exception e){e.printStackTrace();}	
	}

	private byte[] getVecPart(Vector<Byte> vec, int start, int len){
		byte[] part = new byte[len];
		for(int i=0;i<len;i++)
			part[i] = vec.get(start+i);
		return part;
	}

	public static void main(String[] args){
		CamGrabber cg = new CamGrabber();
		cg.start();
		for(int i=0;i<3;i++){
			while(!cg.newFrame());
			saveImage(cg.getFrame(),"img"+i+".jpg");
		}
		cg.interrupt();
	}
	
	public static boolean saveImage(BufferedImage img, String filename){
		try {
	        File outputfile = new File(filename);
	        ImageIO.write(img, "jpg", outputfile);
	        return true;
	    }catch(Exception e){e.printStackTrace();}
	    return false;
	}
}
