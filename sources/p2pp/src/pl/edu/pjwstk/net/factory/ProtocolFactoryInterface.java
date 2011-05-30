package pl.edu.pjwstk.net.factory;

import java.util.Vector;

import pl.edu.pjwstk.net.ProtocolObject;
import pl.edu.pjwstk.types.ExtendedBitSet;

public interface ProtocolFactoryInterface {
	public Vector<ProtocolObject> interpret(byte[] bytes);
	public Vector<ProtocolObject> interpret(ExtendedBitSet ebs);
	public Vector<ProtocolObject> interpret(byte[] bytes, int fromPosition);
	public Vector<ProtocolObject> interpret(ExtendedBitSet ebs, int fromPosition);
	public Vector<ProtocolObject> interpret(byte[] bytes, int fromPosition, int toPosition);
	public Vector<ProtocolObject> interpret(ExtendedBitSet ebs, int fromPosition, int toPosition);
	public Vector<ProtocolObject> interpret(ProtocolObject referencedObject, byte[] bytes);
	public Vector<ProtocolObject> interpret(ProtocolObject referencedObject, ExtendedBitSet ebs);
	public Vector<ProtocolObject> interpret(ProtocolObject referencedObject, byte[] bytes, int fromPosition);
	public Vector<ProtocolObject> interpret(ProtocolObject referencedObject, ExtendedBitSet ebs, int fromPosition);
	public Vector<ProtocolObject> interpret(ProtocolObject referencedObject, byte[] bytes, int fromPosition, int toPosition);
	public Vector<ProtocolObject> interpret(ProtocolObject referencedObject, ExtendedBitSet ebs, int fromPosition, int toPosition);
}
