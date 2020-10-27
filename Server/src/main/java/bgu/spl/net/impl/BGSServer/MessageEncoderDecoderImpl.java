/****************************
 * Submitters:
 * Itay Bouganim, 305278384
 * Sahar Vaya, 205583453
 ***************************/
package bgu.spl.net.impl.BGSServer;

import bgu.spl.net.api.MessageEncoderDecoder;
import bgu.spl.net.impl.Messages.*;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;

public class MessageEncoderDecoderImpl implements MessageEncoderDecoder<BaseMessage> {

    //Fields
    private final byte ZERO_BYTE = '\0'; //Represents a null byte
    private final String ENCODING_DECODING_FORMAT = "UTF-8"; //Server byte encoding and decoding format.
    private final int BUFFER_BYTE_SIZE = 1 << 10; //Byte buffer max size
    private final ByteBuffer opCodeBuffer = ByteBuffer.allocate(OpCodes.OPCODE_BYTES); //A ByteBuffer used to buffer decoded messages OpCodes

    private ByteBuffer messageBuffer; //ByteBuffer object used to buffer message decoded
    private int decodeStage; //Helper variable to store current message decoding stage/
    private ByteArrayOutputStream bytesStream; //Stores encoded message byte.

    private OpCodes currentMessageOpCode; //Holds the current read decoded message OpCode
    private BaseMessage currentMessage; //The current decoded message received

    //Constructor
    public MessageEncoderDecoderImpl()
    {
        this.currentMessageOpCode = OpCodes.EMPTY;
        this.currentMessage = null;
        this.bytesStream = new ByteArrayOutputStream();
        messageBuffer = ByteBuffer.allocate(BUFFER_BYTE_SIZE);
        decodeStage = 0;
    }

