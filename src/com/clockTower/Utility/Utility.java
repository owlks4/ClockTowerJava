package com.clockTower.Utility;

import java.awt.Color;
import java.io.File;
import java.nio.charset.StandardCharsets;
import java.text.Normalizer;
import java.util.LinkedList;

import org.apache.commons.io.FilenameUtils;

import com.clockTower.CT.CTSprite;
import com.clockTower.fileUtilities.*;

    public class Utility
    {
        static String[] vanillaAnimationNames = new String[] { "pattern\\j_1_", "pattern\\j_2_", "pattern\\j_3_", "pattern\\j_4_", "pattern\\j_5_", "pattern\\j_6_", "pattern\\A_1_", "pattern\\d_1_", "pattern\\i_1_", "pattern\\k_1_", "pattern\\k_2_", "pattern\\l_3_", "pattern\\m_1_", "pattern\\O_1_", "pattern\\O_2_", "pattern\\R_1_", "pattern\\T_1_", "pattern\\T_2_", "pattern\\T_3_", "pattern\\T_4_", "pattern\\D_2_", "pattern\\O_4_", "pattern\\O_5_", "pattern\\O_6_", "pattern\\O_7_", "pattern\\O_8_", "pattern\\O_9_", "pattern\\O_a_", "pattern\\O_b_", "pattern\\O_c_", "pattern\\O_d_", "pattern\\O_e_", "pattern\\O_f_", "pattern\\O_g_", "pattern\\O_h_", "pattern\\O_i_", "pattern\\O_j_", "pattern\\O_k_", "pattern\\O_m_", "pattern\\O_n_", "pattern\\O_o_", "pattern\\O_p_", "pattern\\O_q_", "pattern\\O_r_", "pattern\\O_s_", "pattern\\O_t_", "pattern\\O_u_", "pattern\\O_v_", "pattern\\O_w_", "pattern\\O_x_", "pattern\\O_y_", "pattern\\O_z_", "pattern\\G_0_", "pattern\\C_1_", "pattern\\K_3_" };
        
        public static String GetAnimationGroupNameFromID(int ID) {  //do not delete, external programs can use this if they need to
            return vanillaAnimationNames[ID];
        }

        public static void AddBigEndianUIntToList(LinkedList<Byte> list, int number) {
            byte[] numberAsBytes = BitConverter.GetBytes(number);

            list.add(numberAsBytes[3]);
            list.add(numberAsBytes[2]);
            list.add(numberAsBytes[1]);
            list.add(numberAsBytes[0]);
        }

        public static void AddBigEndianUShortToList(LinkedList<Byte> list, int i)
        {
            byte[] inputAsBytes = BitConverter.GetBytes(i);

            list.add(inputAsBytes[1]);
            list.add(inputAsBytes[0]);
        }

        public static short ReverseEndianShort(short input){
            return (short)(((input & 0xFF) << 8) | (input >>> 8));
        }
        
        public static int ReverseEndianUShort(int input) {
        	return (((input & 0xFF) << 8) | ((input >>> 8) & 0xFF));
		}
        
    	public static String GetFileNameWithoutExtension(String s) {

    		return FilenameUtils.getName(s).replace("."+FilenameUtils.getExtension(s), "");
    	}

        public static Color GetRGB555Colour(short input) {

            byte A = (byte)(255 - (((input & 0x8000) >>> 15) * 255));
            byte B = (byte)((input & 0x7C00) >>> 7);
            byte G = (byte)((input & 0x03E0) >>> 2);
            byte R = (byte)((input & 0x1F) << 3);

            if (A == 255 && B == 0 && G == 0 && R == 0) {       //this is a special case for colours encoded as 0x0000: they become entirely transparent
                A = 0;
            }
            
            return new Color(R,G,B,A);
        }
        
        public static String IntToStringWithSetNumberOfDigits(int input, int numDigits) {
        	
        	String temp = Integer.toString(input);
        	String output = "";
        	
        	for (int i = temp.length(); i < numDigits; i++) {
        	output += "0";	
        	}
        	
        	return output + temp;
        }
        
        public static LinkedList<File> GetFilesRecursive(File dir, String ext) {
			
			ext = ext.replace(".", "");
			if (ext == null) {
				ext = "";
			}
			LinkedList<File> files = new LinkedList<File>();
			
			for (final File fileEntry : dir.listFiles()) {
		        if (fileEntry.isDirectory()) {
		        	files.addAll(GetFilesRecursive(fileEntry, ext));
		        	}
		        else
		        	{
		        	String[] checkExt = (fileEntry.getName()).split("\\.");
		        	
		        	if (checkExt[checkExt.length-1].equalsIgnoreCase(ext) || ext == "") {
		        		files.add(fileEntry);
		        		}
		        	}
		    	}
		
			return files;
		}
        
        public static float Lerp(float start, float end, float inbetweenPosition) {
        	return start + ((end - start) * inbetweenPosition);
        }

        public static String DecodeShiftJISString(LinkedList<Byte> data)
        {
        	byte[] bytes = new byte[data.size()];
        	
        	for (int i = 0; i < bytes.length; i++) {
        		bytes[i] = data.get(i);
        	}

			return Normalizer.normalize(new String(bytes, StandardCharsets.UTF_8),Normalizer.Form.NFD);
		}

        
        public static int RandomInRange(long min, long max) {	//min inclusive, max inclusive
        	max++; //to make max inclusive, otherwise it wouldn't be
        	//the calculations are done with long just so that it doesn't go past the maximum int value etc during the calculations
            return (int) ((Math.random() * (max - min)) + min);
        }
        
        public static CTSprite GetSpriteWithSlotID(LinkedList<CTSprite> sprites, int slotID) {
        	for (int i = 0; i < sprites.size(); i++) {
        		if (sprites.get(i).mySlotID == slotID) {
        			return sprites.get(i);
        		}
        	}
        	System.out.println("Could not find sprite with slot ID "+slotID+"...");
        	return null;
        }

		public static LinkedList<CTSprite> MoveSpriteToCorrectLayerPosition(LinkedList<CTSprite> list, int currentIndexOfSprite) {
			
			boolean fulfilled = false;
			CTSprite ourSprite = list.get(currentIndexOfSprite);
			int listSize = list.size();
			
			while (!fulfilled) {
				
					if (currentIndexOfSprite > 0 && list.get(currentIndexOfSprite-1).layer > ourSprite.layer) { //swap with the one in the space behind
						CTSprite temp = list.get(currentIndexOfSprite-1);
						currentIndexOfSprite--;
						list.set(currentIndexOfSprite, ourSprite);
						list.set(currentIndexOfSprite+1, temp);
						while (currentIndexOfSprite > 0 && list.get(currentIndexOfSprite-1).mySlotID < ourSprite.mySlotID) { //then sort within the layer by reverse slot ID
							temp = list.get(currentIndexOfSprite-1);
							currentIndexOfSprite--;
							list.set(currentIndexOfSprite, ourSprite);
							list.set(currentIndexOfSprite+1, temp);
							}
						fulfilled = false;
					} else if (currentIndexOfSprite < listSize - 1 && list.get(currentIndexOfSprite+1).layer < ourSprite.layer) { //swap with the one in the space ahead
						CTSprite temp = list.get(currentIndexOfSprite+1);
						currentIndexOfSprite++;
						list.set(currentIndexOfSprite, ourSprite);
						list.set(currentIndexOfSprite-1, temp);
						while (currentIndexOfSprite < listSize - 1 && list.get(currentIndexOfSprite+1).mySlotID > ourSprite.mySlotID) { //then sort within the layer by reverse slot ID
							temp = list.get(currentIndexOfSprite+1);
							currentIndexOfSprite++;
							list.set(currentIndexOfSprite, ourSprite);
							list.set(currentIndexOfSprite-1, temp);
							}
						fulfilled = false;
					} else {
						fulfilled = true;
					}
			}
			return list;
		}

		public static byte[] GetBytes(int input) {
			
			byte[] output = new byte[4];
			output[0] = (byte)(input & 0xFF);
			output[1] = (byte)((input >> 8) & 0xFF);
			output[2] = (byte)((input >> 16) & 0xFF);
			output[3] = (byte)((input >> 24) & 0xFF);
			
			return output;
		}
		
		public static byte[] GetBytes(short input) {
			
			byte[] output = new byte[2];
			output[0] = (byte)(input & 0xFF);
			output[1] = (byte)((input >> 8) & 0xFF);
			
			return output;
		}
    }