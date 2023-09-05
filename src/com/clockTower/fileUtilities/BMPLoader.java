/*****
 *
 * BMPLoader.cs
 *
 * This is a simple implementation of a BMP file loader for Unity3D.
 * Formats it should support are:
 *  - 1 bit monochrome indexed
 *  - 2-8 bit indexed
 *  - 16 / 24 / 32 bit color (including "BI_BITFIELDS")
 *  - RLE-4 and RLE-8 support has been added.
 *
 * Unless the type is "BI_ALPHABITFIELDS" the loader does not interpret alpha
 * values by default, however you can set the "ReadPaletteAlpha" setting to
 * true to interpret the 4th (usually "00") value as alpha for indexed BufferedImages.
 * You can also set "ForceAlphaReadWhenPossible" to true so it will interpret
 * the "left over" bits as alpha if there are any. It will also force to read
 * alpha from a palette if it's an indexed BufferedImage, just like "ReadPaletteAlpha".
 *
 * It's not tested well to the bone, so there might be some errors somewhere.
 * However I tested it with 4 different BufferedImages created with MS Paint
 * (1bit, 4bit, 8bit, 24bit) as those are the only formats supported.
 *
 * 2017.02.05 - first version
 * 2017.03.06 - Added RLE4 / RLE8 support
 *
 * Copyright (c) 2017 Markus Göbel (Bunny83)
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to
 * deal in the Software without restriction, including without limitation the
 * rights to use, copy, modify, merge, publish, distribute, sublicense, and/or
 * sell copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING
 * FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS
 * IN THE SOFTWARE.
 *
 *****/

package com.clockTower.fileUtilities;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

public class BMPLoader {
    public enum BMPComressionMode {
        BI_RGB,
        BI_RLE8,
        BI_RLE4,
        BI_BITFIELDS,
        BI_JPEG,
        BI_PNG,
        BI_ALPHABITFIELDS,
        dummy_7,
        dummy_8,
        dummy_9,
        dummy_A,
        BI_CMYK,
        BI_CMYKRLE8,
        BI_CMYKRLE4

	}

    public boolean ReadPaletteAlpha = false;
    public boolean ForceAlphaReadWhenPossible = false;

    public short[] awfulTransparencyPosition = new short[]{-1};

    public BufferedImage LoadBMP(String aFileName) {
        if (aFileName.toUpperCase().contains("PW.") || aFileName.toUpperCase().contains("PB.")) {
            awfulTransparencyPosition = new short[]{0x58, 0x7B, 0xB0, 0xDE, 0x5A};
        } else {
            switch (FilenameUtils.getName(aFileName.toUpperCase())) {
                case "BMA_01M.BMP":
                case "DMA_01MC.BMP":
                case "DMA_02M.BMP":
                case "DMA_02MC.BMP":
                case "DMA_2M.BMP":
                case "DMA_03M.BMP":
                case "DMA_03MC.BMP":
                case "DMA_04M.BMP":
                case "DMA_04MC.BMP":
                    awfulTransparencyPosition = new short[]{0xBA};
                    break;
                case "CTT_1.BMP":
                case "CTT_2.BMP":
                    awfulTransparencyPosition = new short[]{0xFE};
                    break;
                case "AME0.BMP":
                case "AME1.BMP":
                case "AME2.BMP":
                case "AME2_0.BMP":
                case "AME2_1.BMP":
                case "AME2_2.BMP":
                case "AME2_3.BMP":
                case "AME2_4.BMP":
                case "AME2_5.BMP":
                case "AME2_6.BMP":
                case "AME3.BMP":
                case "AME4.BMP":
                case "AME5.BMP":
                case "AME6.BMP":
                    awfulTransparencyPosition = new short[]{176};
                    break;
                case "C_07_0.BMP":
                case "C_07_1.BMP":
                case "C_07_2.BMP":
                case "C_07_3.BMP":
                    awfulTransparencyPosition = new short[]{173};
                    break;
                case "KANA1.BMP":
                case "KANA2.BMP":
                case "MOJI_C1.BMP":
                case "MOJI_C2.BMP":
                case "MOJI_L.BMP":
                case "MOJI_L2.BMP":
                case "MOJI_O1.BMP":
                case "MOJI_O2.BMP":
                case "MOJI_K.BMP":
                case "MOJI_K2.BMP":
                case "MOJI_T.BMP":
                case "MOJI_T2.BMP":
                case "MOJI_W.BMP":
                case "MOJI_W2.BMP":
                case "MOJI_E.BMP":
                case "MOJI_E2.BMP":
                case "MOJI_R.BMP":
                case "MOJI_R2.BMP":
                case "OYAJI.BMP":
                    awfulTransparencyPosition = new short[]{10};
                    break;
                case "STAFF.BMP":
                    awfulTransparencyPosition = new short[]{0};
                    break;
                case "S_CLEAR.BMP":
                case "A_CLEAR.BMP":
                case "B_CLEAR.BMP":
                case "C_CLEAR.BMP":
                case "D_CLEAR.BMP":
                case "E_CLEAR.BMP":
                case "F_CLEAR.BMP":
                case "G_CLEAR.BMP":
                case "H_CLEAR.BMP":
                case "S_LIST.BMP":
                case "A_LIST.BMP":
                case "B_LIST.BMP":
                case "C_LIST.BMP":
                case "D_LIST.BMP":
                case "E_LIST.BMP":
                case "F_LIST.BMP":
                case "G_LIST.BMP":
                case "H_LIST.BMP":
                    awfulTransparencyPosition = new short[]{0x8E};
                    break;
                default:
                    //System.out.println("Tried to use awfulTransparencyMode, but the file "+Path.GetFileName(aFileName.ToUpper())+" is not in the switch statement");
                    break;
            }
        }

        try {
            return ImageIO.read(new File(aFileName));
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }


    public Color[] GetBMPPalette(String aFileName, boolean readAlpha) {

        byte[] filebytes = null;
        int pos = 0;

        try {
            filebytes = FileUtils.readFileToByteArray((new File(aFileName)));
        } catch (IOException e) {
            e.printStackTrace();
        }

        short magic = BitConverter.ToInt16(filebytes, pos);
        pos += 2;

        if (magic != 0x4D42) {
            JOptionPane.showMessageDialog(null, "Incorrect BMP file magic in: " + aFileName);
        }

        pos = 0x0E;

        int infoHeaderEnd = pos + BitConverter.ToInt32(filebytes, pos);
        pos += 4;

        pos += 4; //width
        pos += 4; //height
        pos += 2; //num colour planes
        short bpp = BitConverter.ToInt16(filebytes, pos);
        pos += 2;
        pos += 4; //compression method
        pos += 4; //raw image size
        pos += 4; //pixels per metre x
        pos += 4; //pixels per metre y
        int numColours = BitConverter.ToInt32(filebytes, pos);
        pos += 4;
        int numImportantColours = BitConverter.ToInt32(filebytes, pos);
        pos += 4;

        pos = infoHeaderEnd;

        if (numColours == 0) {
            numColours = 256;
        }

        if (numImportantColours == 0) {
            numImportantColours = numColours;
        }

        Color[] palette = new Color[numColours];

        for (int i = 0; i < numColours; i++) {

            int B = filebytes[pos] & 0xFF;
            pos++;
            int G = filebytes[pos] & 0xFF;
            pos++;
            int R = filebytes[pos] & 0xFF;
            pos++;
            int A = filebytes[pos] & 0xFF;
            pos++;

            if (!readAlpha) {
                A = 255;
            }

            palette[i] = new Color(R, G, B, A);
        }

        return palette;
    }
}