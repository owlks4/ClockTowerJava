package com.clockTower.fileUtilities;

import com.clockTower.Utility.Utility;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.util.LinkedList;

public class PYXfile    //describes the sprite movement for each frame
{
    public LinkedList<PYXAnimData> PYXAnimations = new LinkedList<PYXAnimData>();
    public String PYXname;

    public class PYXAnimData {
        public LinkedList<PYXFrameData> frames = new LinkedList<PYXFrameData>();
    }

    public class PYXFrameData {     //the position offset is relative to the first frame of the animation. So if a sprite is walking forwards, each subsequent frame will have a position that strays further and further from zero. The position the sprite is left in at the end of the cycle becomes its new 'origin' for the next cycle.
        public short XOffset;     //the highest bit is seemingly not part of the movement, this may be the case for Y movement too
        public short YOffset;
        public short unknown;     //possibly layer?
    }

    public void LoadFromPath(String path) {
        PYXAnimations = new LinkedList<PYXAnimData>();

        File f = new File(path);
        PYXname = Utility.GetFileNameWithoutExtension(path);

        byte[] filebytes = null;
        try {
            filebytes = FileUtils.readFileToByteArray(f);
        } catch (IOException e) {
            e.printStackTrace();
        }

        int pos = 0;

        int animCount = Utility.ReverseEndianUShort(BitConverter.ToUInt16(filebytes, pos));
        pos += 2;

        for (int i = 0; i < animCount; i++) {

            PYXAnimData newAnim = new PYXAnimData();
            int numFrames = Utility.ReverseEndianUShort(BitConverter.ToUInt16(filebytes, pos));
            pos += 2;

            for (int j = 0; j < numFrames; j++) {
                PYXFrameData newFrame = new PYXFrameData();
                newFrame.XOffset = Utility.ReverseEndianShort(BitConverter.ToInt16(filebytes, pos));
                pos += 2;
                newFrame.YOffset = Utility.ReverseEndianShort(BitConverter.ToInt16(filebytes, pos));
                pos += 2;
                newFrame.unknown = Utility.ReverseEndianShort(BitConverter.ToInt16(filebytes, pos));
                pos += 2;
                newAnim.frames.add(newFrame);
            }
            PYXAnimations.add(newAnim);
        }

    }
}