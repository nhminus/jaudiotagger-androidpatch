package org.jaudiotagger.issues;

import org.jaudiotagger.AbstractTestCase;
import org.jaudiotagger.audio.AudioFile;
import org.jaudiotagger.audio.AudioFileIO;
import org.jaudiotagger.logging.ErrorMessage;
import org.jaudiotagger.tag.FieldKey;
import org.jaudiotagger.tag.TagField;
import org.jaudiotagger.tag.TagOptionSingleton;
import org.jaudiotagger.tag.id3.AbstractID3v2Frame;
import org.jaudiotagger.tag.id3.AbstractID3v2Tag;
import org.jaudiotagger.tag.id3.ID3v23Tag;
import org.jaudiotagger.tag.id3.ID3v24Tag;
import org.jaudiotagger.tag.id3.framebody.FrameBodyTCON;
import org.jaudiotagger.tag.mp4.Mp4FieldKey;
import org.jaudiotagger.tag.mp4.Mp4Tag;
import org.jaudiotagger.tag.reference.ID3V2Version;

import java.io.File;
import java.util.List;

/**
 * Test
 */
public class Issue173Test extends AbstractTestCase
{
    public void testMp4GenresUsingGenericInterface()
    {
        try
        {
            System.out.println(TagOptionSingleton.getInstance().isWriteMp4GenresAsText());
            AudioFile mp4File = null;
            Mp4Tag tag = null;
            File testFile = AbstractTestCase.copyAudioToTmp("01.m4a");
            mp4File = AudioFileIO.read(testFile);
            tag = (Mp4Tag) mp4File.getTag();
            //Set valid value
            tag.setField(FieldKey.GENRE, "Rock");
            //mapped correctly otherwise would not be value for Mp4Fieldkey
            assertEquals("Rock", tag.getFirst(FieldKey.GENRE));
            assertEquals("Rock", tag.getFirst(Mp4FieldKey.GENRE));
            assertEquals("", tag.getFirst(Mp4FieldKey.GENRE_CUSTOM));
            mp4File.commit();

            //Rereads as value
            mp4File = AudioFileIO.read(testFile);
            tag = (Mp4Tag) mp4File.getTag();
            assertEquals("Rock", tag.getFirst(FieldKey.GENRE));
            assertEquals("Rock", tag.getFirst(Mp4FieldKey.GENRE));
            assertEquals("", tag.getFirst(Mp4FieldKey.GENRE_CUSTOM));

            //Set Genre using integer
            tag.setField(FieldKey.GENRE, "1");
            //Read back as integer
            //TODO should read back as Blues here I think
            assertEquals("1", tag.getFirst(FieldKey.GENRE));
            assertEquals("1", tag.getFirst(Mp4FieldKey.GENRE));
            mp4File.commit();
            //On fresh reread shows as mapped value
            mp4File = AudioFileIO.read(testFile);
            tag = (Mp4Tag) mp4File.getTag();
            assertEquals("Classic Rock", tag.getFirst(FieldKey.GENRE));
            assertEquals("Classic Rock", tag.getFirst(Mp4FieldKey.GENRE));

            //Set value that can only be stored as a custom
            //because using generic interface the genre field is removed automtically
            tag.setField(FieldKey.GENRE, "FlapFlap");
            //mapped correctly otherwise would not be value for Mp4Fieldkey
            assertEquals("FlapFlap", tag.getFirst(FieldKey.GENRE));
            assertEquals("FlapFlap", tag.getFirst(Mp4FieldKey.GENRE_CUSTOM));
            assertEquals("", tag.getFirst(Mp4FieldKey.GENRE));
            mp4File.commit();
            assertEquals("FlapFlap", tag.getFirst(FieldKey.GENRE));
            assertEquals("FlapFlap", tag.getFirst(Mp4FieldKey.GENRE_CUSTOM));
            assertEquals("", tag.getFirst(Mp4FieldKey.GENRE));

            tag.setField(FieldKey.GENRE, "Rock");
            //mapped correctly otherwise would not be value for Mp4Fieldkey
            assertEquals("Rock", tag.getFirst(FieldKey.GENRE));
            assertEquals("", tag.getFirst(Mp4FieldKey.GENRE_CUSTOM));
            assertEquals("Rock", tag.getFirst(Mp4FieldKey.GENRE));
            mp4File.commit();
            assertEquals("Rock", tag.getFirst(FieldKey.GENRE));
            assertEquals("", tag.getFirst(Mp4FieldKey.GENRE_CUSTOM));
            assertEquals("Rock", tag.getFirst(Mp4FieldKey.GENRE));

            //Always use custom
            TagOptionSingleton.getInstance().setWriteMp4GenresAsText(true);
            tag.setField(FieldKey.GENRE, "Rock");
            //mapped correctly otherwise would not be value for Mp4Fieldkey
            assertEquals("Rock", tag.getFirst(FieldKey.GENRE));
            assertEquals("Rock", tag.getFirst(Mp4FieldKey.GENRE_CUSTOM));
            assertEquals("", tag.getFirst(Mp4FieldKey.GENRE));
            mp4File.commit();
            assertEquals("Rock", tag.getFirst(FieldKey.GENRE));
            assertEquals("Rock", tag.getFirst(Mp4FieldKey.GENRE_CUSTOM));
            assertEquals("", tag.getFirst(Mp4FieldKey.GENRE));
        }
        catch (Exception ex)
        {
            ex.printStackTrace();
        }
    }

