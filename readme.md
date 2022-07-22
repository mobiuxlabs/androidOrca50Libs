[![](https://jitpack.io/v/mobiuxlabs/androidOrca50Libs.svg)](https://jitpack.io/#mobiuxlabs/androidOrca50Libs)


# Reader Library for Orca50 Device And Lynx Device

Steps 1: add jitpack repo in project-level gradle
   
     repositories {
          maven { url 'https://jitpack.io' }
      } 

Steps 2: add in app-level gradle dependencies

    dependencies  {
 	       implementation 'com.github.mobiuxlabs:androidOrca50Libs:v1.0.2'
    }
    
Steps 3: extends RFIDReaderBaseActivity in you activity and override onRfidScan(RFIDTag rfidTag)

    public class MainActivity extends RFIDReaderBaseActivity {
   
        @Override
        protected void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            setContentView(R.layout.activity_main); 
        }
    
        @Override
        public void onRfidScan(RFIDTag rfidTag) {
            Toast.makeText(this, ""+rfidTag.getEpc(), Toast.LENGTH_SHORT).show(); 
        }
        
        @Override
        public void onRFIDScanEnd(RFIDTag.InventoryTagEnd tagEnd) {
              Toast.makeText(this, "On Scan End : totat tag count is : " + tagEnd.mTagCount, Toast.LENGTH_SHORT).show();
        }    
    }
