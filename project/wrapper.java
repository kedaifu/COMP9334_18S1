import java.io.File;
import java.io.FileNotFoundException;
import java.util.Scanner;

public class wrapper {

	public static void main(String[] args) {
		Scanner sc = null;
		
		try{
			
			sc = new Scanner(new File("num_tests.txt"));
			String line = sc.nextLine();
			String[] words = line.split(" ");
			int num = Integer.parseInt(words[0]);
			//System.out.println(num);
			
			Project myProject = new Project(num);
			myProject.outPutFile();
			
		}
		catch (FileNotFoundException e){
			System.out.println(e.getMessage());
		}
		finally{
			if (sc != null) sc.close();
		}

	}

}
