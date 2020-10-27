/****************************
 * Submitters:
 * Itay Bouganim, 305278384
 * Sahar Vaya, 205583453
 ***************************/
package bgu.spl.net.impl.Messages;

import java.util.HashMap;
import java.util.Map;

/**
 * An enum representing all different messages OpCodes
 */
public enum OpCodes {

    EMPTY(0),
    REGISTER(1),
    LOGIN(2),
    LOGOUT(3),
    FOLLOW(4),
    POST(5),
    PM(6),
    USERLIST(7),
    STAT(8),
    NOTIFICATION(9),
    ACK(10),
    ERROR(11);

    private final short value; // the short value of the enum value
    private static Map<Short, OpCodes> opCodes = new HashMap<>(); //Maps short numbers to their corresponding OpCode enum value
    public static final short OPCODE_BYTES = 2; //Represents a constant size for OpCode representation in bytes

    OpCodes(int value)
    {
        this.value = (short) value;
    }

    static {
        for (OpCodes opCode : OpCodes.values()) {
            opCodes.put(opCode.value, opCode);
        }
    }

    /**
     * Retrieves an enum value for given int OpCode value
     * @param type an integer representing the OpCode
     * @return the OpCode enum value for given type int
     */
    public static OpCodes valueOf(int type) {
        return opCodes.get((short)type);
    }

    //Returns the shoet representation of the enum value
    public final short getShortOpCode()
    {
        return this.value;
    }

}
