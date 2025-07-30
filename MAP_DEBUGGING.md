# Map Debugging Guide

## Steps to Debug Map Issues

### 1. Check Logcat Output
Run the app and check the Android Studio Logcat for these log messages:

**Expected Log Messages:**
```
HomeFragment: Setting up map...
HomeFragment: Map is ready!
HomeFragment: Map camera moved to Austin area
HomeFragment: Added test marker at Austin
HomeFragment: Tools data observed in onCreateView: X tools
HomeFragment: First tool: [Tool Name] at [latitude], [longitude]
HomeFragment: Tools data received: X tools
HomeFragment: Adding markers for X tools
HomeFragment: Adding marker for tool: [Tool Name] at location: [lat], [lng]
HomeFragment: Moving camera to show all markers
```

### 2. Common Issues and Solutions

#### Issue: "Map is ready!" not appearing
**Problem:** Google Maps API key issue
**Solution:** 
- Check that you have a valid API key in `AndroidManifest.xml`
- Ensure "Maps SDK for Android" is enabled in Google Cloud Console
- Verify the API key has no restrictions or correct restrictions

#### Issue: "Tools data received: 0 tools" or "null"
**Problem:** No data from server
**Solution:**
- Check if your backend server is running
- Verify the API endpoint is working
- Check network connectivity

#### Issue: "Adding markers for X tools" but no markers visible
**Problem:** Invalid coordinates or camera position
**Solution:**
- Check if coordinates are valid (not 0,0)
- Verify the camera is positioned correctly
- Look for "Moving camera to show all markers" log

#### Issue: Map shows but no test marker
**Problem:** Map API key or permissions
**Solution:**
- Check API key is valid
- Ensure location permissions are granted
- Verify Google Play Services is up to date

### 3. Quick Tests

#### Test 1: API Key
1. Open `AndroidManifest.xml`
2. Check the meta-data tag has a valid API key
3. Try the API key in a browser: `https://maps.googleapis.com/maps/api/staticmap?center=30.2672,-97.7431&zoom=11&size=400x400&key=YOUR_API_KEY`

#### Test 2: Server Data
1. Check if your backend is running
2. Test the tools endpoint: `http://your-server/tools`
3. Verify tools have valid latitude/longitude values

#### Test 3: Map View
1. Switch to map view
2. Look for the test marker at Austin, TX
3. If test marker appears, the issue is with tool data
4. If no test marker, the issue is with map setup

### 4. Expected Behavior

**When Working Correctly:**
- Map should show a test marker at Austin, TX
- When tools data loads, test marker should be replaced with tool markers
- Camera should automatically adjust to show all tool markers
- Each marker should show tool name, price, and owner

### 5. Next Steps

1. Run the app with debugging enabled
2. Check Logcat for the debug messages above
3. Identify which step is failing
4. Follow the corresponding solution
5. If still having issues, share the Logcat output 