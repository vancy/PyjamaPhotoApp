import java.io.*;
import java.util.LinkedList;

import pj.compiler.*;

public class myGenerate extends Object{
	
	public static void main (String[] args) {
		LinkedList<File> list = new LinkedList<File>();
		list.add(new File("src/application/flickr/Search.pj"));
		list.add(new File("src/application/SearchProjectPanel.pj"));
		list.add(new File("src/application/ImageProjectPanel.pj"));
		for (File f: list) {
			try {
				PyjamaToJavaCompiler.compile(f.getAbsolutePath(), "./", pj.compiler.CompileChecker.CompileOption.P2J);			
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

	    System.out.println("Finished tag");
	
	}
	
	public static void compilePJCode() {
		
	}

}
