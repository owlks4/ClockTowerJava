package com.clockTower.fileUtilities;

import com.clockTower.Utility.Utility;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.util.LinkedList;

public class PTNfile    //describes how long each frame should be displayed for, and also something else
{
    public LinkedList<PTNAnimData> PTNAnimations = new LinkedList<PTNAnimData>();
    public String PTNname;

    public class PTNAnimData {
        public LinkedList<PTNFrameData> frames = new LinkedList<PTNFrameData>();
    }

    public class PTNFrameData {
        public int unknown;  //one would think, the index of the frame to play next. But if that's the case, it's seemingly ignored? Maybe it has a use for ADO commands, e.g. if you wanted to pick a frame with a certain ID?
        public int duration;
    }

    public void LoadFromPath(String path) {
        PTNAnimations = new LinkedList<PTNAnimData>();

        File f = new File(path);
        PTNname = Utility.GetFileNameWithoutExtension(path);

        byte[] filebytes = null;
        try {
            filebytes = FileUtils.readFileToByteArray(f);
        } catch (IOException e) {
            e.printStackTrace();
        }

        int pos = 0;

        while (BitConverter.ToUInt16(filebytes, pos) != 0xFFFF) {

            PTNAnimData newAnim = new PTNAnimData();
            int numFrames = Utility.ReverseEndianUShort(BitConverter.ToUInt16(filebytes, pos));
            pos += 2;

            for (int j = 0; j < numFrames; j++) {
                PTNFrameData newFrame = new PTNFrameData();
                newFrame.unknown = Utility.ReverseEndianUShort(BitConverter.ToUInt16(filebytes, pos));
                pos += 2;
                newFrame.duration = Utility.ReverseEndianUShort(BitConverter.ToUInt16(filebytes, pos));
                pos += 2;
                newAnim.frames.add(newFrame);
            }
            PTNAnimations.add(newAnim);
        }


    }
}