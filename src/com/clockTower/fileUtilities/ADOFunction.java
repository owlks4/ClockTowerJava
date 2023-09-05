package com.clockTower.fileUtilities;

import com.clockTower.Utility.Utility;

import javax.swing.*;
import java.util.LinkedList;


public class ADOFunction {
    public byte[] filebytes = new byte[0];
    public int myIndex;

    public ADOFunction(byte[] bytes) {
        filebytes = bytes;
        myIndex = 0;
    }


    public ADOFunction() {
    }


    public LinkedList<String> ADOFunctionToPlainText() {
        int pos = 0;

        LinkedList<String> output = new LinkedList<String>();
        boolean triggerConditionalNextTime = false;
        int conditionalLayer = 0; //how many nested conditionals are we in right now

        boolean SlightlyResistIndent = false;  //this becomes true for 'IF', 'ELSE' etc so that they get unindented when necessary

        while (pos < filebytes.length) {
            SlightlyResistIndent = false;

            if (filebytes[pos + 1] == 0xFF) {
                switch ((int) filebytes[pos] & 0xFF) {
                    case 0x00: //RETN
                        pos += 2;
                        break;
                    case 0x0A: //DIVIDEVAR
                    {
                        String line = "DivideVariableBy(";
                        pos += 2;
                        line += EvaluateADOInt16(BitConverter.ToInt16(filebytes, pos)) + ", ";
                        pos += 2;
                        line += BitConverter.ToInt16(filebytes, pos) + ");";
                        pos += 2;
                        output.add(line);
                    }
                    break;
                    case 0x0B: //MULTVAR
                    {
                        String line = "MultiplyVariableBy(";
                        pos += 2;
                        line += EvaluateADOInt16(BitConverter.ToInt16(filebytes, pos)) + ", ";
                        pos += 2;
                        line += BitConverter.ToInt16(filebytes, pos) + ");";
                        pos += 2;
                        output.add(line);
                    }
                    break;
                    case 0x0C: //DECVAR   increases the Variable by a certain amount
                    {
                        String line = "DecreaseVariableBy(";
                        pos += 2;
                        line += EvaluateADOInt16(BitConverter.ToInt16(filebytes, pos)) + ", ";
                        pos += 2;
                        line += BitConverter.ToInt16(filebytes, pos) + ");";
                        pos += 2;
                        output.add(line);
                    }
                    break;
                    case 0x0D: //INCVAR   increases the Variable by a certain amount
                    {
                        String line = "IncreaseVariableBy(";
                        pos += 2;
                        line += EvaluateADOInt16(BitConverter.ToInt16(filebytes, pos)) + ", ";
                        pos += 2;
                        line += BitConverter.ToInt16(filebytes, pos) + ");";
                        pos += 2;
                        output.add(line);
                    }
                    break;

                    //E is decrement variable by 1

                    //F is increment variable by 1

                    case 0x10: //SETVAR Set Variable. It's used when you get items, and is used a lot for a massive reset in an early function
                    {
                        String line = "Set_Variable(";
                        pos += 2;
                        line += EvaluateADOInt16(BitConverter.ToInt16(filebytes, pos)) + ", ";
                        pos += 2;
                        line += EvaluateADOInt16(BitConverter.ToInt16(filebytes, pos)) + ");";
                        pos += 2;
                        output.add(line);
                    }
                    break;
                    case 0x11: //EQUAL TO
                    {
                        String line = "Is_Variable_Equal_To(";
                        pos += 2;
                        line += EvaluateADOInt16(BitConverter.ToInt16(filebytes, pos)) + ", ";
                        pos += 2;
                        line += EvaluateADOInt16(BitConverter.ToInt16(filebytes, pos)) + ")";
                        pos += 2;
                        output.add(line);
                    }
                    break;
                    case 0x12: //NOT EQUAL TO
                    {
                        String line = "Is_Variable_NotEqualTo(";
                        pos += 2;
                        line += EvaluateADOInt16(BitConverter.ToInt16(filebytes, pos)) + ", ";
                        pos += 2;
                        line += BitConverter.ToInt16(filebytes, pos) + ")";
                        pos += 2;
                        output.add(line);
                    }
                    break;
                    case 0x13: //GREATER THAN
                    {
                        String line = "Is_Variable_GreaterThan(";
                        pos += 2;
                        line += EvaluateADOInt16(BitConverter.ToInt16(filebytes, pos)) + ", ";
                        pos += 2;
                        line += BitConverter.ToInt16(filebytes, pos) + ")";
                        pos += 2;
                        output.add(line);
                    }
                    break;
                    case 0x14: //LESS THAN
                    {
                        String line = "Is_Variable_LessThan(";
                        pos += 2;
                        line += EvaluateADOInt16(BitConverter.ToInt16(filebytes, pos)) + ", ";
                        pos += 2;
                        line += BitConverter.ToInt16(filebytes, pos) + ")";
                        pos += 2;
                        output.add(line);
                    }
                    break;
                    case 0x15: //GREATER THAN OR EQUAL TO
                    {
                        String line = "Is_Variable_GreaterThanOrEqualTo(";
                        pos += 2;
                        line += EvaluateADOInt16(BitConverter.ToInt16(filebytes, pos)) + ", ";
                        pos += 2;
                        line += BitConverter.ToInt16(filebytes, pos) + ")";
                        pos += 2;
                        output.add(line);
                    }
                    break;
                    case 0x16: //LESS THAN OR EQUAL TO
                    {
                        String line = "Is_Variable_LessThanOrEqualTo(";
                        pos += 2;
                        line += EvaluateADOInt16(BitConverter.ToInt16(filebytes, pos)) + ", ";
                        pos += 2;
                        line += BitConverter.ToInt16(filebytes, pos) + ")";
                        pos += 2;
                        output.add(line);
                    }
                    break;
                    case 0x1F: //UNK_1F (DO?)   //closes the conditions in an IF statement or WHILE statement
                        pos += 2;
                        output.add("DO");
                        SlightlyResistIndent = true;
                        break;
                    case 0x20: //ALL
                        output.add("All();");
                        pos += 2;
                        break;
                    case 0x21: //ALLEND - similar to JMP
                    {
                        String line = "JumpAllEnd(";
                        pos += 2;
                        line += BitConverter.ToInt16(filebytes, pos);
                        pos += 2;
                        output.add(line + ");");
                    }
                    break;
                    case 0x22:  //JMP
                    {
                        String line = "JumpToADTFunction(";
                        pos += 2;
                        line += BitConverter.ToInt16(filebytes, pos) + ");";
                        pos += 2;
                        output.add(line);
                    }
                    break;
                    case 0x23:  //CALL      SEE SETMARK!!!!!!
                    {
                        String line = "Call(";
                        pos += 2;
                        line += EvaluateADOInt16(BitConverter.ToInt16(filebytes, pos)) + ", ";
                        pos += 2;
                        line += BitConverter.ToInt16(filebytes, pos) + ", ";
                        pos += 2;
                        line += BitConverter.ToInt16(filebytes, pos) + ");";
                        pos += 2;
                        output.add(line);
                    }
                    break;
                    case 0x28:  //!!!!!  Message terminator - we won't write anything to the output. On subsequent reimports, it will get appended to the end of a 0xFF33 opcode.
                        pos += 2;
                        break;
                    case 0x29: //""END_IF"" (BUT I THINK IT'S ACTUALLY START_IF)
                    {
                        output.add("");

                        String line = "IF ";
                        pos += 2;
                        pos += 2;  //ID of the if statement to start (counts from 1, independent from the 'while' ID)
                        short StatementType = BitConverter.ToInt16(filebytes, pos);
                        pos += 2;

                        switch (StatementType) {
                            case 1:
                                line += "AND";
                                break;
                            case 2:
                                line += "OR";
                                break;
                            default:
                                JOptionPane.showMessageDialog(null, "Unhandled IF statement type: " + StatementType);
                                break;
                        }

                        output.add(line);

                        triggerConditionalNextTime = true;
                    }
                    break;
                    case 0x2A: //WHILE
                    {
                        output.add("");

                        String line = "WHILE";
                        pos += 2;

                        pos += 2;                           //ID of this while statement
                        pos += 2;                           //AND (1) or OR (2). There are no examples of WHILE OR in the game, but supposedly it exists.
                        output.add(line);

                        triggerConditionalNextTime = true;
                    }
                    break;
                    case 0x2B: //NOP
                        pos += 2;
                        break;
                    case 0x2C: //BREAK
                        output.add("Stop();");
                        pos += 2;
                        break;
                    case 0x2D: //ENDIF
                    {
                        String indent = "";
                        for (int i = 0; i < conditionalLayer - 1; i++) {
                            indent += "    ";
                        }
                        String line = indent + "ENDIF";
                        pos += 2;
                        pos += 2;  // // ID of IF statement to end (independent of the current while statement ID)
                        output.add(line);
                        output.add("");
                        triggerConditionalNextTime = false;
                        conditionalLayer--;
                        SlightlyResistIndent = true;
                    }
                    break;
                    case 0x2E: //ENDWHILE
                    {
                        String indent = "";
                        for (int i = 0; i < conditionalLayer - 1; i++) {
                            indent += "    ";
                        }
                        String line = indent + "ENDWHILE";
                        pos += 2;

                        pos += 2;   // ID of WHILE statement to end (independent of the current IF statement ID)
                        output.add(line);
                        output.add("");
                        triggerConditionalNextTime = false;
                        conditionalLayer--;
                        SlightlyResistIndent = true;
                    }
                    break;
                    case 0x2F:  //ELSE
                        pos += 4;
                        output.add("ELSE");
                        SlightlyResistIndent = true;
                        break;
                    case 0x30:  //MSGINIT (This is used to set some values for cutscene text (e.g. opening, walk to mansion, ending G)
                    {
                        String line = "InitMessage(";
                        pos += 2;

                        line += BitConverter.ToInt16(filebytes, pos) + ", ";    //bottom left X of text box?
                        pos += 2;

                        line += BitConverter.ToInt16(filebytes, pos) + ", ";   //bottom left Y of text box?
                        pos += 2;

                        line += BitConverter.ToInt16(filebytes, pos) + ", ";    //top right X of text box?
                        pos += 2;

                        line += BitConverter.ToInt32(filebytes, pos) + ", ";    //top right Y of text box?
                        pos += 2;

                        line += BitConverter.ToInt16(filebytes, pos);    //unknown
                        pos += 2;

                        output.add(line + ");");
                    }
                    break;
                    case 0x32:  //MSGATTR (This is used to set some values for cutscene text (e.g. opening, walk to mansion, ending G))
                    {
                        String line = "SetMessageAttributes(";
                        pos += 2;

                        line += BitConverter.ToInt16(filebytes, pos) + ", ";
                        pos += 2;

                        line += BitConverter.ToInt16(filebytes, pos) + ", ";
                        pos += 2;

                        line += BitConverter.ToInt32(filebytes, pos) + ", ";
                        pos += 2;

                        line += BitConverter.ToInt16(filebytes, pos) + ", ";
                        pos += 2;

                        line += BitConverter.ToInt16(filebytes, pos) + ", ";
                        pos += 2;

                        line += BitConverter.ToInt16(filebytes, pos) + ", ";
                        pos += 2;

                        line += BitConverter.ToInt16(filebytes, pos) + ", ";
                        pos += 2;

                        line += BitConverter.ToInt16(filebytes, pos);
                        pos += 2;

                        output.add(line + ");");
                    }
                    break;
                    case 0x33:  //MSGOUT
                    {
                        String line = "ShowMessage(\"";
                        pos += 6; //also skips over 4 bytes of data (usually 00 F0 00 F0)

                        LinkedList<Byte> message = new LinkedList<Byte>();

                        while (BitConverter.ToInt16(filebytes, pos) != 0xFF28) {
                            message.add((byte) (filebytes[pos] & 0xFF));
                            if (filebytes[pos + 1] != 0) {
                                message.add(filebytes[pos + 1]);
                            }

                            pos += 2;
                        }

                        line += Utility.DecodeShiftJISString(message).replace((char) 0x0A + "", "\\n");

                        output.add(line + "\");");
                    }
                    break;
                    case 0x34: //SETMARK        //This is probably setting hotspot positions. Values might include position, and the position Jennifer needs to be at to activate it?

                        //The functions to play when each mark is pressed is set out by CALL. E.g. look at function 261, in the first if statement. It gets the calls, then draws the marks.
                    {
                        pos += 2;
                        String line = "SetMark(";

                        while (filebytes[pos + 1] != 0xFF)  //count sections of 0x0A until we hit the next opcode
                        {
                            line += "[";
                            line += BitConverter.ToInt16(filebytes, pos) + ", ";   //Tentatively, XPos of marker
                            pos += 2;
                            line += BitConverter.ToInt16(filebytes, pos) + ", ";   //Tentatively, YPos of marker
                            pos += 2;
                            line += BitConverter.ToInt16(filebytes, pos) + ", ";   //Tentatively, max X the player must autowalk to
                            pos += 2;
                            line += BitConverter.ToInt16(filebytes, pos) + ", ";   //Tentatively, Y position the player must be at
                            pos += 2;
                            line += BitConverter.ToInt16(filebytes, pos) + "]";    //Tentatively, min X the player must be at
                            pos += 2;

                            if (filebytes[pos + 1] != 0xFF) {
                                line += ",";
                            }
                        }
                        output.add(line + ");");
                    }
                    break;
                    case 0x36:  //MSGWAIT
                        pos += 2;
                        output.add("MessageWait();");
                        break;
                    case 0x37:  //EVSTART
                    {
                        String line = "StartEvent(";
                        pos += 2;
                        line += BitConverter.ToInt16(filebytes, pos) + ", ";
                        pos += 2;
                        line += EvaluateADOInt16(BitConverter.ToInt16(filebytes, pos)) + ");";
                        pos += 2;
                        output.add(line);
                    }
                    break;
                    case 0x39: //BGLOAD
                    {
                        String line = "LoadBackground(";
                        pos += 2;

                        line += BitConverter.ToInt16(filebytes, pos) + ", \"";
                        pos += 2;

                        while (filebytes[pos] != 0x00) {
                            line += (char) filebytes[pos];
                            pos++;
                        }
                        pos += 2; //skip null terminating bytes

                        while (pos % 2 != 0) //then pad until pos is a multiple of two
                        {
                            pos++;
                        }
                        line += "\");";

                        output.add(line);
                    }
                    break;
                    case 0x3A: //PALLOAD
                    {
                        String line = "LoadPalette(";
                        pos += 2;

                        line += BitConverter.ToInt16(filebytes, pos) + ", \"";
                        pos += 2;

                        while (filebytes[pos] != 0x00) {
                            line += (char) filebytes[pos];
                            pos++;
                        }
                        pos += 2; //skip null terminating bytes

                        while (pos % 2 != 0) //then pad until pos is a multiple of two
                        {
                            pos++;
                        }
                        line += "\");";

                        output.add(line);
                    }
                    break;
                    case 0x3B: //BGMREQ
                    {
                        String line = "PlayBGM(\"";
                        pos += 2;

                        boolean loopedBGM = filebytes[pos] == 1;
                        pos += 2;

                        while (filebytes[pos] != 0x00) {
                            line += (char) filebytes[pos];
                            pos++;
                        }
                        pos += 2; //skip null terminating bytes

                        while (pos % 2 != 0) //then pad until pos is a multiple of two
                        {
                            pos++;
                        }

                        if (loopedBGM) {
                            line += "\", LOOP);";
                        } else {
                            line += "\", NO_LOOP);";
                        }

                        output.add(line);
                    }
                    break;
                    case 0x3C:  //SPRCLR
                    {
                        String line = "ClearSprite(";
                        pos += 2;
                        line += BitConverter.ToInt16(filebytes, pos) + ");";
                        pos += 2;
                        output.add(line);
                    }
                    break;
                    case 0x3F:  //ALLSPRCLR
                        pos += 2;
                        output.add("ClearAllSprites();");
                        break;
                    case 0x40:  //MSGCLEAR
                        pos += 2;
                        output.add("ClearMessage();");
                        break;
                    case 0x41:  //SCREENCLR
                        pos += 2;
                        output.add("ClearScreen();");
                        break;
                    case 0x42: //SCREENON
                        pos += 2;
                        output.add("ScreenOn();");
                        break;
                    case 0x43: //SCREENOFF
                        pos += 2;
                        output.add("ScreenOff();");
                        break;
                    case 0x46:  //BGDISP
                    {
                        String line = "DisplayBackground(";
                        pos += 2;

                        line += BitConverter.ToInt16(filebytes, pos) + ", ";   //X
                        pos += 2;

                        line += BitConverter.ToInt16(filebytes, pos) + ", ";   //Y
                        pos += 2;

                        line += BitConverter.ToInt16(filebytes, pos) + ", ";   //possibly layer
                        pos += 2;

                        line += BitConverter.ToInt16(filebytes, pos) + ", ";   //BG slot to get the BufferedImage from
                        pos += 2;

                        line += BitConverter.ToInt16(filebytes, pos) + ", ";
                        pos += 2;

                        line += BitConverter.ToInt16(filebytes, pos);  //use alpha
                        pos += 2;

                        output.add(line + ");");
                    }
                    break;
                    case 0x47:  //BGANIM
                    {
                        String line = "AnimateBackground(";
                        pos += 2;

                        line += BitConverter.ToInt16(filebytes, pos) + ", ";   //X
                        pos += 2;

                        line += BitConverter.ToInt16(filebytes, pos) + ", ";   //Y
                        pos += 2;

                        line += BitConverter.ToInt16(filebytes, pos) + ", ";   //First bg slot to get BufferedImage from
                        pos += 2;

                        line += BitConverter.ToInt16(filebytes, pos) + ", ";   //Number of slots in animation
                        pos += 2;

                        line += BitConverter.ToInt16(filebytes, pos) + ", ";   //wait time?
                        pos += 2;

                        line += BitConverter.ToInt16(filebytes, pos) + ", ";  //number of loops?
                        pos += 2;

                        line += BitConverter.ToInt16(filebytes, pos);  //use alpha
                        pos += 2;

                        output.add(line + ");");
                    }
                    break;
                    case 0x48: //BGSCROLL                       Oh! Of interest might be the fact that scrolling backgrounds actually tile if you go past the end.
                    {
                        String line = "BGScroll(";     //these values are for the opening scroll when Mary is pointing at the mansion. It also gets used when Anne falls out of the window, but I haven't experimented with that version's values yet.
                        pos += 2;
                        line += BitConverter.ToInt16(filebytes, pos) + ", ";   //?
                        pos += 2;
                        line += BitConverter.ToInt16(filebytes, pos) + ", ";   //Should be set to 4. Other values seem to stop it from scrolling.
                        pos += 2;
                        line += BitConverter.ToInt16(filebytes, pos) + ", ";   //scroll speed (lower is slower)
                        pos += 2;
                        line += BitConverter.ToInt16(filebytes, pos) + ", ";   //? speed multiplier where higher is slower
                        pos += 2;
                        line += BitConverter.ToInt16(filebytes, pos) + ");";   //amount to scroll?
                        pos += 2;
                        output.add(line);
                    }
                    break;
                    case 0x49:  //PALSET
                    {
                        String line = "SetPalette(";
                        pos += 2;
                        line += BitConverter.ToInt16(filebytes, pos) + ", ";
                        pos += 2;
                        line += BitConverter.ToInt16(filebytes, pos) + ", ";
                        pos += 2;
                        line += BitConverter.ToInt16(filebytes, pos) + ", ";
                        pos += 2;
                        line += BitConverter.ToInt16(filebytes, pos) + ", ";
                        pos += 2;
                        line += EvaluateADOInt16(BitConverter.ToInt16(filebytes, pos)) + ");";
                        pos += 2;
                        output.add(line);
                    }
                    break;
                    case 0x4A:  //BGWAIT
                        pos += 2;
                        output.add("BGWait();");
                        break;
                    case 0x4B: //WAIT
                    {
                        String line = "Wait(";
                        pos += 2;
                        line += BitConverter.ToInt32(filebytes, pos);
                        pos += 4;
                        output.add(line + ");");
                    }
                    break;
                    case 0x4D: //BOXFILL
                    {
                        String line = "BoxFill(";
                        pos += 2;
                        line += BitConverter.ToInt32(filebytes, pos) + ", ";
                        pos += 4;
                        line += BitConverter.ToInt16(filebytes, pos) + ", ";
                        pos += 2;
                        line += BitConverter.ToInt16(filebytes, pos) + ", ";
                        pos += 2;
                        line += BitConverter.ToInt16(filebytes, pos) + ", ";
                        pos += 2;
                        line += BitConverter.ToInt16(filebytes, pos) + ", ";
                        pos += 2;
                        line += BitConverter.ToInt16(filebytes, pos) + ");";
                        pos += 2;
                        output.add(line);
                    }
                    break;
                    case 0x4F:  //SETBKCOL - probably RGB, though it's only ever used for black in the game
                    {
                        String line = "SetBackColour(";
                        pos += 2;

                        line += BitConverter.ToInt16(filebytes, pos) + ", ";
                        pos += 2;

                        line += BitConverter.ToInt16(filebytes, pos) + ", ";
                        pos += 2;

                        line += BitConverter.ToInt16(filebytes, pos) + ");";
                        pos += 2;

                        output.add(line);
                    }
                    break;
                    case 0x50:  //MSGCOL            //This is never used in the original game, so I'm unclear on how many parameters it has
                    {
                        String line = "SetMessageColor(";
                        pos += 2;
                        line += BitConverter.ToInt16(filebytes, pos) + ", ";   //R
                        pos += 2;
                        line += BitConverter.ToInt16(filebytes, pos) + ", ";   //G
                        pos += 2;
                        line += BitConverter.ToInt16(filebytes, pos) + ", ";   //B
                        pos += 2;
                        line += BitConverter.ToInt16(filebytes, pos) + ", ";   //Highlight R
                        pos += 2;
                        line += BitConverter.ToInt16(filebytes, pos) + ", ";   //Highlight G
                        pos += 2;
                        line += BitConverter.ToInt16(filebytes, pos) + ");";   //Highlight B
                        pos += 2;
                        output.add(line);
                    }
                    break;
                    case 0x51:  //MSGSPD
                    {
                        String line = "SetMessageSpeed(";
                        pos += 2;
                        line += BitConverter.ToInt16(filebytes, pos) + ");";
                        pos += 2;
                        output.add(line);
                    }
                    break;
                    case 0x52: //MAPINIT
                    {
                        String line = "MapInit(";       //effectively controls the black bars at the sides of the screen
                        pos += 2;
                        line += BitConverter.ToInt16(filebytes, pos) + ", ";       //top left X
                        pos += 2;
                        line += BitConverter.ToInt16(filebytes, pos) + ", ";       //top left Y
                        pos += 2;
                        line += BitConverter.ToInt16(filebytes, pos) + ", ";       //  ? usually 0
                        pos += 2;
                        line += BitConverter.ToInt16(filebytes, pos) + ", ";       //  position of right black bar
                        pos += 2;
                        line += BitConverter.ToInt16(filebytes, pos) + ", ";       //  position of lower black bar
                        pos += 2;
                        line += BitConverter.ToInt16(filebytes, pos) + ");";       //  ? usually 0
                        pos += 2;
                        output.add(line);
                    }
                    break;
                    case 0x53: //MAPLOAD
                    {
                        String line = "LoadMap(\"";
                        pos += 4;

                        while (filebytes[pos] != 0x00) {
                            line += (char) filebytes[pos];
                            pos++;
                        }
                        pos += 2; //skip null terminating bytes

                        while (pos % 2 != 0) //then pad until pos is a multiple of two
                        {
                            pos++;
                        }

                        line += "\",\"";

                        while (filebytes[pos] != 0x00) {
                            line += (char) filebytes[pos];
                            pos++;
                        }
                        pos += 2; //skip null terminating bytes

                        while (pos % 2 != 0) //then pad until pos is a multiple of two
                        {
                            pos++;
                        }

                        line += "\");";

                        output.add(line);
                    }
                    break;
                    case 0x55: //SPRENT
                    {
                        String line = "CreateSprite(";
                        pos += 2;
                        line += BitConverter.ToInt16(filebytes, pos) + ", ";   //Sprite slot to put it in
                        pos += 2;
                        line += EvaluateADOInt16(BitConverter.ToInt16(filebytes, pos)) + ", "; //X position
                        pos += 2;
                        line += EvaluateADOInt16(BitConverter.ToInt16(filebytes, pos)) + ", "; //Y position
                        pos += 2;
                        line += BitConverter.ToInt16(filebytes, pos) + ", ";   //Idk. On the credits screen, it is 0x50, which allows jennifer to animate, whereas the 0x40 on the two friends keeps their animation on one frame. Possible animation parameters bitfield? It also controls the layer that the sprite is drawn on.
                        pos += 2;
                        line += filebytes[pos] + ", ";    //Animation number (e.g. 74 = Jennifer during good ending credits)
                        pos++;
                        line += filebytes[pos] + ", ";    //Animation group index (e.g. 2 = "J_3")
                        pos++;
                        line += BitConverter.ToInt16(filebytes, pos) + ", ";       //if this is set to 0 then it won't animate; possible bitfield where the 0x10 bit is 'animated'?
                        pos += 2;
                        line += BitConverter.ToInt16(filebytes, pos) + ", ";       //index of starting frame
                        pos += 2;
                        line += BitConverter.ToInt16(filebytes, pos) + ");";       //whether it should use transparency or not
                        pos += 2;
                        output.add(line);
                    }
                    break;
                    case 0x56: //SETPROC
                    {
                        String line = "SetProc(";
                        pos += 2;
                        line += BitConverter.ToInt16(filebytes, pos) + ");";
                        pos += 2;
                        output.add(line);
                    }
                    break;
                    case 0x57: //SCEINIT
                        pos += 2;
                        output.add("SceneInit();");
                        break;
                    case 0x58: //USRCTL
                    {
                        String line = "SetPlayerControl(";
                        pos += 2;
                        line += BitConverter.ToInt16(filebytes, pos) + ");";
                        pos += 2;
                        output.add(line);
                    }
                    break;
                    case 0x59: //MAPATTR
                    {
                        String line = "SetMapAttributes(";
                        pos += 2;
                        line += BitConverter.ToInt16(filebytes, pos) + ");";
                        pos += 2;
                        output.add(line);
                    }
                    break;
                    case 0x5A: //MAPPOS
                    {
                        String line = "SetMapPos(";
                        pos += 2;
                        line += BitConverter.ToInt16(filebytes, pos) + ", ";
                        pos += 2;
                        line += BitConverter.ToInt16(filebytes, pos) + ", ";
                        pos += 2;
                        line += BitConverter.ToInt16(filebytes, pos) + ");";
                        pos += 2;
                        output.add(line);
                    }
                    break;
                    case 0x5B: //SPRPOS
                    {
                        String line = "SetSpritePosition(";
                        pos += 2;
                        line += BitConverter.ToInt16(filebytes, pos) + ", ";
                        pos += 2;
                        line += EvaluateADOInt16(BitConverter.ToInt16(filebytes, pos)) + ", ";
                        pos += 2;
                        line += EvaluateADOInt16(BitConverter.ToInt16(filebytes, pos)) + ", ";
                        pos += 2;
                        line += BitConverter.ToInt16(filebytes, pos) + ");";
                        pos += 2;
                        output.add(line);
                    }
                    break;
                    case 0x5C: //SPRANIM
                    {
                        String line = "AnimateSprite(";
                        pos += 2;
                        line += BitConverter.ToInt16(filebytes, pos) + ", ";
                        pos += 2;
                        line += EvaluateADOInt16(BitConverter.ToInt16(filebytes, pos)) + ", ";
                        pos += 2;
                        line += EvaluateADOInt16(BitConverter.ToInt16(filebytes, pos)) + ", ";
                        pos += 2;
                        line += EvaluateADOInt16(BitConverter.ToInt16(filebytes, pos)) + ");";
                        pos += 2;
                        output.add(line);
                    }
                    break;
                    case 0x5D: //SPRDIR     //sprite direction?
                    {
                        String line = "SetSpriteDirection(";
                        pos += 2;
                        line += BitConverter.ToInt16(filebytes, pos) + ", ";
                        pos += 2;
                        line += BitConverter.ToInt16(filebytes, pos) + ", ";
                        pos += 2;
                        line += BitConverter.ToInt16(filebytes, pos) + ", ";
                        pos += 2;
                        line += BitConverter.ToInt16(filebytes, pos) + ", ";
                        pos += 2;
                        line += BitConverter.ToInt16(filebytes, pos) + ");";
                        pos += 2;
                        output.add(line);
                    }
                    break;
                    case 0x5E: //GAMEINIT
                        pos += 2;
                        output.add("GameInit();");
                        break;
                    case 0x5F: //CONTINIT
                        pos += 2;
                        output.add("ContInit();");
                        break;
                    case 0x60: //SCEEND
                        pos += 2;
                        output.add("SceneEnd();");
                        break;
                    case 0x61: //MAPSCROLL
                    {
                        String line = "ScrollMap(";
                        pos += 2;
                        line += BitConverter.ToInt16(filebytes, pos) + ", ";    //X target position (-1 for none)
                        pos += 2;
                        line += BitConverter.ToInt16(filebytes, pos) + ", ";    //Y target position (-1 for none)
                        pos += 2;
                        line += BitConverter.ToInt16(filebytes, pos) + ");";   //scroll speed
                        pos += 2;
                        output.add(line);
                    }
                    break;
                    case 0x62:  //SPRLMT
                    {
                        String line = "SpriteLmt(";
                        pos += 2;
                        line += BitConverter.ToInt16(filebytes, pos) + ", ";
                        pos += 2;
                        line += BitConverter.ToInt16(filebytes, pos) + ", ";
                        pos += 2;
                        line += BitConverter.ToInt16(filebytes, pos) + ");";
                        pos += 2;
                        output.add(line);
                    }
                    break;
                    case 0x63: //SPRWALKX
                    {
                        String line = "SpriteWalkX(";
                        pos += 2;
                        line += BitConverter.ToInt16(filebytes, pos) + ", ";
                        pos += 2;
                        line += BitConverter.ToInt16(filebytes, pos) + ", ";
                        pos += 2;
                        line += BitConverter.ToInt16(filebytes, pos) + ", ";
                        pos += 2;
                        line += BitConverter.ToInt16(filebytes, pos) + ", ";
                        pos += 2;
                        line += BitConverter.ToInt16(filebytes, pos) + ");";
                        pos += 2;
                        output.add(line);
                    }
                    break;
                    case 0x66:  //SPRWAIT
                    {
                        String line = "SpriteWait(";
                        pos += 2;
                        line += BitConverter.ToInt16(filebytes, pos) + ");";
                        pos += 2;
                        output.add(line);
                    }
                    break;
                    case 0x67: //SEREQ
                    {
                        String line = "PlaySoundEffect(\"";
                        pos += 2;

                        boolean loopedSE = filebytes[pos] == 1;
                        pos += 2;

                        while (filebytes[pos] != 0x00) {
                            line += (char) filebytes[pos];
                            pos++;
                        }
                        pos += 2; //skip null terminating bytes

                        while (pos % 2 != 0) //then pad until pos is a multiple of two
                        {
                            pos++;
                        }

                        if (loopedSE) {
                            line += "\", LOOP);";
                        } else {
                            line += "\", NO_LOOP);";
                        }

                        output.add(line);
                    }
                    break;
                    case 0x68: //SNDSTOP
                        pos += 2;
                        output.add("SoundStop();");
                        break;
                    case 0x69: //SESTOP
                    {
                        String line = "StopSoundEffect(\"";
                        pos += 2;
                        while (filebytes[pos] != 0x00) {
                            line += (char) filebytes[pos];
                            pos++;
                        }
                        pos += 2; //skip null terminating bytes

                        while (pos % 2 != 0) //then pad until pos is a multiple of two
                        {
                            pos++;
                        }
                        line += "\");";

                        output.add(line);
                    }
                    break;
                    case 0x6A: //BGMSTOP
                        pos += 2;
                        output.add("StopBGM();");
                        break;
                    case 0x6B: //DOORNOSET
                        pos += 2;
                        output.add("DoorNoSet();");
                        break;
                    case 0x6C: //RAND
                    {
                        String line = "Random(";
                        pos += 2;
                        line += BitConverter.ToInt16(filebytes, pos) + ", ";
                        pos += 2;
                        line += BitConverter.ToInt16(filebytes, pos) + ", ";
                        pos += 2;
                        line += BitConverter.ToInt16(filebytes, pos) + ");";
                        pos += 2;
                        output.add(line);
                    }
                    break;
                    case 0x6D: //BTWAIT
                    {
                        String line = "BTWait(";
                        pos += 2;
                        line += BitConverter.ToInt16(filebytes, pos);
                        pos += 2;
                        output.add(line + ");");
                    }
                    break;
                    case 0x6E:  //FAWAIT
                        pos += 2;
                        output.add("FaWait();");
                        break;
                    case 0x6F: //SCLBLOCK
                    {
                        String line = "SclBlock(";
                        pos += 2;
                        int type = BitConverter.ToInt32(filebytes, pos);
                        pos += 4;

                        while (filebytes[pos + 1] != 0xFF) {
                            line += BitConverter.ToInt16(filebytes, pos);
                            pos += 2;
                            if (filebytes[pos + 1] != 0xFF) {
                                line += ", ";
                            }
                        }

                        output.add(line + ");");
                    }
                    break;
                    case 0x71: //SEREQPV
                    {
                        String line = "PlaySoundEffectPV(\"";       //Play sound effect but with Pan and volume parameters?
                        pos += 2;

                        line += BitConverter.ToInt16(filebytes, pos) + ", ";
                        pos += 2;

                        line += BitConverter.ToInt16(filebytes, pos) + ", ";
                        pos += 2;

                        line += BitConverter.ToInt16(filebytes, pos) + ", ";
                        pos += 2;

                        while (filebytes[pos] != 0x00) {
                            line += (char) filebytes[pos];
                            pos++;
                        }
                        pos += 2; //skip null terminating bytes

                        while (pos % 2 != 0) //then pad until pos is a multiple of two
                        {
                            pos++;
                        }
                        line += "\");";

                        output.add(line);
                    }
                    break;
                    case 0x72: //SEREQSPR
                    {
                        String line = "PlaySoundEffectAtSprite(";
                        pos += 2;

                        line += BitConverter.ToInt16(filebytes, pos) + ", ";
                        pos += 2;

                        line += BitConverter.ToInt16(filebytes, pos) + ", ";
                        pos += 2;

                        line += BitConverter.ToInt16(filebytes, pos) + ", \"";
                        pos += 2;

                        while (filebytes[pos] != 0x00) {
                            line += (char) filebytes[pos];
                            pos++;
                        }
                        pos += 2; //skip null terminating bytes

                        while (pos % 2 != 0) //then pad until pos is a multiple of two
                        {
                            pos++;
                        }
                        line += "\");";

                        output.add(line);
                    }
                    break;
                    case 0x73: //SCERESET           could SCERESET be the thing that resets the marks and calls so that they can be set anew...?
                        pos += 2;
                        output.add("SceneReset();");
                        break;
                    case 0x74: //BGSPRENT
                    {
                        String line = "CreateBackgroundSprite(";
                        pos += 2;
                        line += BitConverter.ToInt16(filebytes, pos) + ", ";
                        pos += 2;
                        line += EvaluateADOInt16(BitConverter.ToInt16(filebytes, pos)) + ", "; //X position
                        pos += 2;
                        line += EvaluateADOInt16(BitConverter.ToInt16(filebytes, pos)) + ", "; //Y position
                        pos += 2;
                        line += BitConverter.ToInt16(filebytes, pos) + ", ";
                        pos += 2;
                        line += EvaluateADOInt16(BitConverter.ToInt16(filebytes, pos)) + ", ";
                        pos += 2;
                        line += BitConverter.ToInt16(filebytes, pos) + ");";
                        pos += 2;
                        output.add(line);
                    }
                    break;
                    case 0x77: //SLANTSET
                    {
                        String line = "SetSlant(";
                        pos += 2;
                        line += BitConverter.ToInt16(filebytes, pos) + ", ";
                        pos += 2;
                        line += BitConverter.ToInt16(filebytes, pos) + ", ";
                        pos += 2;
                        line += BitConverter.ToInt16(filebytes, pos) + ", ";
                        pos += 2;
                        line += BitConverter.ToInt16(filebytes, pos) + ");";
                        pos += 2;
                        output.add(line);
                    }
                    break;
                    case 0x78: //SLANTCLR
                        output.add("ClearSlant();");
                        pos += 2;
                        break;
                    case 0x7A:  //SPCFUNC       Varies in length, depending on the first parameter.
                    {
                        String line = "SpcFunc(";
                        pos += 2;
                        int val1 = BitConverter.ToInt16(filebytes, pos);
                        line += val1 + ", ";
                        pos += 2;

                        line += BitConverter.ToInt16(filebytes, pos);
                        pos += 2;

                        if (val1 == 2 || val1 == 6 || val1 == 0x12 || val1 == 0x13 || val1 == 0x14 || val1 == 0x15) {
                            line += ", ";
                            line += BitConverter.ToInt16(filebytes, pos);
                            pos += 2;

                            //if (val1 == 0x14)
                            //  {
                            //     JOptionPane.showMessageDialog(null, "Adding 2 for a 0x14 val1 in 7AFF command. This should only be done for the wonderswan version, so remove this code when you've finished experimenting");
                            //     pos += 2;
                            //  }
                        } else if (val1 == 9) {
                            line += ", ";
                            line += BitConverter.ToInt16(filebytes, pos) + ", ";
                            pos += 2;
                            line += BitConverter.ToInt16(filebytes, pos) + ", ";
                            pos += 2;
                            line += BitConverter.ToInt16(filebytes, pos) + ", ";
                            pos += 2;
                            line += BitConverter.ToInt16(filebytes, pos);
                            pos += 2;
                        }

                        output.add(line + ");");
                    }
                    break;
                    case 0x7B: //SEPAN
                    {
                        String line = "SetSoundEffectPan(";
                        pos += 2;

                        line += BitConverter.ToInt16(filebytes, pos) + ", \"";
                        pos += 2;

                        while (filebytes[pos] != 0x00) {
                            line += (char) filebytes[pos];
                            pos++;
                        }
                        pos += 2; //skip null terminating bytes

                        while (pos % 2 != 0) //then pad until pos is a multiple of two
                        {
                            pos++;
                        }
                        line += "\");";

                        output.add(line);
                    }
                    break;
                    case 0x7C: //SEVOL
                    {
                        String line = "SetSoundEffectVolume(";
                        pos += 2;

                        line += BitConverter.ToInt16(filebytes, pos) + ", \"";
                        pos += 2;

                        while (filebytes[pos] != 0x00) {
                            line += (char) filebytes[pos];
                            pos++;
                        }
                        pos += 2; //skip null terminating bytes

                        while (pos % 2 != 0) //then pad until pos is a multiple of two
                        {
                            pos++;
                        }
                        line += "\");";

                        output.add(line);
                    }
                    break;
                    case 0x7D: //BGDISPTRN
                    {
                        String line = "BGDispTrn(";
                        pos += 2;
                        line += BitConverter.ToInt16(filebytes, pos) + ", ";
                        pos += 2;
                        line += BitConverter.ToInt16(filebytes, pos) + ", ";
                        pos += 2;
                        line += BitConverter.ToInt16(filebytes, pos) + ", ";
                        pos += 2;
                        line += BitConverter.ToInt16(filebytes, pos) + ", ";
                        pos += 2;
                        line += BitConverter.ToInt16(filebytes, pos) + ", ";
                        pos += 2;
                        line += BitConverter.ToInt16(filebytes, pos) + ", ";
                        pos += 2;
                        line += BitConverter.ToInt16(filebytes, pos) + ");";
                        pos += 2;
                        output.add(line);
                    }
                    break;
                    case 0x80: //TMWAIT
                    {
                        String line = "WaitForSeconds(";
                        pos += 2;
                        line += BitConverter.ToInt32(filebytes, pos);
                        pos += 4;
                        output.add(line + ");");
                    }
                    break;
                    case 0x81: //BGSPRANIM
                    {
                        String line = "AnimateBackgroundSprite(";
                        pos += 2;
                        line += BitConverter.ToInt16(filebytes, pos) + ", ";
                        pos += 2;
                        line += BitConverter.ToInt16(filebytes, pos) + ", ";
                        pos += 2;
                        line += BitConverter.ToInt16(filebytes, pos) + ", ";
                        pos += 2;
                        line += BitConverter.ToInt16(filebytes, pos) + ", ";
                        pos += 2;
                        line += BitConverter.ToInt16(filebytes, pos) + ", ";
                        pos += 2;
                        line += BitConverter.ToInt16(filebytes, pos) + ", ";
                        pos += 2;
                        line += BitConverter.ToInt16(filebytes, pos) + ", ";
                        pos += 2;
                        line += BitConverter.ToInt16(filebytes, pos) + ", ";
                        pos += 2;
                        line += BitConverter.ToInt16(filebytes, pos) + ")";
                        pos += 2;
                        output.add(line);
                    }
                    break;
                    case 0x83: //NEXTCOM
                    {
                        String line = "NextCom(";
                        pos += 2;
                        line += BitConverter.ToInt16(filebytes, pos) + ");";
                        pos += 2;
                        output.add(line);
                    }
                    break;
                    case 0x84: //WORKCLR
                        pos += 2;
                        output.add("WorkClear();");
                        break;
                    case 0x85: //BGBUFCLR
                    {
                        String line = "ClearBGBuffer(";
                        pos += 2;
                        line += BitConverter.ToInt16(filebytes, pos) + ", ";
                        pos += 2;
                        line += BitConverter.ToInt16(filebytes, pos) + ");";
                        pos += 2;
                        output.add(line);
                    }
                    break;
                    case 0x86: //ABSBGSPRENT
                    {
                        String line = "CreateAbsBackgroundSprite(";
                        pos += 2;
                        line += BitConverter.ToInt16(filebytes, pos) + ", ";
                        pos += 2;
                        line += BitConverter.ToInt16(filebytes, pos) + ", ";
                        pos += 2;
                        line += BitConverter.ToInt16(filebytes, pos) + ", ";
                        pos += 2;
                        line += BitConverter.ToInt16(filebytes, pos) + ", ";
                        pos += 2;
                        line += BitConverter.ToInt16(filebytes, pos) + ", ";
                        pos += 2;
                        line += BitConverter.ToInt16(filebytes, pos) + ");";
                        pos += 2;
                        output.add(line);
                    }
                    break;
                    case 0x87: //AVIPLAY
                    {
                        String line = "PlayAVI(";
                        pos += 2;

                        line += BitConverter.ToInt16(filebytes, pos) + ", ";
                        pos += 2;

                        line += BitConverter.ToInt16(filebytes, pos) + ", ";
                        pos += 2;

                        line += BitConverter.ToInt16(filebytes, pos) + ", ";
                        pos += 2;

                        line += BitConverter.ToInt16(filebytes, pos) + ", ";
                        pos += 2;

                        line += BitConverter.ToInt16(filebytes, pos) + ", \"";
                        pos += 2;

                        while (filebytes[pos] != 0x00) {
                            line += (char) filebytes[pos];
                            pos++;
                        }
                        pos += 2; //skip null terminating bytes

                        while (pos % 2 != 0) //then pad until pos is a multiple of two
                        {
                            pos++;
                        }
                        line += "\");";

                        output.add(line);
                    }
                    break;
                    case 0x88: //AVISTOP
                        pos += 2;
                        output.add("StopAVI();");
                        break;
                    case 0x89: //SPRMARK
                    {
                        String line = "SetSpriteMark(";
                        pos += 2;
                        line += BitConverter.ToInt16(filebytes, pos) + ", ";
                        pos += 2;
                        line += BitConverter.ToInt16(filebytes, pos) + ", ";
                        pos += 2;
                        line += BitConverter.ToInt16(filebytes, pos) + ", ";
                        pos += 2;
                        line += BitConverter.ToInt16(filebytes, pos) + ", ";
                        pos += 2;
                        line += BitConverter.ToInt16(filebytes, pos) + ");";
                        pos += 2;
                        output.add(line);
                    }
                    break;
                    case 0x8A: //BGMATTR
                    {
                        String line = "SetBGMAttributes(";
                        pos += 2;
                        line += BitConverter.ToInt16(filebytes, pos) + ", ";
                        pos += 2;
                        line += BitConverter.ToInt16(filebytes, pos);
                        pos += 2;
                        output.add(line + ");");
                    }
                    break;
                    case 0xFE: //UNK_FE     Wonderswan only
                    {
                        String line = "UNK_FE(";
                        pos += 2;
                        line += BitConverter.ToInt16(filebytes, pos) + ", ";
                        pos += 2;
                        line += BitConverter.ToInt16(filebytes, pos);
                        pos += 2;
                        output.add(line + ");");
                    }
                    break;
                    default:
                        JOptionPane.showMessageDialog(null, "Couldn't understand opcode: " + (filebytes[pos] << 8 | filebytes[pos + 1]));
                        pos += 2;
                        output.add("============ DID NOT FINISH PARSING DUE TO UNHANDLED OPCODE! ============");
                        return output;
                }

                if (conditionalLayer > 0) { //indent the most recent line if we are in a conditional
                    String indent = "";
                    for (int i = 0; i < conditionalLayer; i++) {
                        if (SlightlyResistIndent && i == conditionalLayer - 1)   //if we are resisting the indent, break one loop early (i.e. so that 'ELSE' doesn't get indented with the rest of the IF statement)
                        {
                            break;
                        }
                        indent += "    ";
                    }
                    output.set(output.size() - 1, indent + output.get(output.size() - 1));
                }

                if (triggerConditionalNextTime) {
                    conditionalLayer++;
                    triggerConditionalNextTime = false;
                }
            } else {
                //File.WriteAllBytes("ERRORED_ADO_FUNCTION", filebytes);
                JOptionPane.showMessageDialog(null, "The ADO parser is lost... it was expecting an ADO opcode at " + pos + ", but this doesn't look like one!");
            }
        }
        return output;
    }


    public String EvaluateADOInt16(short input) {     //separate into variable ID and the bank to get it from. If the bank is 0 then just treat it as a number

        if ((input & 0x0F00) != 0x0F00) //if there's an F in the second nibble, assume it's just a negative number and don't bother with the bank stuff
        { //otherwise, check the first nibble for the bank
            byte bank = (byte) ((input >>> 12) & 0x0F);
            if (bank != 0) {
                return "BANK" + bank + "[" + (short) (input & 0x0FFF) + "]";
            }
        }
        return (short) (input & 0xFFFF) + "";
    }
}
