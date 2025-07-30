# Google Maps Setup Instructions

## Prerequisites
1. A Google Cloud Platform account
2. Google Maps API enabled

## Steps to Get API Key

1. Go to the [Google Cloud Console](https://console.cloud.google.com/)
2. Create a new project or select an existing one
3. Enable the following APIs:
   - Maps SDK for Android
   - Places API (if you plan to use places features)
4. Go to "Credentials" in the left sidebar
5. Click "Create Credentials" → "API Key"
6. Copy the generated API key

## Configure the API Key

1. Open `app/src/main/AndroidManifest.xml`
2. Find the meta-data tag with `com.google.android.geo.API_KEY`
3. Replace `YOUR_API_KEY_HERE` with your actual API key:

```xml
<meta-data
    android:name="com.google.android.geo.API_KEY"
    android:value="your_actual_api_key_here" />
```

## Security Best Practices

For production apps, consider:
1. Restricting the API key to your app's package name
2. Using API key restrictions in Google Cloud Console
3. Storing the API key in a secure location (not in version control)

## Testing

After setting up the API key:
1. Build and run the app
2. Navigate to the Home screen
3. Tap the "Map" button to switch to map view
4. Grant location permissions when prompted
5. The map should display with markers for available tools

## Troubleshooting

- If the map doesn't load, check that the API key is correct
- Ensure the Maps SDK for Android is enabled in Google Cloud Console
- Check that location permissions are granted
- Verify internet connectivity 