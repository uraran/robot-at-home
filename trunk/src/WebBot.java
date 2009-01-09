import java.net.*;
import java.io.*;

public class WebBot extends Thread {
	Socket c;
	static CamGrabber cg = new CamGrabber();

	public WebBot(Socket s){
		c=s;
		start();
	}

	public static void main(String[]a){
		cg.start();		
		try{
			ServerSocket s=new ServerSocket(8181);
			for(;;){new WebBot(s.accept());}
		}catch(Exception e){}
	}

	public void run(){
		try{
			BufferedReader i=new BufferedReader(new InputStreamReader(c.getInputStream()));
			DataOutputStream o=new DataOutputStream(c.getOutputStream());
			try{
				String s;
				String[] p;
				while((s=i.readLine()).length()>0){
					if(s.startsWith("GET")){
						p=(s.split(" "))[1].split("\\?");
						if(p[0].equals("/cam.jpg"))
							sendData(o, cg.getFrameBytes());
						else
							System.out.println(c.getRemoteSocketAddress()+" : "+s);
						if(p[0].equals("/") || p[0].equals("/index.html"))
							sendGui(o);
						if(p.length<2)continue;
						else if(p[0].equals("/rc") && p[1].equals("q"))
							UsbController.stop();
						else if(p[0].equals("/rc") && p[1].equals("w"))
							UsbController.forward();
						else if(p[0].equals("/rc") && p[1].equals("a"))
							UsbController.turn_left();
						else if(p[0].equals("/rc") && p[1].equals("s"))
							UsbController.backward();
						else if(p[0].equals("/rc") && p[1].equals("d"))
							UsbController.turn_right();
						else if(p[0].equals("/rcs"))
							UsbController.speak(URLDecoder.decode(p[1],"UTF-8"));
					}
				}
			}catch(Exception e){
				e.printStackTrace();
				o.writeBytes("HTTP/1.0 404 ERROR\n\n");
			}
			o.close();
		}catch(Exception e){}
	}

	private void sendData(DataOutputStream o, byte[] d){
		try{		
			o.writeBytes("HTTP/1.0 200 OK\nContent-Length:"+d.length+"\n\n");
			o.write(d,0,d.length);
		}catch(Exception e){e.printStackTrace();}
	}

	private byte[] readFile(String f){
		try{
			int l=(int)new File(f).length();
			byte[]b=new byte[l];
			new FileInputStream(f).read(b);
			return b;
		}catch(Exception e){
			e.printStackTrace();
			return new byte[0];
		}
	}

	private void sendGui(DataOutputStream o){
		System.out.println("Sending gui");
		sendData(o,readFile("webbot.html"));
	}
}
