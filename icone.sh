#!/bin/bash
#convert ic_launcher.webp     -resize 36x36\>         app/src/main/res/mipmap-ldpi/ic_launcher.webp
#convert ic_launcher.webp     -resize 48x48\>         app/src/main/res/mipmap-mdpi/ic_launcher.webp 
#convert ic_launcher.webp     -resize 72x72\>         app/src/main/res/mipmap-hdpi/ic_launcher.webp
#convert ic_launcher.webp     -resize 96x96\>         app/src/main/res/mipmap-xhdpi/ic_launcher.webp  
#convert ic_launcher.webp     -resize 144x144\>       app/src/main/res/mipmap-xxhdpi/ic_launcher.webp 
#convert ic_launcher.webp     -resize 192x192\>       app/src/main/res/mipmap-xxxhdpi/ic_launcher.webp              

convert ic_launcher.webp     -resize 192x192\>       app/src/main/res/mipmap-mdpi/ic_launcher.webp 
convert ic_launcher.webp     -resize 192x192\>       app/src/main/res/mipmap-hdpi/ic_launcher.webp
convert ic_launcher.webp     -resize 192x192\>       app/src/main/res/mipmap-xhdpi/ic_launcher.webp          
convert ic_launcher.webp     -resize 192x192\>       app/src/main/res/mipmap-xxhdpi/ic_launcher.webp 
convert ic_launcher.webp     -resize 192x192\>       app/src/main/res/mipmap-xxxhdpi/ic_launcher.webp      

cp ic_foreground.webp              app/src/main/res/mipmap-xxxhdpi/ic_foreground.webp 
cp ic_foreground.webp              app/src/main/res/mipmap-xxhdpi/ic_foreground.webp
cp ic_foreground.webp              app/src/main/res/mipmap-xhdpi/ic_foreground.webp
cp ic_foreground.webp              app/src/main/res/mipmap-hdpi/ic_foreground.webp
cp ic_foreground.webp              app/src/main/res/mipmap-mdpi/ic_foreground.webp

 



#######################################################################################


#######################################################################################



