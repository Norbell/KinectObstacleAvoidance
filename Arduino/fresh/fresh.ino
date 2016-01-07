int currentValue = 0;
int values[] = {0, 0, 0};

void setup()  {
  pinMode(13, OUTPUT);
  Serial.begin(9600);
}

void loop() {
  digitalWrite(13, LOW);

  if (Serial.available()) {
    Serial.println("Starting the test");
    byte x = Serial.read();
    Serial.println(x);

    /*    int incomingValue = Serial.read();

        values[currentValue] = incomingValue;

        Serial.print("--Arduino received: ");
        Serial.println(incomingValue);

        currentValue++;
        if (currentValue > 2) {
          currentValue = 0;
        }*/
  }
}
