package com.clockTower.CT;


public class CTPlayer {

    /*

	public int animationGroup;
	
	this should be a SyncVar
	public int animationIndex;
	
	public CTSprite mySprite;
	
	public NetworkIdentity networkIdentity;

	public boolean walking;

	public void Start(){
	
	networkIdentity = this.GetComponent<NetworkIdentity>();
	
	if (networkIdentity.isServer)
		{
			print("hey");
		}

	mySprite = this.transform.Find("offsetObject").transform.Find("default").SceneObject.AddComponent<CTSprite>();
	mySprite.offsetObject = mySprite.SceneObject.transform.parent;
	mySprite.myTransform = mySprite.offsetObject.parent;
	mySprite.immuneToDeletion = true;
	
	if (networkIdentity.isLocalPlayer)
		{
			System.out.println("Should now set up local player");
			int startingAnimGroup = 1;	//Jennifer set 2
			int startingAnimIndex = 2;	//Standing normally
			
			if (CTMemory.loader.spriteSlots.size() == 0)
				{	//If slot zero is not yet taken, simply make a new sprite there to be our player object
				CTMemory.loader.spriteSlots.add(mySprite);
				mySprite.myTransform.localPosition = new Vector3(mySprite.myTransform.localPosition.x,mySprite.myTransform.localPosition.y,(-0.5f) - (0.05f*40f));
				}
			else if (CTMemory.loader.spriteSlots[0] == null)
				{
				CTMemory.loader.spriteSlots[0] = mySprite;
				}
			else
				{	//if there is already a sprite in that slot, copy all its values and and take its place
				startingAnimGroup = CTMemory.loader.spriteSlots[0].animGroup;
				startingAnimIndex = CTMemory.loader.spriteSlots[0].animIndex;
				mySprite.transform.parent.transform.parent.localPosition = CTMemory.loader.spriteSlots[0].myTransform.localPosition;
				mySprite.transform.parent.transform.parent.localScale = CTMemory.loader.spriteSlots[0].myTransform.localScale;
				mySprite.transform.parent.localPosition = CTMemory.loader.spriteSlots[0].offsetObject.localPosition;
				mySprite.transform.parent.localScale= CTMemory.loader.spriteSlots[0].offsetObject.localScale;
				mySprite.transform.localPosition = CTMemory.loader.spriteSlots[0].transform.localPosition;
				mySprite.transform.localScale= CTMemory.loader.spriteSlots[0].transform.localScale;
				mySprite.loop = CTMemory.loader.spriteSlots[0].loop;
				Destroy(CTMemory.loader.spriteSlots[0].myTransform.SceneObject);
				CTMemory.loader.spriteSlots[0] = mySprite;
				}
			mySprite.isSprite = true;
			mySprite.mySlotID = 0;
			mySprite.rend = mySprite.GetComponent<MeshRenderer>();
			LoadAndPlaySpriteData(startingAnimGroup, startingAnimIndex, 0);
			animationGroup = startingAnimGroup;
			animationIndex = startingAnimIndex;
		}
		else
		{
		mySprite.animGroup = animationGroup;
		mySprite.animIndex = animationIndex;
		mySprite.isOtherPlayer = true;
		LoadAndPlaySpriteData(mySprite.animGroup, mySprite.animIndex, 0);
		}
	}
	
	public void Update(){
	if (networkIdentity.isLocalPlayer)
		{
		if (CTMemory.userControl){
			if (Input.GetButtonDown("Click") && !walking){
				walking = true;
				if (MustTurnAroundFirst()){
					CmdTurnThenWalkToPosition(Camera.main.ScreenToWorldPoint(Input.mousePosition).x);
					}
				else
					{
					CmdWalkToPosition(Camera.main.ScreenToWorldPoint(Input.mousePosition).x);
					}
				}
			}
		}
	}
	
	private boolean MustTurnAroundFirst(){
		if (mySprite.myTransform.localScale.x < 0 && Input.mousePosition.x >= Camera.main.WorldToScreenPoint(mySprite.myTransform.position).x){	//facing left but you are trying to go right
			return true;
		} else if (mySprite.myTransform.localScale.x > 0 && Input.mousePosition.x < Camera.main.WorldToScreenPoint(mySprite.myTransform.position).x){	//facing right but you are trying to go left
			return true;
		}
		return false;
	}
	
	//TURN THEN WALK TO POSITION
	
	//[Command]
	private void CmdTurnThenWalkToPosition(float destinationPos){
	RpcTurnThenWalkToPosition(destinationPos);	
	}
	
	//[ClientRpc]
	private void RpcTurnThenWalkToPosition(float destinationPos){
	StartCoroutine(TurnThenWalkToPosition(destinationPos));	
	}

	private IEnumerator TurnThenWalkToPosition(float destinationPos){

	mySprite.isSprite = true;
	mySprite.loop = false;
	mySprite.numLoops = 1;
	mySprite.myTransform.localScale = new Vector3(-mySprite.myTransform.localScale.x,mySprite.myTransform.localScale.y,mySprite.myTransform.localScale.z);
	
	LoadAndPlaySpriteData(mySprite.animGroup, CTMemory.GetTurningAnimation(mySprite.animGroup), 0);
	mySprite.isAnimating = true;
	
	mySprite.rend.enabled = true;
	
	while (mySprite.isAnimating){
		yield return null;
		}
		
	CmdWalkToPosition(destinationPos);
	}
	
	
	//WALK TO POSITION
	
	//[Command]
	private void CmdWalkToPosition(float destinationPos){
	RpcWalkToPosition(destinationPos);	
	}
	
	//[ClientRpc]
	private void RpcWalkToPosition(float destinationPos){
	StartCoroutine(WalkToPosition(destinationPos));	
	}
	
	private IEnumerator WalkToPosition(float destinationPos){
		
		mySprite.isAnimating = false;
		mySprite.isSprite = true;
		mySprite.loop = true;
		mySprite.numLoops = 1;
		
		LoadAndPlaySpriteData(mySprite.animGroup, CTMemory.GetWalkingAnimation(mySprite.animGroup), 0);

		StartCoroutine(mySprite.WaitUntilAtPosition(destinationPos, destinationPos, (short)Math.Round(mySprite.myTransform.localScale.x)));
		
		while (mySprite.busy){
			yield return null;
			}
		
		walking = false;
		}
		
	//LOAD PLAYER SPRITE DATA (done like this so that it syncs across network)
	
	public void LoadAndPlaySpriteData(int animGroupNumber, int animIndex, int startingFrame){
		animationGroup = animGroupNumber;
		animationIndex = animIndex;
		CmdLoadAndPlaySpriteData(animGroupNumber, animIndex, startingFrame, mySprite.myTransform.localPosition, mySprite.myTransform.localEulerAngles, mySprite.myTransform.localScale);	//position is set here just so that all the clients keep up with where this sprite is supposed to be (because we don't use NetworkTransform, so this method is required instead)
	}
	
	//[Command]
	public void CmdLoadAndPlaySpriteData(int animGroupNumber, int animationIndex, int startingFrame, Vector3 position, Vector3 rotation, Vector3 scale){
		RpcLoadAndPlaySpriteData(animGroupNumber, animationIndex, startingFrame, position, rotation, scale);
	}
	
	//[ClientRpc]
	public void RpcLoadAndPlaySpriteData(int animGroupNumber, int animationIndex, int startingFrame, Vector3 position, Vector3 rotation, Vector3 scale){
		mySprite.myTransform.localPosition = position;
		mySprite.myTransform.localEulerAngles = rotation;
		mySprite.myTransform.localScale = scale;
		mySprite.LoadSpriteDataIntoSprite(animGroupNumber, animationIndex, startingFrame);
		mySprite.UpdateRenderer();
		mySprite.rend.enabled = true;
	}

	[Command]
		void CmdGrantAuthority(SceneObject target)
		{
			// target must have a NetworkIdentity component to be passed through a Command
			// and must already exist on both server and client
			target.GetComponent<NetworkIdentity>().RemoveClientAuthority();
			target.GetComponent<NetworkIdentity>().AssignClientAuthority(connectionToClient);
		}
*/
}
	
	