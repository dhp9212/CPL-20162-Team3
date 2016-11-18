void setup()
{
    Serial.begin(9600);
}
 
void loop()
{ 
    unsigned int val;
    float dat;
    
    val=analogRead(0.0);
    dat= (500.0 * val) /1024.0;
    
    Serial.print("Temp: ");
    Serial.print(dat,1);
    Serial.println(" C");
    delay(1000);
}


