int VibMotorLeft = 3;	//D3
int VibMotorCenter = 5;	//D5
int VibMotorRight = 6;	//D6
int led = 13;

int currentValue = 0;
int vibValues[] = {0, 0, 0};

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
    int incomingValue = Serial.read();

    vibValues[currentValue] = incomingValue;

    currentValue++;
    if (currentValue > 2) {
      currentValue = 0;
      vibMotorControl(vibValues);
      delay(5000);
      vibMotorsOff();
    }

    Serial.print("--Arduino received: ");
    Serial.println(incomingValue);
  }
}

void vibMotorControl(int intensity[3]) {
  for (int i = 0; i <= sizeof(intensity); i++) {
    if (i == 0) {
      analogWrite(VibMotorLeft, convIntens(intensity[i]));
    }
    if (i == 1) {
      analogWrite(VibMotorCenter, convIntens(intensity[i]));
    }
    if (i == 2) {
      analogWrite(VibMotorRight, convIntens(intensity[i]));
    }
  }
}

void vibMotorsOff() {
  digitalWrite(VibMotorLeft, LOW);
  digitalWrite(VibMotorCenter, LOW);
  digitalWrite(VibMotorRight, LOW);
}

int convIntens(int intens) {
  // Motor off
  if (intens == 0) {
    return intens;
  }
  // Motor warning
  if (intens == 100) {
    return intens = 255;
  }
  //1 Meter
  if (intens == 10) {
    return intens = 225;
  }
  //2 Meter
  if (intens == 20) {
    return intens = 200;
  }
  //3 Meter
  if (intens == 30) {
    return intens = 175;
  }

  return 0;
}

