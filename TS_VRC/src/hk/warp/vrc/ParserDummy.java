package hk.warp.vrc;

import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;

public class ParserDummy extends Parser{
	public static void main(String[] args) throws IOException {
		try {
			new ParserDummy().runIt();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	@Override
	String requestEventAvailability(Event myEvent) {
		try {
			return 	readJson("xxxx_event.json");
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}

	@Override
	String requestMembers() {
		try {
			return 	readJson("xxxx_members.json");
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}

	@Override
	String requestEvents() {
		try {
			return 	readJson("xxxx.json");
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}

	@Override
	void writeCSV(String csv) {
		try {
			FileWriter out = new FileWriter("test.csv");
			out.write(csv);
			out.close();
		} catch (IOException e) {
			e.printStackTrace();
		}

	}
	
	


}
