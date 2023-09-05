package com.clockTower.main;

import com.clockTower.fileUtilities.ADOScript;
import com.clockTower.main.loader.ConditionalType;

public class conditional {
	public conditional(ConditionalType _type, short _myID, ADOScript _script, int _functionIndex) {
		
		type = _type;
		myID = _myID;
		script = _script;
		functionIndex = _functionIndex;
		
	}
	public ConditionalType type;			//IF or WHILE
	public int operatorType;		  //1 (AND) or 2 (OR)
	public int myID;
	public ADOScript script;
	public int functionIndex;
	public int startPoint;
	public int currentPos;
	public boolean fulfilled;		//whether it will return true or false
	public boolean ignoreElseSection = false;
}