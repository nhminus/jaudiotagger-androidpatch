/*
 * Entagged Audio Tag library
 * Copyright (c) 2003-2005 Rapha�l Slinckx <raphael@slinckx.net>
 * Copyright (c) 2004-2005 Christian Laireiter <liree@web.de>
 * 
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *  
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301  USA
 */
package org.jaudiotagger.audio.ogg;

import org.jaudiotagger.tag.vorbiscomment.VorbisCommentReader;
import org.jaudiotagger.tag.vorbiscomment.VorbisCommentTag;
import org.jaudiotagger.audio.ogg.util.OggPageHeader;
import org.jaudiotagger.audio.ogg.util.VorbisHeader;
import org.jaudiotagger.audio.ogg.util.VorbisPacketType;
import org.jaudiotagger.audio.exceptions.*;
import org.jaudiotagger.tag.Tag;

import java.io.*;

/**
 * Read Vorbis Tag within ogg
 *
 * Vorbis is the audiostream within an ogg file, Vorbis uses VorbisComments as its tag
 */
public class VorbisTagReader
{

    private VorbisCommentReader vorbisCommentReader = new VorbisCommentReader();

    public Tag read(RandomAccessFile raf) throws CannotReadException, IOException
    {
        //1st page = codec infos
        OggPageHeader pageHeader = OggPageHeader.read (raf);
        //Skip over data to end of page header 1
        raf.seek(raf.getFilePointer() + pageHeader.getPageLength());

        //2nd page = comment, may extend to additional pages or not , may also have decode header
        pageHeader = OggPageHeader.read (raf);

        //Now at start of packets on page 2 , check this is the vorbis comment header 
        byte [] b = new byte[7];
        raf.read(b);
        if(!vorbisCommentReader.isVorbisComentHeader (b))
        {
            throw new CannotReadException("Cannot find comment block (no vorbiscomment header)");
        }


        //Convert the coment raw data which maybe over many pages back into raw packet
        byte[] rawVorbisCommentData = convertToVorbisCommentPacket(pageHeader,raf);

        //Begin tag reading
        VorbisCommentTag tag = vorbisCommentReader.read(rawVorbisCommentData);
        return tag;
    }

    /**
     * The Vorbis Comment may span multiple pages so we we need to identify the pages they contain and then
     * extract the packet data from the pages
     */
    private byte[] convertToVorbisCommentPacket(OggPageHeader startVorbisCommentPage,RandomAccessFile raf)
        throws IOException,CannotReadException
    {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        byte [] b = new byte[startVorbisCommentPage.getPacketList().get(0).getLength()
            - (VorbisHeader.FIELD_PACKET_TYPE_LENGTH + VorbisHeader.FIELD_CAPTURE_PATTERN_LENGTH) ];
        raf.read(b);
        baos.write(b);

        //Because there is at least one other packet (SetupHeaderPacket) this means the Comment Packet has finished
        //on this page so thats all we need and wer can return
        if(startVorbisCommentPage.getPacketList().size()>1)
        {
            return baos.toByteArray();
        }

        //There is only the VorbisComment packet on page if it has completed on this page we can return
        if(!startVorbisCommentPage.isLastPacketIncomplete())
        {
            return baos.toByteArray();
        }

        //The VorbisComment extends to the next page, so should be at end of page already
        //so carry on reading pages until we get to the end of comment
        while(true)
        {
            OggPageHeader nextPageHeader = OggPageHeader.read (raf);
            b = new byte[ nextPageHeader.getPacketList().get(0).getLength()];
            raf.read(b);
            baos.write(b);

            //Because there is at least one other packet (SetupHeaderPacket) this means the Comment Packet has finished
            //on this page so thats all we need and we can return
            if(nextPageHeader.getPacketList().size()>1)
            {
                return baos.toByteArray();
            }

            //There is only the VorbisComment packet on page if it has completed on this page we can return
            if(!nextPageHeader.isLastPacketIncomplete())
            {
                return baos.toByteArray();
            }
        }
    }
}

