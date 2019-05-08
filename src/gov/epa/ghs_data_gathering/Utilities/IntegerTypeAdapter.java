package gov.epa.ghs_data_gathering.Utilities;

import java.io.IOException;

import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import com.google.gson.stream.JsonWriter;

/**
 * Class to fix case in json file where something is null and screws up importing the file to a class
 * 
 * @author Todd Martin
 *
 */
public class IntegerTypeAdapter extends TypeAdapter<Integer>{
	@Override
	public Integer read(JsonReader reader) throws IOException {
		if(reader.peek() == JsonToken.NULL){
			reader.nextNull();
			return null;
		}
		String stringValue = reader.nextString();
		try{
			Integer value = Integer.valueOf(stringValue);
			return value;
		}catch(NumberFormatException e){
			return null;
		}
	}
	@Override
	public void write(JsonWriter writer, Integer value) throws IOException {
		if (value == null) {
			writer.nullValue();
			return;
		}
		writer.value(value);
	}
}


