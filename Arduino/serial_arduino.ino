int VibMotorLeft = 3;	//D3
int VibMotorCenter = 5;	//D5
int VibMotorRight = 6;	//D6
int led = 13;

int oldvib = 0;
int vibAll = 0;
int vibLeft = 0;
int vibCenter = 0;
int vibRight = 0;

void setup()
{
	Serial.begin(9600); // // opens serial port, sets data rate to 9600 bps

	pinMode(led, OUTPUT);
	pinMode( VibMotorLeft, OUTPUT );
	pinMode( VibMotorCenter, OUTPUT );
	pinMode( VibMotorRight, OUTPUT );
}

void loop()
{ 
	digitalWrite(led, LOW);    // turn the LED off by making the voltage LOW
	if (Serial.available() > 0) {
		// read the incoming byte:
		//int vib = Serial.read()-'0';  //not using this
		int vib = Serial.parseInt();
		Serial.print(vib);    


	    if(vib == 1){  
	    	if( vibAll == 0) {
				digitalWrite(VibMotorLeft, HIGH);  
				digitalWrite(VibMotorCenter, HIGH);
				digitalWrite(VibMotorRight, HIGH); 
				vibAll = 1;
    		} else {
				digitalWrite(VibMotorLeft, LOW);  
				digitalWrite(VibMotorCenter, LOW);
				digitalWrite(VibMotorRight, LOW); 	    		
				vibAll = 0;
    		}			
	    }


    	//Left-Motor D3
	    if (vib == 5){
	    	if( vibLeft == 0) {
				digitalWrite(VibMotorLeft, HIGH);  
				vibLeft = 1;
    		} else {
				digitalWrite(VibMotorLeft, LOW);  
				vibLeft = 0;
    		}			     
	    }
        

        //Center-Motor D5
	    if(vib == 6) {
	    	if( vibCenter == 0) {
				digitalWrite(VibMotorCenter, HIGH);
				vibCenter = 1;
    		} else {
				digitalWrite(VibMotorCenter, LOW);
				vibCenter = 0;
    		}		
	    } 
    	

    	//Right-Motor D6
	    if(vib == 7){
	    	if( vibRight == 0) {
				digitalWrite(VibMotorRight, HIGH); 
				vibRight = 1;
    		} else {
				digitalWrite(VibMotorRight, LOW); 	    		
				vibRight = 0;
    		}
	    }      
	}
}

