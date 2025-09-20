# Android Icon Resources

This directory should contain PNG icon files for different screen densities:

## Required PNG files:
- mipmap-mdpi/ic_launcher.png (48x48)
- mipmap-hdpi/ic_launcher.png (72x72)  
- mipmap-xhdpi/ic_launcher.png (96x96)
- mipmap-xxhdpi/ic_launcher.png (144x144)
- mipmap-xxxhdpi/ic_launcher.png (192x192)

## How to generate:
1. Use Android Studio's Image Asset tool
2. Or use online tools like:
   - https://icon.kitchen/
   - https://appicon.co/
3. Or use command line tools like ImageMagick

## Source files:
- app_icon.svg (full icon)
- app_icon_foreground.svg (foreground only for adaptive icons)

The adaptive icon system will use the vector drawable defined in ic_launcher_foreground.xml