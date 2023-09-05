package com.clockTower.main;

import com.clockTower.fileUtilities.ADOScript;

public class returnPoint {		//so that the script can jump to other places and come back later
	public ADOScript script;
	public int function;
	public int returnpos;
	
	public returnPoint (ADOScript _script, int _functionIndex, int _returnPos) {
		script = _script;
		function = _functionIndex;
		returnpos = _returnPos;
	}
}