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

    String ret = "TMPR#";
    String sval = String(dat);
    Serial.print(ret + sval);
    delay(10000);
}


