int VibMotorLeft = 3;  //D3
int VibMotorCenter = 6; //D5
int VibMotorRight = 5;  //D6
int led = 13;

int const OFF = 0;
int const ZONE1 = 1;
int const ZONE2 = 2;
int const ZONE3 =  3;
int const ZONE4 = 4;
int const ZONE5 = 5;
int const ZONE6 = 6;
int const ZONE7 = 7;
int const ZONE8 = 8;
int const WARNING = 9;

int const ALLOFF = 127;

int const MOTOR_LEFT  = 10;
int const MOTOR_CENTER = 20;
int const MOTOR_RIGHT = 30;


void setup()  {
  Serial.begin(9600);

  pinMode(led, OUTPUT);
  pinMode( VibMotorLeft, OUTPUT );
  pinMode( VibMotorCenter, OUTPUT );
  pinMode( VibMotorRight, OUTPUT );
}

void loop() {
  digitalWrite(13, LOW);

  if (Serial.available()) {
    int inValue = Serial.read();

    //ALL OFF
    if ( inValue == ALLOFF ) {
      vibMotorsOff();
    }

    //Left Motor
    if ( inValue >= MOTOR_LEFT && inValue < MOTOR_CENTER) {
      digitalWrite(13, HIGH);
      analogWrite(VibMotorLeft, getIntensity(MOTOR_LEFT, inValue));
    }

    //Center Motor
    if ( inValue >= MOTOR_CENTER && inValue < MOTOR_RIGHT) {
      digitalWrite(13, HIGH);
      analogWrite(VibMotorCenter, getIntensity(MOTOR_CENTER, inValue));
    }

    if ( inValue >= MOTOR_RIGHT && inValue < 40) {
      digitalWrite(13, HIGH);
      analogWrite(VibMotorRight, getIntensity(MOTOR_RIGHT, inValue));
    }
  }
  //Messagereceived feedback
  //Serial.print("Arduino received!");
}

void vibMotorsOff() {
  digitalWrite(VibMotorLeft, LOW);
  digitalWrite(VibMotorCenter, LOW);
  digitalWrite(VibMotorRight, LOW);
}

int getIntensity(int base, int msg) {
  int intensFilter = (msg - base);

  if ( intensFilter == WARNING) {
    return 255;
  }

  if ( intensFilter == OFF ) {
    return 0;
  }

  if ( intensFilter == ZONE1) {
    return 200;
  }

  if ( intensFilter == ZONE2) {
    return 160;
  }

  if ( intensFilter == ZONE3) {
    return 120;
  }
}

