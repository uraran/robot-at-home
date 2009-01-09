import java.lang.ProcessBuilder;

public class UsbController {

	public static void main(String[] args){
		char c = ' ';
		while(true){
			try{
				c=(char)System.in.read();
			}catch(Exception e){}
			if(c=='w')forward();
			else if(c=='a')turn_left();
			else if(c=='d')turn_right();
			else if(c=='s')backward();
			else if(c=='q')stop();
			wait(200);
		}
	}

	public static boolean stop(){return setMotors(0,0);}
	public static boolean forward(){return setMotors(2,1);}
	public static boolean backward(){return setMotors(1,2);}
	public static boolean turn_left(){return setMotors(1,1);}
	public static boolean turn_right(){return setMotors(2,2);}
	
	public static void wait(int ms){try{Thread.sleep(ms);}catch(Exception e){}}
	
	public static String sanitize(String s){
		String ok = "abcdefghijklmnopqrstuvwxyzäöü";
		ok += ok.toUpperCase();
		ok += "ß1234567890+-_.!?,;";
		return sanitize(s,ok);
	}
	public static String sanitize(String s, String ok){
		String filtered="";
		for(int i=0;i<s.length();i++)
			if(ok.indexOf(s.charAt(i))!=-1)
				filtered+=s.charAt(i);
		return filtered;
	}

	public static void speak(String text){ speak(text,"de","m5"); }
	public static void speak(String text, String lang, String gender){		
		text = sanitize(text);
		lang = sanitize(lang);
		gender = sanitize(gender);
		try{
			ProcessBuilder pb = new ProcessBuilder("espeak","-v"+lang+"+"+gender,"-p20","-s120","-a185",text);
			Process p = pb.start();
		}catch(Exception e){e.printStackTrace();}
	}	

	//0 = stop, 1 = turn right, 2 = turn left
	private static boolean setMotors(int lm, int rm){
		if(lm<0||lm>2||rm<0||rm>2){sendRawCommand(0);return false;}
		int cmd = rm<<2 | lm;
		return sendRawCommand(cmd);
	}

	private static boolean sendRawCommand(int cmd){
		try{
			ProcessBuilder pb = new ProcessBuilder("./ctllauncher", ""+cmd);
			Process p = pb.start();
			p.waitFor();
			return p.exitValue()==0;
		}catch(Exception e){}
		return false;
	}

	
}