    @Override
    public BaseMessage decodeNextByte(byte nextByte){
        try {
            //If current message opCode is empty, current byte read is used to initiate message OpCode
            if (currentMessageOpCode == OpCodes.EMPTY)
                initMessageOpCode(nextByte);
            //Else decode the correct message determined by its OpCode
            else switch (currentMessageOpCode) {
                case REGISTER:
                    decodeRegisterMessage(nextByte);
                    break;
                case LOGIN:
                    decodeLoginMessage(nextByte);
                    break;
                case FOLLOW:
                    decodeFollowMessage(nextByte);
                    break;
                case POST:
                    decodePostMessage(nextByte);
                    break;
                case PM:
                    decodePMMessage(nextByte);
                    break;
                case STAT:
                    decodeStatMessage(nextByte);
                    break;
            }
            handleNoContentMessages(); //Handles messages that contain only OpCode with no additional content
            return getAndResetCurrentMessage(); //Return the decoded message if finished decoding, else returns null
        } catch (UnsupportedEncodingException e)
        {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * @return the current decoded message if finished decoding, else returns null
     */
    private BaseMessage getAndResetCurrentMessage()
    {
        BaseMessage message = currentMessage;
        if(currentMessage != null && currentMessage.isCompleted()) {
            currentMessage = null;
            return message;
        }
        else return null;
    }

    /**
     * Adds parameter byte to the OpCode byte buffer until it is full, then resolves current message OpCode to the byte buffer bytes value.
     * @param nextByte the next byte to add to the buffer
     */
    private void initMessageOpCode(byte nextByte) {
        opCodeBuffer.put(nextByte);
        if (!opCodeBuffer.hasRemaining()){
            currentMessageOpCode = OpCodes.valueOf(bytesToShort(getDataFromBuffer(opCodeBuffer)));
        }
    }

    /**
     * Sets the current message for message types that include only OpCode information.
     */
    private void handleNoContentMessages()
    {
        if(currentMessageOpCode == OpCodes.USERLIST) {
            currentMessage = new UserlistMessage();
            resetMessageDecode();
        }
        else if(currentMessageOpCode == OpCodes.LOGOUT) {
            currentMessage = new LogoutMessage();
            resetMessageDecode();
        }
    }

    /**
     * Receives the next byte to decode and decodes a register message.
     * @param nextByte next byte from decoded message
     * @throws UnsupportedEncodingException
     */
    private void decodeRegisterMessage(byte nextByte) throws UnsupportedEncodingException {
        if(currentMessage == null)
            currentMessage = new RegisterMessage();
        if(nextByte != ZERO_BYTE)
            messageBuffer.put(nextByte);
        else {
            decodeStage++;
            String decoded = new String (getDataFromBuffer(messageBuffer), ENCODING_DECODING_FORMAT);
            switch (decodeStage){
                //Stage 1 - set the registering user name
                case 1: ((RegisterMessage) currentMessage).setUsername(decoded);
                    messageBuffer.clear();
                    break;
                //Stage 2 - set the registering user password
                case 2: ((RegisterMessage) currentMessage).setPassword(decoded);
                    resetMessageDecode();
                    break;
            }
        }
    }

    /**
     * Receives the next byte to decode and decodes a login message.
     * @param nextByte next byte from decoded message
     * @throws UnsupportedEncodingException
     */
    private void decodeLoginMessage(byte nextByte) throws UnsupportedEncodingException {
        if(currentMessage == null)
            currentMessage = new LoginMessage();
        if(nextByte != ZERO_BYTE)
            messageBuffer.put(nextByte);
        else {
            decodeStage++;
            String decoded = new String (getDataFromBuffer(messageBuffer), ENCODING_DECODING_FORMAT);
            switch (decodeStage){
                //Stage 1 - set the login user name
                case 1: ((LoginMessage) currentMessage).setUsername(decoded);
                    messageBuffer.clear();
                    break;
                //Stage 2 - set the login user password
                case 2: ((LoginMessage) currentMessage).setPassword(decoded);
                    resetMessageDecode();
                    break;
            }
        }
    }

    /**
     * Receives the next byte to decode and decodes a follow message.
     * @param nextByte next byte from decoded message
     * @throws UnsupportedEncodingException
     */
    private void decodeFollowMessage(byte nextByte) throws UnsupportedEncodingException {
        if(currentMessage == null)
            currentMessage = new FollowMessage();
        switch (decodeStage) {
            //Stage 0 - set the byte determines if this is a Follow\Unfollow message
            case 0: ((FollowMessage) currentMessage).setFollowUnfollow(nextByte == 0);
                decodeStage++;
                break;
            //Stage 1 - Add another additional byte
            case 1: messageBuffer.put(nextByte);
                decodeStage++;
                break;
            //Stage 2 - Add additional byte and set the Message num of users to follow-unfollow
            case 2: messageBuffer.put(nextByte);
                ((FollowMessage) currentMessage).setNumOfUsers(bytesToShort(getDataFromBuffer(messageBuffer)));
                messageBuffer.clear();
                decodeStage++;
                break;
            //Stage 3 - Add bytes and set user name list
            case 3:
                if(((FollowMessage) currentMessage).usernameListNotFull()) {
                    if (nextByte != ZERO_BYTE)
                        messageBuffer.put(nextByte);
                    else { //Zero byte indicates end of a username to add to the list.
                        String username = new String(getDataFromBuffer(messageBuffer), ENCODING_DECODING_FORMAT);
                        ((FollowMessage) currentMessage).addUsernameToFollow(username);
                        messageBuffer.clear();
                        if(!((FollowMessage) currentMessage).usernameListNotFull())
                            resetMessageDecode();
                    }
                }
        }
    }

    /**
     * Receives the next byte to decode and decodes a post message.
     * @param nextByte next byte from decoded message
     * @throws UnsupportedEncodingException
     */
    private void decodePostMessage(byte nextByte) throws UnsupportedEncodingException {
        if(currentMessage == null)
            currentMessage = new PostMessage();
        if(nextByte != ZERO_BYTE)
            messageBuffer.put(nextByte);
        else { //Store message content until reading a Zero byte
            String decoded = new String (getDataFromBuffer(messageBuffer), ENCODING_DECODING_FORMAT);
            ((PostMessage) currentMessage).setContent(decoded);
            resetMessageDecode();
        }
    }

    /**
     * Receives the next byte to decode and decodes a private message.
     * @param nextByte next byte from decoded message
     * @throws UnsupportedEncodingException
     */
    private void decodePMMessage(byte nextByte) throws UnsupportedEncodingException {
        if(currentMessage == null)
            currentMessage = new PMMessage();
        if(nextByte != ZERO_BYTE)
            messageBuffer.put(nextByte);
        else { //Zero byte indicated A delimiter between recipient username and message content
            decodeStage++;
            String decoded = new String (getDataFromBuffer(messageBuffer), ENCODING_DECODING_FORMAT);
            switch (decodeStage){
                //Stage 1 - Set the message recipient user name
                case 1: ((PMMessage) currentMessage).setRecipientUsername(decoded);
                    messageBuffer.clear();
                    break;
                //Stage 2 - Set the message content
                case 2: ((PMMessage) currentMessage).setContent(decoded);
                    resetMessageDecode();
                    break;
            }
        }
    }

    /**
     * Receives the next byte to decode and decodes a user statistics message.
     * @param nextByte next byte from decoded message
     * @throws UnsupportedEncodingException
     */
    private void decodeStatMessage(byte nextByte) throws UnsupportedEncodingException {
        if(currentMessage == null)
            currentMessage = new StatMessage();
        if(nextByte != ZERO_BYTE)
            messageBuffer.put(nextByte);
        else { //Until reading a zero byte, read the username to get statistics for
            String decoded = new String (getDataFromBuffer(messageBuffer), ENCODING_DECODING_FORMAT);
            ((StatMessage) currentMessage).setUsername(decoded);
            resetMessageDecode();
        }
    }

    /**
     * Resets current decoded message details after completed decoding.
     * Resets byte buffers and helper variables.
     */
    private void resetMessageDecode()
    {
        opCodeBuffer.clear();
        messageBuffer.clear();
        currentMessageOpCode = OpCodes.EMPTY;
        currentMessage.complete();
        decodeStage = 0;
    }

    @Override
    public byte[] encode(BaseMessage message) {
        byte[] encodedMessage = null;
        try {
            //Encodes the message by given message OpCode
            switch (message.getOpCode()) {
                case NOTIFICATION:
                    encodedMessage = encodeNotificationMessage((NotificationMessage) message);
                    break;
                case ERROR:
                    encodedMessage = encodeErrorMessage((ErrorMessage) message);
                    break;
                case ACK:
                    encodedMessage = encodeAckMessage((AckMessage) message);
                    break;
            }
        }catch (IOException e)
        {
            e.printStackTrace();
            return new byte[0];
        }
        bytesStream.reset(); //After each message encoding is finished, reset the byteStream with encoded message details
        return encodedMessage; //Returns an array of bytes representing the encoded message.
    }

    /**
     * Encodes a Notification message with given message parameter
     * @param message message to encode
     * @return A byte array representing the message details in {@code ENCODING_DECODING_FORMAT}
     * @throws IOException
     */
    private byte[] encodeNotificationMessage(NotificationMessage message) throws IOException
    {
        //Encoding format - OpCode | PM | PostingUser | ZERO_BYTE | Content | ZERO_BYTE
        bytesStream.write(shortToBytes(message.getOpCode().getShortOpCode()));
        bytesStream.write(message.getNotificationType());
        bytesStream.write(message.getPostingUsername().getBytes(ENCODING_DECODING_FORMAT));
        bytesStream.write(ZERO_BYTE);
        bytesStream.write(message.getContent().getBytes(ENCODING_DECODING_FORMAT));
        bytesStream.write(ZERO_BYTE);
        return bytesStream.toByteArray();
    }

    /**
     * Encodes an Error message with given message parameter
     * @param message message to encode
     * @return A byte array representing the message details in {@code ENCODING_DECODING_FORMAT}
     * @throws IOException
     */
    private byte[] encodeErrorMessage(ErrorMessage message) throws IOException
    {
        //Encoding format - OpCode | ErrorMessageOpCode
        bytesStream.write(shortToBytes(message.getOpCode().getShortOpCode()));
        bytesStream.write(shortToBytes(message.getErrorMessageOpCode().getShortOpCode()));
        return bytesStream.toByteArray();
    }

    /**
     * Encodes a Acknowledgement message with given message parameter
     * @param message message to encode
     * @return A byte array representing the message details in {@code ENCODING_DECODING_FORMAT}
     * @throws IOException
     */
    private byte[] encodeAckMessage(AckMessage message) throws IOException
    {
        //Encoding format - OpCode | AckMessageOpCode | OptinalInfo
        bytesStream.write(shortToBytes(message.getOpCode().getShortOpCode()));
        bytesStream.write(shortToBytes(message.getAckMessageOpCode().getShortOpCode()));
        switch (message.getAckType())
        {
            case DEFAULT: break;
            case FOLLOW_ACK:
                //Encoding format - OpCode | AckMessageOpCode | NumOfUsersSucceeded | ListOfUsersSucceededFor
                AckMessage.FollowAckInfo followInfo = (AckMessage.FollowAckInfo) message.getOptionalInfo();
                bytesStream.write(shortToBytes(followInfo.getNumOfUsers()));
                for(String username : followInfo.getUsernameList())
                {
                    bytesStream.write(username.getBytes(ENCODING_DECODING_FORMAT));
                    bytesStream.write(ZERO_BYTE);
                }
                break;
            case STAT_ACK:
                //Encoding format - OpCode | AckMessageOpCode | NumOfPosts | NumOfFollowers | NumFollowing
                AckMessage.StatusAckInfo statusInfo = (AckMessage.StatusAckInfo) message.getOptionalInfo();
                bytesStream.write(shortToBytes(statusInfo.getNumOfPosts()));
                bytesStream.write(shortToBytes(statusInfo.getNumOfFollowers()));
                bytesStream.write(shortToBytes(statusInfo.getNumOfFollowing()));
                break;
            case USERLIST_ACK:
                //Encoding format - OpCode | AckMessageOpCode | NumOfUsersSucceeded | ListOfUsersSucceededFor
                AckMessage.UserlistAckInfo userlistInfo = (AckMessage.UserlistAckInfo) message.getOptionalInfo();
                bytesStream.write(shortToBytes(userlistInfo.getNumOfUsers()));
                for(String username : userlistInfo.getUsernameList())
                {
                    bytesStream.write(username.getBytes(ENCODING_DECODING_FORMAT));
                    bytesStream.write(ZERO_BYTE);
                }
                break;
        }
        return bytesStream.toByteArray();
    }

    /**
     * Retrieves an array of bytes from a byte buffer instance
     * @param buffer the buffer to get information from
     * @return An array of byte representing the content of the byte buffer
     */
    private byte[] getDataFromBuffer(ByteBuffer buffer) {
        buffer.flip();
        byte[] objectBytes = new byte[buffer.limit()];
        buffer.get(objectBytes,0, buffer.limit());
        return objectBytes;
    }

    //Decode 2 bytes to short
    private short bytesToShort(byte[] byteArr)
    {
        short result = (short)((byteArr[0] & 0xff) << 8);
        result += (short)(byteArr[1] & 0xff);
        return result;
    }

    //Encode short to 2 bytes
    private byte[] shortToBytes(short num)
    {
        byte[] bytesArr = new byte[2];
        bytesArr[0] = (byte)((num >> 8) & 0xFF);
        bytesArr[1] = (byte)(num & 0xFF);
        return bytesArr;
    }
}