    public void testMp4GenresUsingMp4Interface()
    {
        try
        {
            System.out.println(TagOptionSingleton.getInstance().isWriteMp4GenresAsText());
            AudioFile mp4File = null;
            Mp4Tag tag = null;
            File testFile = AbstractTestCase.copyAudioToTmp("01.m4a");
            mp4File = AudioFileIO.read(testFile);
            tag = (Mp4Tag) mp4File.getTag();
            //Set valid value
            tag.setField(Mp4FieldKey.GENRE, "Rock");
            //mapped correctly otherwise would not be value for Mp4Fieldkey
            assertEquals("Rock", tag.getFirst(FieldKey.GENRE));
            assertEquals("Rock", tag.getFirst(Mp4FieldKey.GENRE));
            //Doesnt remove CUSTOM field as we are using mp4 interface
            assertEquals("Genre", tag.getFirst(Mp4FieldKey.GENRE_CUSTOM));
            mp4File.commit();
            assertEquals("Rock", tag.getFirst(FieldKey.GENRE));
            assertEquals("Rock", tag.getFirst(Mp4FieldKey.GENRE));
            //Doesnt remove CUSTOM field as we are using mp4 interface
            assertEquals("Genre", tag.getFirst(Mp4FieldKey.GENRE_CUSTOM));
        }
        catch (Exception ex)
        {
            ex.printStackTrace();
        }
    }

    public void testMp4InvalidGenresUsingMp4Interface()
    {
        try
        {
            System.out.println(TagOptionSingleton.getInstance().isWriteMp4GenresAsText());
            AudioFile mp4File = null;
            Mp4Tag tag = null;
            File testFile = AbstractTestCase.copyAudioToTmp("01.m4a");
            mp4File = AudioFileIO.read(testFile);
            tag = (Mp4Tag) mp4File.getTag();
            //Set valid value
            tag.setField(Mp4FieldKey.GENRE, "Rocky");
        }
        catch (Exception ex)
        {
            assertTrue(ex instanceof IllegalArgumentException);
            assertTrue(ex.getMessage().equals(ErrorMessage.NOT_STANDARD_MP$_GENRE.getMsg()));
        }
    }

    public void testMp3ID3v24sGenresUsingGenericInterface()
    {
        try
        {
            TagOptionSingleton.getInstance().setID3V2Version(ID3V2Version.ID3_V24);
            System.out.println(TagOptionSingleton.getInstance().isWriteMp4GenresAsText());
            AudioFile mp3File = null;
            ID3v24Tag tag = null;
            File testFile = AbstractTestCase.copyAudioToTmp("01.mp3");
            mp3File = AudioFileIO.read(testFile);
            mp3File.getTagOrCreateAndSetDefault();
            tag = (ID3v24Tag) mp3File.getTag();

            //Set string  representation of standard value
            tag.setField(FieldKey.GENRE, "Rock");
            assertEquals("Rock",tag.getFirst(FieldKey.GENRE));
            FrameBodyTCON body = (FrameBodyTCON)((AbstractID3v2Frame)tag.getFrame("TCON")).getBody();
            assertEquals("17",body.getText());

            //Set Integral value directly, gets converted
            tag.setField(FieldKey.GENRE, "1");
            assertEquals("Classic Rock",tag.getFirst(FieldKey.GENRE));
            body = (FrameBodyTCON)((AbstractID3v2Frame)tag.getFrame("TCON")).getBody();
            assertEquals("1",body.getText());

            //Set Invalid Integral value directly,taken literally
            tag.setField(FieldKey.GENRE, "250");
            assertEquals("250",tag.getFirst(FieldKey.GENRE));
            body = (FrameBodyTCON)((AbstractID3v2Frame)tag.getFrame("TCON")).getBody();
            assertEquals("250",body.getText());

            tag.setField(FieldKey.GENRE, "Rock");
            tag.addField(FieldKey.GENRE, "Musical");
            assertEquals("Rock",tag.getFirst(FieldKey.GENRE));
            assertEquals("Rock",tag.getValue(FieldKey.GENRE, 0));
            assertEquals("Musical",tag.getValue(FieldKey.GENRE, 1));
            body = (FrameBodyTCON)((AbstractID3v2Frame)tag.getFrame("TCON")).getBody();
            assertEquals("17\u000077",body.getText());
            tag.setField(FieldKey.GENRE, "1");
            tag.addField(FieldKey.GENRE,"2");
            assertEquals("Classic Rock",tag.getFirst(FieldKey.GENRE));
            assertEquals("Classic Rock",tag.getValue(FieldKey.GENRE, 0));
            assertEquals("Country",tag.getValue(FieldKey.GENRE, 1));
            List<String> results = tag.getAll(FieldKey.GENRE);
            assertEquals("Classic Rock",results.get(0));
            assertEquals("Country", results.get(1));
            body = (FrameBodyTCON)((AbstractID3v2Frame)tag.getFrame("TCON")).getBody();
            assertEquals("1\u00002",body.getText());
            mp3File.commit();
            mp3File = AudioFileIO.read(testFile);
            tag = (ID3v24Tag) mp3File.getTag();
            results = tag.getAll(FieldKey.GENRE);
            assertEquals("Classic Rock",results.get(0));
            assertEquals("Country",results.get(1));
            body = (FrameBodyTCON)((AbstractID3v2Frame)tag.getFrame("TCON")).getBody();
            assertEquals("1\u00002",body.getText());
        }
        catch (Exception ex)
        {
            ex.printStackTrace();
        }
    }

}
