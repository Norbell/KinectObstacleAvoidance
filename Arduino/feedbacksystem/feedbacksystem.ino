int VibMotorLeft = 3;	//D3
int VibMotorCenter = 5;	//D5
int VibMotorRight = 6;	//D6
int led = 13;
bool done = false;

char delimiters[] = ";";
char* valPosition = NULL;
int vibValue;
char msg[6];
int i = 0;

void setup() {
  Serial.begin(9600); // // opens serial port, sets data rate to 9600 bps

  pinMode(led, OUTPUT);
  pinMode( VibMotorLeft, OUTPUT );
  pinMode( VibMotorCenter, OUTPUT );
  pinMode( VibMotorRight, OUTPUT );
}

void loop() {
  if (Serial.available()) {
    //lets read byte for byte but leave room for NULL
    msg[i] = Serial.read();
    i++  ;
  } else {
       done = true;
    msg[i] = '\0'; //end with NULL
    i = 0; 
  }
  
  if(done){
  valPosition = strtok(msg, delimiters);
  while (valPosition != NULL) {
    vibValue = atoi(valPosition);
    Serial.println(vibValue);
    i++;
    //Here we pass in a NULL value, which tells strtok to continue working with the previous string
    valPosition = strtok(NULL, delimiters);
  }}
}


void vibMotorsOn(int recv) {
  // if 'y' (decimal 121) is received, turn LED/Powertail on
  // anything other than 121 is received, turn LED/Powertail off
  if (recv == 121) {
    digitalWrite(led, HIGH);
  } else {
    digitalWrite(led, LOW);
  }
}
