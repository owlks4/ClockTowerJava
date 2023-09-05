package com.clockTower.fileUtilities;

import com.clockTower.Utility.Utility;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;

import javax.swing.*;
import java.io.File;
import java.io.IOException;
import java.util.LinkedList;


public class ADOScript    //ADO script. ADT also lives in here
{
    public class ADTJumpTable {
        public LinkedList<Integer> offsets = new LinkedList<Integer>();
    }

    ADTJumpTable jumpTable = new ADTJumpTable();
    public LinkedList<ADOFunction> ADOFunctions = new LinkedList<ADOFunction>();


    public ADOScript(byte[] bytes) {    //creating a single-function ADO script from a byte array
        ADOFunctions.add(new ADOFunction(bytes));
    }

    public ADOScript() {
    }

    public void LoadFromPath(String path) {

        File f = new File(path);

        //first, read the ADT...

        String ADTpath = FilenameUtils.concat(f.getParentFile().getPath(), Utility.GetFileNameWithoutExtension(path)) + ".ADT";
        File fADT = new File(ADTpath);

        jumpTable = new ADTJumpTable();

        byte[] filebytesADT = null;
        try {
            filebytesADT = FileUtils.readFileToByteArray(fADT);
        } catch (IOException e) {
            e.printStackTrace();
        }

        int pos = 0;

        if (fADT.exists()) {
            while (pos < filebytesADT.length) {  //read all the jump offsets out of the ADT file
                jumpTable.offsets.add(ConvertADTOffsetToReal(BitConverter.ToInt32(filebytesADT, pos)));
                pos += 4;
            }
        } else {
            JOptionPane.showMessageDialog(null, "Warning: " + path + " does not have an equivalent ADT file in its directory. ADO parsing cannot proceed.");
            return;
        }

        byte[] filebytes = null;

        try {
            filebytes = FileUtils.readFileToByteArray(f);
        } catch (IOException e) {
            e.printStackTrace();
        }

        //Now read the ADO functions.

        for (int i = 0; i < jumpTable.offsets.size(); i++)   //for each jump point in the ADT, grab that function from the ADO
        {
            ADOFunction newFunction = new ADOFunction();
            newFunction.myIndex = i;

            int len = 0;

            if (i < jumpTable.offsets.size() - 1) {
                //then get length by comparing to the offset of the next one along
                len = jumpTable.offsets.get(i + 1) - jumpTable.offsets.get(i);
            } else {
                //then get length by comparing to EOF
                len = filebytes.length - jumpTable.offsets.get(i);
            }

            pos = jumpTable.offsets.get(i);

            newFunction.filebytes = new byte[len];
            System.arraycopy(filebytes, pos, newFunction.filebytes, 0, len);
            pos += len;

            ADOFunctions.add(newFunction);
        }

    }

    public int ConvertADTOffsetToReal(int input) {          //This function is based heavily on http://punkrockhacker.blogspot.com/2014/08/hacking-clocktower-first-fear.html
        int highbytes = (input & 0xFFFF0000) / 2;
        return highbytes + (input & 0xFFFF);
    }

    public int ConvertRealOffsetToFake(int input) {          //This function is based heavily on http://punkrockhacker.blogspot.com/2014/08/hacking-clocktower-first-fear.html
        int mult = input / 0x8000;
        int shft_val = 0x8000 * mult;

        return input + shft_val;
    }
}

