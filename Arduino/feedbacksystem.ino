int VibMotorLeft = 3;	//D3
int VibMotorCenter = 5;	//D5
int VibMotorRight = 6;	//D6
int led = 13;
int recv = 0;


void setup() {
    Serial.begin(9600); // // opens serial port, sets data rate to 9600 bps

    pinMode(led, OUTPUT);
    pinMode( VibMotorLeft, OUTPUT );
    pinMode( VibMotorCenter, OUTPUT );
    pinMode( VibMotorRight, OUTPUT );
}
 
void loop() {
  digitalWrite(led, LOW);    // turn the LED off by making the voltage LOW

  // if serial port is available, read incoming bytes
  if (Serial.available() > 0) {
    recv = Serial.read();
 
    // if 'y' (decimal 121) is received, turn LED/Powertail on
    // anything other than 121 is received, turn LED/Powertail off
    if (recv == 121){
      digitalWrite(led, HIGH);
    } else {
      digitalWrite(led, LOW);
    }
     
    // confirm values received in serial monitor window
    Serial.print("--Arduino received: ");
    Serial.println(recv);
  }
}
