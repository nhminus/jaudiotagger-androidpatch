package org.jaudiotagger.tag.mp4;

import org.jaudiotagger.audio.generic.Utils;
import org.jaudiotagger.audio.mp4.util.Mp4BoxHeader;

import java.util.List;
import java.util.ArrayList;
import java.nio.ByteBuffer;
import java.io.UnsupportedEncodingException;

/**
 * Represents the Track No field
 *
 * <p>For some reason uses an array of four numbers, but only the middle two are of use for display purposes
 */
public class Mp4TrackField extends Mp4TagTextNumberField
{
    private static final int NONE_VALUE_INDEX   = 0;
    private static final int TRACK_NO_INDEX     = 1;
    private static final int TRACK_TOTAL_INDEX  = 2;
    private static final int NONE_END_VALUE_INDEX   = 3;

    /**
     * Create new Track Field parsing the String for the trackno/total
     *
     * TODO what should we do if trackValue is invalid
     * 
     * @param trackValue
     */
    public Mp4TrackField(String trackValue)
    {
        super(Mp4FieldKey.TRACK.getFieldName(), trackValue);

        numbers = new ArrayList<Short>();
        numbers.add(new Short("0"));

        String values[] = trackValue.split("/");
        switch(values.length)
        {
            case 1:
                numbers.add(Short.parseShort(values[0]));
                numbers.add(new Short("0"));
                numbers.add(new Short("0"));

            case 2:
                numbers.add(Short.parseShort(values[0]));
                numbers.add(Short.parseShort(values[1]));
                numbers.add(new Short("0"));

            default:
                numbers.add(new Short("0"));
                numbers.add(new Short("0"));
                numbers.add(new Short("0"));
        }
    }


    /**
     * Create new Track Field with only track No
     *
     * @param trackNo
     */
    public Mp4TrackField(int trackNo)
    {
        super(Mp4FieldKey.TRACK.getFieldName(), String.valueOf(trackNo));
        numbers = new ArrayList<Short>();
        numbers.add(new Short("0"));
        numbers.add((short)trackNo);
        numbers.add(new Short("0"));
        numbers.add(new Short("0"));
    }

     /**
     * Create new Track Field with track No and total tracks
     *
     * @param trackNo
     * @param total
     */
    public Mp4TrackField(int trackNo,int total)
    {
        super(Mp4FieldKey.TRACK.getFieldName(), String.valueOf(trackNo));
        numbers = new ArrayList<Short>();
        numbers.add(new Short("0"));
        numbers.add((short)trackNo);
        numbers.add((short)total);
        numbers.add(new Short("0"));
    }

    /**
     * Construct from filedata
     *
     * @param id
     * @param data
     * @throws UnsupportedEncodingException
     */
    public Mp4TrackField(String id, ByteBuffer data) throws UnsupportedEncodingException
    {
        super(id, data);
    }


    protected void build(ByteBuffer data) throws UnsupportedEncodingException
    {
        //Data actually contains a 'Data' Box so process data using this
        Mp4BoxHeader header  = new Mp4BoxHeader(data);
        Mp4DataBox   databox = new Mp4DataBox(header,data);
        dataSize = header.getDataLength();
        numbers  = databox.getNumbers();

        //Track number always hold three values, we can discard the first one, the second one is the track no
        //and the third is the total no of tracks so only use if not zero
        StringBuffer sb = new StringBuffer();
        sb.append(numbers.get(TRACK_NO_INDEX));
        if(numbers.get(TRACK_TOTAL_INDEX )>0)
        {
            sb.append("/"+numbers.get(TRACK_TOTAL_INDEX ));
        }
        content  = sb.toString();
    }
}
