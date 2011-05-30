package pl.edu.pjwstk.net;

import java.io.InputStream;

import pl.edu.pjwstk.types.ExtendedBitSet;

public interface ProtocolObjectInterface {
	public boolean tryParse(int fromPosition, ExtendedBitSet ebs);
	public boolean tryParse(byte[] bytes);
	public boolean tryParse(InputStream inputStream);
}
