package com.clockTower.CT;

public class CTAnimation {

	public CTAnimation(int _animGroup, int _animIndex, int _startingFrame) {
		animGroup = _animGroup;
		animIndex = _animIndex;
		startingFrame = _startingFrame;
	}
	public CTAnimation(int _animGroup, int _animIndex, int _startingFrame, boolean _loop, int _numLoops) {
		animGroup = _animGroup;
		animIndex = _animIndex;
		startingFrame = _startingFrame;
		loop = _loop;
		numLoops = _numLoops;
	}
	public int animGroup = 0;
	public int animIndex = 0;
	public int startingFrame = 0;
	public boolean loop = false;
	public int numLoops = 1;
}
