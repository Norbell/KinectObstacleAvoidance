void setup() {
  Serial.begin(9600);
}

void loop() {
  if (Serial.available()) {
    while (Serial.available() > 0) {
      char character = Serial.read();
      Serial.print(character);
    }
  }
}
