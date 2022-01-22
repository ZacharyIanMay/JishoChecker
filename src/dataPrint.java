import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Scanner;
import java.util.concurrent.TimeUnit;

import javax.swing.JFileChooser;
import javax.swing.JOptionPane;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

public class dataPrint {

	public static void main(String[] args)
	{
		//Get Directory using JFileChooser
		JOptionPane.showMessageDialog(null, "Please select input.txt");
		JFileChooser dirPicker = new JFileChooser();
		dirPicker.showOpenDialog(null);
		String dir = dirPicker.getSelectedFile().getAbsolutePath();
		dir = dir.substring(0, dir.indexOf("input.txt"));
		
		//Create files if they don't exist, and open a file writer with them
		try
		{
			File fileCreator = new File(dir + "\\output1.txt");
			if(fileCreator.createNewFile())
			{
				System.out.println("File created: " + fileCreator.getName());
			}
			else
			{
				System.out.println("output1.txt already exists");
			}
			fileCreator = new File(dir + "\\output2.txt");
			if(fileCreator.createNewFile())
			{
				System.out.println("File created: " + fileCreator.getName());
			}
			else
			{
				System.out.println("output2.txt already exists");
			}
		}catch(IOException e)
		{
			System.out.println("Error:");
			e.printStackTrace();
		}
		
		//Read info from input.txt and save it to a list
		ArrayList<String> search = new ArrayList<String>();
		try {
			FileReader input = new FileReader(dir + "\\input.txt");
			Scanner scanner = new Scanner(input);
			while(scanner.hasNextLine())
			{
				search.add(scanner.nextLine());
			}
			scanner.close();
		} catch (FileNotFoundException e) {
			System.out.println("Error:");
			e.printStackTrace();
		}
		
		//Get JSON and Parse Data
		ArrayList<String> jsonStrings = new ArrayList<String>();
		ArrayList<String> fails = new ArrayList<String>();
		String urlBase = "https://jisho.org/api/v1/search/words?keyword=";
		for(int i = 0; i < search.size(); i++)
		{
			try {
				//building the url in % encoding
				String jpnEncode = URLEncoder.encode(search.get(i), "UTF-8").replace("+", "%20");
				URL url = new URL(urlBase + jpnEncode);
				System.out.println(url.toString());
				
				//make connection
				HttpURLConnection connection = (HttpURLConnection)url.openConnection();
				connection.setRequestProperty("Accept", "application/json");
				System.out.println(connection.getResponseCode() + " " + connection.getResponseMessage());
				
				Scanner jsonReader = new Scanner(url.openStream());
				String json = "";
				json += jsonReader.nextLine();
				jsonStrings.add(json);
				System.out.println(jsonStrings.size());
				jsonReader.close();
				
				connection.disconnect();
				TimeUnit.MILLISECONDS.sleep(200);
			} catch (IOException | InterruptedException e) {
				e.printStackTrace();
				fails.add(search.get(i));
				jsonStrings.add("");
			}
		}
		
		try
		{
			//Create file writers
			FileWriter writer1 = new FileWriter(dir + "\\output1.txt");
			FileWriter writer2 = new FileWriter(dir + "\\output2.txt");
			
			//Write needed information to new Files using 2 file writers
			for(int i = 0; i < jsonStrings.size(); i++)
			{
				if(jsonStrings.get(i).isEmpty())
				{
					writer1.write("check this\n");
					writer2.write("check this\n");
				}else
				{
					JSONParser converter = new JSONParser();
					JSONObject full = (JSONObject) converter.parse(jsonStrings.get(i));
					JSONArray first = (JSONArray) full.get("data");
					JSONObject partial = (JSONObject) first.get(0);
					JSONArray second = (JSONArray)partial.get("senses");
					full = (JSONObject) second.get(0);
					second = (JSONArray) full.get("parts_of_speech");
					String PoS = (String) second.get(0);
					System.out.println(PoS);
					try
					{
						String transitivity = (String) second.get(1);
						if(transitivity.contains("Transitive"))
						{
							writer2.write("transitive\n");
						}else if(transitivity.contains("Intransitive"))
						{
							writer2.write("intransitive\n");
						}else
						{
							writer2.write("check this\n");
						}
					}
					catch(IndexOutOfBoundsException e)
					{
						String transitivity = "N/A";
						writer2.write(transitivity + "\n");
					}
					if(PoS.contains("Godan"))
					{
						writer1.write("う\n");
					} else if(PoS.contains("Ichidan"))
					{
						writer1.write("る\n");
					} else
					{
						writer1.write("check this\n");
					}
				}
			}
			
			//Close writers
			writer1.close();
			writer2.close();
			
		}catch(IOException | ParseException e)
		{
			System.out.println("Error:");
			e.printStackTrace();
		}
		
		for(int i = 0; i < fails.size(); i++)
		{
			System.out.println(fails.get(i));
		}
		System.out.println("Done");
	}
}
