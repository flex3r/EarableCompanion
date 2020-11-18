# EarableCompanion
## Usage

### Connecting
- Initially, the FloatingActionButton labeled "Connect" can be used to start the scanning process.
- When there is already an active connection to a device, the smaller FloatinActionButton with the Bluetooth icon can be used to start an additional scan.

### Configuration
- If a device is supported, a small cogwheel icon will be visible in the devices information card along with the supported sensors. Tapping the item will then bring up a screen with the available configuration.
- If a device has the ability to store configuration, an additional FloatingActionButton wil be visible in the configuration screen. Tapping this button will attempt to save the current configuration on the device.

### Recording
- If at least one device is connected, a FloatingActionButton labeled "Record" is visible, which starts the recording process.
- Before a recording is started, a basic activity label can be chosen from.
- Alternatively, the label "Custom" can be chosen to input a custom title.
- Additional activity labels can be added and edited in the settings screen.


### Data viewing & Exporting
- Existing recordings can be viewed in the "Data" bottom navigation tab, detailed information can be accessed by tapping on the item cards.
- When tapping the "Export" FloatingActionbutton, the user has the ability to select the file location and name for the exported recording.
- The Recording will be saved in the csv table format.

### Additional features
These features have to be enabled separately in the Settings screen.
- The app can intercept media button events when connected via a regular bluetooth connection and the required profile is available. These button events will then be included in the recording.
- The app can record from a microphone, if a supported device is available. The microphone recording can be exported separately in a recordings detail screen.
