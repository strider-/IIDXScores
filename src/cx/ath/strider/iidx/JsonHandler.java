package cx.ath.strider.iidx;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringWriter;

public abstract class JsonHandler {
	protected String getJsonString(InputStream stream) throws IOException {
		StringWriter writer = new StringWriter();
		char[] buffer = new char[0x1000];
		
		try {
			BufferedReader reader = new BufferedReader(new InputStreamReader(stream, "UTF-8"));
			int r;
			while((r = reader.read(buffer, 0, buffer.length)) != -1)
				writer.write(buffer, 0, r);
		} finally {
			stream.close();
		}
		
		return writer.toString();
	}
}
