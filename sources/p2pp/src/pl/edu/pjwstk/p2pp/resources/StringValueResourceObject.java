package pl.edu.pjwstk.p2pp.resources;

import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;

import pl.edu.pjwstk.p2pp.objects.ResourceObject;
import pl.edu.pjwstk.p2pp.objects.ResourceObjectValue;
import pl.edu.pjwstk.p2pp.util.P2PPUtils;

/**
 * StringValue resource object wasn't defined in P2PP specification (draft 01), but is used in reference implementation.
 * 
 * @author Maciej Skorupka s3874@pjwstk.edu.pl
 * 
 */
public class StringValueResourceObject extends ResourceObject {

	/**
	 * Creates empty StringValueResourceObject.
	 */
	public StringValueResourceObject() {
		super(P2PPUtils.STRING_VALUE_CONTENT_TYPE, (byte) 0);
	}

	/**
	 * Creates StringValueResourceObject. To be filled later with ResourceID and owner. Given key and value are have to
	 * be encoded using UTF-8 encoding (this encoding is used when converting between string and byte array).
	 * 
	 * @param unhashedKey
	 * @param value
	 */
	public StringValueResourceObject(String unhashedKey, String value) {
		super(P2PPUtils.STRING_VALUE_CONTENT_TYPE, (byte) 0);
		try {
			this.unhashedID = unhashedKey.getBytes("UTF-8");
			this.value = new ResourceObjectValue(value.getBytes("UTF-8"));
		} catch (UnsupportedEncodingException e) {
			// probably won't be thrown because UTF-8 is supported everywhere
			e.printStackTrace();
		}
	}

	@Override
	public String getValueAsString() {
		return new String(value.getValue(), Charset.forName("UTF-8"));
	}
}
